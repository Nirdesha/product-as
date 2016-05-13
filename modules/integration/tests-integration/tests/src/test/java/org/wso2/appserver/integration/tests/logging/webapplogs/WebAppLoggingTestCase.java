/*
*Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.appserver.integration.tests.logging.webapplogs;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.appserver.integration.common.clients.LogViewerClient;
import org.wso2.appserver.integration.common.clients.WebAppAdminClient;
import org.wso2.appserver.integration.common.utils.ASIntegrationLoggingUtil;
import org.wso2.appserver.integration.common.utils.ASIntegrationTest;
import org.wso2.appserver.integration.common.utils.WebAppDeploymentUtil;
import org.wso2.appserver.integration.common.utils.WebAppTypes;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.appserver.integration.common.utils.ASHttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.carbon.logging.view.stub.types.carbon.PaginatedLogEvent;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * This class contains test cases that to test the logs are generated by webapps
 * deployed in AS are recorded properly under different configurations
 */
@SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
public class WebAppLoggingTestCase extends ASIntegrationTest {

	private static final Log log = LogFactory.getLog(WebAppLoggingTestCase.class);
	private TestUserMode userMode;
	private WebAppAdminClient webAppAdminClient;

	@Factory(dataProvider = "userModeProvider")
	public WebAppLoggingTestCase(TestUserMode userMode) {
		this.userMode = userMode;
	}

	@DataProvider
	private static TestUserMode[][] userModeProvider() {
		return new TestUserMode[][] { new TestUserMode[] { TestUserMode.SUPER_TENANT_ADMIN },
		                              new TestUserMode[] { TestUserMode.TENANT_USER } };
	}

	@BeforeClass(alwaysRun = true)
	public void init() throws Exception {
		super.init(userMode);
	}

	@Test(groups = "wso2.as", description = "Test logs generated by a web app using commons logging API")
	public void testCommonsLogs() throws Exception {

		assertTrue(deploySampleWebApp("commons-logging-test-webapp"),
		           "Commons logging API test web app is not deployed properly in user mode : " + userMode);

		assertEquals(makeRequest(getWebAppURL(WebAppTypes.WEBAPPS) +
		                         "/commons-logging-test-webapp/services/test_logging/logging"), HttpStatus.SC_OK,
		             "Error in commons web app response in user mode : " + userMode);

		String[] commonsLogs = ASIntegrationLoggingUtil.getLogsFromLogfile(
				new File(System.getProperty("carbon.home") + File.separator + "repository" + File.separator + "logs" +
				         File.separator + "web_app_commons.log"));

		LogAvailability logAvailability = readLogs(commonsLogs);

		assertTrue(logAvailability.isInfoLogAvailable(),
		           "Error in INFO logs of the web app which uses commons logging API in user mode : " + userMode);
		assertTrue(logAvailability.isErrorLogAvailable(),
		           "Error in ERROR logs of the web app which uses commons logging API in user mode : " + userMode);
		assertTrue(logAvailability.isWarnLogAvailable(),
		           "Error in WARN logs of the web app which uses commons logging API in user mode : " + userMode);
		assertTrue(logAvailability.isDebugLogAvailable(),
		           "Error in DEBUG logs of the web app which uses commons logging API in user mode : " + userMode);
	}

	@Test(groups = "wso2.as", description = "Test logs generated by a web app using slf4j logging API")
	public void testSlf4jLogs() throws Exception {

		assertTrue(deploySampleWebApp("slf4j-logging-test-webapp"),
		           "slf4j logging API test web app is not deployed properly in user mode : " + userMode);

		assertEquals(makeRequest(getWebAppURL(WebAppTypes.WEBAPPS) +
		                         "/slf4j-logging-test-webapp/services/test_logging/do/logging"), HttpStatus.SC_OK,
		             "Error in slf4j web app response in user mode : " + userMode);

		String[] slf4jLogs = ASIntegrationLoggingUtil.getLogsFromLogfile(
				new File(System.getProperty("carbon.home") + File.separator + "repository" + File.separator + "logs" +
				         File.separator + "web_app_slf4j.log"));

		LogAvailability logAvailability = readLogs(slf4jLogs);

		assertTrue(logAvailability.isInfoLogAvailable(),
		           "Error in INFO logs of the web app which uses slf4j logging API in user mode : " + userMode);
		assertTrue(logAvailability.isErrorLogAvailable(),
		           "Error in ERROR logs of the web app which uses slf4j logging API in user mode : " + userMode);
		assertTrue(logAvailability.isWarnLogAvailable(),
		           "Error in WARN logs of the web app which uses slf4j logging API in user mode : " + userMode);
		assertTrue(logAvailability.isDebugLogAvailable(),
		           "Error in DEBUG logs of the web app which uses slf4j logging API in user mode : " + userMode);
	}

	@Test(groups = "wso2.as", description = "Test java util logs")
	public void testJavaUtilLogs() throws Exception {

		assertTrue(deploySampleWebApp("jul-logs-test-webapp"),
		           "JUL logging test web app is not deployed properly in user mode : " + userMode);

		assertEquals(
				makeRequest(getWebAppURL(WebAppTypes.WEBAPPS) + "/jul-logs-test-webapp/services/test_logging/logging"),
				HttpStatus.SC_OK, "Error in java util web app response in user mode : " + userMode);

		String[] julLogs = ASIntegrationLoggingUtil.getLogsFromLogfile(
				new File(System.getProperty("carbon.home") + File.separator + "repository" + File.separator + "logs" +
				         File.separator + "web_app_java_util_logging.log"));

		LogAvailability logAvailability = readLogs(julLogs);

		assertTrue(logAvailability.isInfoLogAvailable(),
		           "INFO level logs are not working of the webapp which uses JUL in user mode : " + userMode);
		assertTrue(logAvailability.isSevereLogAvailable(),
		           "SEVERE level logs are not working of the webapp which uses JUL in user mode : " + userMode);
		assertTrue(logAvailability.isWarningLogAvailable(),
		           "WARNING level logs are not working of the webapp which uses JUL in user mode : " + userMode);
	}

	@Test(groups = "wso2.as", description = "Test Carbon logging framework")
	public void testCarbonFrameworkLoggingMemoryAppender() throws Exception {

		assertTrue(deploySampleWebApp("carbon-logging-test-webapp"),
		           "Carbon framework logging test web app is not deployed properly in user mode : " + userMode);

		assertEquals(makeRequest(getWebAppURL(WebAppTypes.WEBAPPS) +
		                         "/carbon-logging-test-webapp/services/test_carbon_logging/logging"), HttpStatus.SC_OK,
		             "Error in Carbon logging web app response in user mode : " + userMode);

		LogViewerClient logViewerClient = new LogViewerClient(backendURL, sessionCookie);
		PaginatedLogEvent paginatedLogEvents = logViewerClient.getPaginatedLogEvents(0, "ALL", "", "", "");

		LogEvent[] logEvents = paginatedLogEvents.getLogInfo();

		boolean statusInfo = false;
		boolean statusWarn = false;
		boolean statusError = false;
		boolean statusFatal = false;

		for (LogEvent logEvent : logEvents) {
			if (logEvent.getMessage().contains("WARN LOG - Sample web app")) {
				statusWarn = true;
			} else if (logEvent.getMessage().contains("INFO LOG - Sample web app")) {
				statusInfo = true;
			} else if (logEvent.getMessage().contains("ERROR LOG - Sample web app")) {
				statusError = true;
			} else if (logEvent.getMessage().contains("FATAL LOG - Sample web app")) {
				statusFatal = true;
			}
		}

		assertTrue(statusInfo,
		           "Error in INFO logs of the web app which uses carbon logging framework in user mode : " + userMode);
		assertTrue(statusWarn,
		           "Error in WARN logs of the web app which uses carbon logging framework in user mode : " + userMode);
		assertTrue(statusError,
		           "Error in ERROR logs of the web app which uses carbon logging framework in user mode : " + userMode);
		assertTrue(statusFatal,
		           "Error in FATAL logs of the web app which uses carbon logging framework in user mode : " + userMode);
	}

	@Test(groups = "wso2.as", description = "Test java util logs",
			dependsOnMethods = "testCarbonFrameworkLoggingMemoryAppender")
	public void testCarbonFrameworkLoggingCarbonFileAppender() throws Exception {

		String[] carbonLogs = ASIntegrationLoggingUtil.getLogsFromLogfile(
				new File(System.getProperty("carbon.home") + File.separator + "repository" + File.separator + "logs" +
				         File.separator + "wso2carbon.log"));

		LogAvailability logAvailability = readLogs(carbonLogs);

		assertTrue(logAvailability.isInfoLogAvailable(),
		           "Error in INFO logs of the web app which uses carbon logging framework in user mode : " + userMode);
		assertTrue(logAvailability.isWarnLogAvailable(),
		           "Error in WARN logs of the web app which uses carbon logging framework in user mode : " + userMode);
		assertTrue(logAvailability.isErrorLogAvailable(),
		           "Error in ERROR logs of the web app which uses carbon logging framework in user mode : " + userMode);
		assertTrue(logAvailability.isFatalLogAvailable(),
		           "Error in FATAL logs of the web app which uses carbon logging framework in user mode : " + userMode);
	}

	@AfterClass(alwaysRun = true)
	public void webApplicationDelete() throws Exception {
		sessionCookie = loginLogoutClient.login();
		webAppAdminClient = new WebAppAdminClient(backendURL, sessionCookie);
		String[] warFileNames =
				{ "commons-logging-test-webapp.war", "slf4j-logging-test-webapp.war", "carbon-logging-test-webapp.war",
				  "jul-logs-test-webapp.war" };
		for (String fileName : warFileNames) {
			webAppAdminClient.deleteWebAppFile(fileName, asServer.getDefaultInstance().getHosts().get("default"));
		}
	}

	private boolean deploySampleWebApp(String appName) throws Exception {
		sessionCookie = loginLogoutClient.login();
		webAppAdminClient = new WebAppAdminClient(backendURL, sessionCookie);
		String sampleWebApp =
				System.getProperty("basedir", ".") + File.separator + "target" + File.separator + "resources" +
				File.separator + "artifacts" + File.separator + "AS" + File.separator + "war" + File.separator +
				appName + ".war";

		webAppAdminClient.uploadWarFile(sampleWebApp);
		return WebAppDeploymentUtil.isWebApplicationDeployed(backendURL, sessionCookie, appName);
	}

	private LogAvailability readLogs(String[] logRecordsList) {

		LogAvailability logAvailability = new LogAvailability();
		for (String logRecord : logRecordsList) {
			if (logRecord.contains("INFO LOG")) {
				logAvailability.setIsInfoLogAvailable(true);
			} else if (logRecord.contains("ERROR LOG")) {
				logAvailability.setIsErrorLogAvailable(true);
			} else if (logRecord.contains("WARN LOG")) {
				logAvailability.setIsWarnLogAvailable(true);
			} else if (logRecord.contains("DEBUG LOG")) {
				logAvailability.setIsDebugLogAvailable(true);
			} else if (logRecord.contains("WARNING LOG")) {
				logAvailability.setIsWarningLogAvailable(true);
			} else if (logRecord.contains("SEVERE LOG")) {
				logAvailability.setIsSevereLogAvailable(true);
			} else if (logRecord.contains("FATAL LOG")) {
				logAvailability.setIsFatalLogAvailable(true);
			}
		}

		return logAvailability;
	}

	private int makeRequest(String url) throws IOException {
		HttpResponse response = ASHttpRequestUtil.sendGetRequest(url, null);
		log.debug("Response of the webapp : " + response.getData());
		return response.getResponseCode();
	}

	private class LogAvailability {

		private boolean isInfoLogAvailable = false;
		private boolean isDebugLogAvailable = false;
		private boolean isWarnLogAvailable = false;
		private boolean isWarningLogAvailable = false;
		private boolean isSevereLogAvailable = false;
		private boolean isErrorLogAvailable = false;
		private boolean isFatalLogAvailable = false;

		public boolean isFatalLogAvailable() {
			return isFatalLogAvailable;
		}

		public void setIsFatalLogAvailable(boolean isFatalLogAvailable) {
			this.isFatalLogAvailable = isFatalLogAvailable;
		}

		public boolean isInfoLogAvailable() {
			return isInfoLogAvailable;
		}

		public void setIsInfoLogAvailable(boolean isInfoLogAvailable) {
			this.isInfoLogAvailable = isInfoLogAvailable;
		}

		public boolean isDebugLogAvailable() {
			return isDebugLogAvailable;
		}

		public void setIsDebugLogAvailable(boolean isDebugLogAvailable) {
			this.isDebugLogAvailable = isDebugLogAvailable;
		}

		public boolean isWarnLogAvailable() {
			return isWarnLogAvailable;
		}

		public void setIsWarnLogAvailable(boolean isWarnLogAvailable) {
			this.isWarnLogAvailable = isWarnLogAvailable;
		}

		public boolean isWarningLogAvailable() {
			return isWarningLogAvailable;
		}

		public void setIsWarningLogAvailable(boolean isWarningLogAvailable) {
			this.isWarningLogAvailable = isWarningLogAvailable;
		}

		public boolean isErrorLogAvailable() {
			return isErrorLogAvailable;
		}

		public void setIsErrorLogAvailable(boolean isErrorLogAvailable) {
			this.isErrorLogAvailable = isErrorLogAvailable;
		}

		public boolean isSevereLogAvailable() {
			return isSevereLogAvailable;
		}

		public void setIsSevereLogAvailable(boolean isSevereLogAvailable) {
			this.isSevereLogAvailable = isSevereLogAvailable;
		}

	}
}