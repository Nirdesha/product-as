/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.appserver.test.integration.statisticspublishing;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;

import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


public class StatisticsPublisherTestIT {
    Logger log = Logger.getLogger(StatisticsPublisherTestIT.class);
    //    private static final String STREAM_NAME = "org.wso2.http.stats";
//    private static final String VERSION = "1.0.0";
    private ThriftTestServer thriftTestServer;

//
//    private static final String STREAM_DEFN = "{" +
//            "  'name':'" + STREAM_NAME + "'," +
//            "  'version':'" + VERSION + "'," +
//            "  'metaData':[ " +
//            "          {'name':'server_address','type':'STRING'}" +
//            "          {'name':'server_name','type':'STRING'}" +
//            "  ]," +
//            "  'payloadData':[" +
//            "          {'name':'appName','type':'STRING'}," +
//            "          {'name':'appVersion','type':'STRING'}," +
//            "          {'name':'userId','type':'STRING'}," +
//            "          {'name':'requestUri','type':'STRING'}," +
//            "          {'name':'timestamp','type':'STRING'}," +
//            "          {'name':'resourcePath','type':'STRING'}," +
//            "          {'name':'appType','type':'STRING'}," +
//            "          {'name':'appDisplayName','type':'STRING'}," +
//            "          {'name':'sessionId','type':'STRING'}," +
//            "          {'name':'httpMethod','type':'STRING'}," +
//            "          {'name':'requestContentType','type':'STRING'}," +
//            "          {'name':'responseContentType','type':'STRING'}," +
//            "          {'name':'responseHttpStatusCode','type':'LONG'}," +
//            "          {'name':'remoteAddress','type':'STRING'}," +
//            "          {'name':'referrer','type':'STRING'}," +
//            "          {'name':'userAgent','type':'STRING'}," +
//            "          {'name':'host','type':'STRING'}," +
//            "          {'name':'remoteUser','type':'STRING'}," +
//            "          {'name':'authType','type':'STRING'}," +
//            "          {'name':'responseTime','type':'LONG'}," +
//            "          {'name':'requestSizeBytes','type':'LONG'}," +
//            "          {'name':'responseSizeBytes','type':'LONG'}," +
//            "          {'name':'requestHeader','type':'STRING'}," +
//            "          {'name':'responseHeader','type':'STRING'}," +
//            "          {'name':'language','type':'STRING'}," +
//            "  ]" +
//            "}";


    @BeforeClass
    public static void init() {

        DataPublisherTestUtil.setKeyStoreParams();
        DataPublisherTestUtil.setTrustStoreParams();

    }

    private String convertJSONtoString() throws IOException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;
        try {

            Object obj = parser.parse(new FileReader(DataPublisherTestUtil.getStreamDefinitionPath()));

            jsonObject = (JSONObject) obj;

        } catch (Exception e) {
            throw new IOException("Failed");
        }
        return jsonObject.toJSONString();
    }

    private synchronized void startServer(int port) throws
            StreamDefinitionStoreException, MalformedStreamDefinitionException, DataBridgeException, IOException {
        thriftTestServer = new ThriftTestServer();
        thriftTestServer.start(port);
        thriftTestServer.addStreamDefinition(convertJSONtoString(), -1234);

    }

    @Test
    public void testDataEndpoint() throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException,
            TransportException, DataEndpointException, DataEndpointConfigurationException,
            MalformedStreamDefinitionException, DataBridgeException, StreamDefinitionStoreException, IOException {

        startServer(7611);

        String url = "http://localhost:8080";
        try {
            URL requestUrl = new URL(url);

            int numberOfRequestsMade = 10;
            int responseCode;

            for (int i = 0; i < numberOfRequestsMade; i++) {
                HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
                connection.setRequestMethod("GET");
                responseCode = connection.getResponseCode();
                Assert.assertEquals(200, responseCode);
            }

            Assert.assertEquals(numberOfRequestsMade, thriftTestServer.getNumberOfEventsReceived());
            thriftTestServer.resetReceivedEvents();
        } catch (IOException e) {
            Assert.fail("Fail connection to the server. Error: " + e.getMessage());
        }

        thriftTestServer.stop();
    }


}
