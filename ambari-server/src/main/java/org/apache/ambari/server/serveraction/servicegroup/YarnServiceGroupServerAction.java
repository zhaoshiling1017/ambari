/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ambari.server.serveraction.servicegroup;

import com.google.gson.reflect.TypeToken;
import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.actionmanager.HostRoleStatus;
import org.apache.ambari.server.agent.CommandReport;
import org.apache.ambari.server.agent.ExecutionCommand;
import org.apache.ambari.server.controller.ServiceComponentHostResponse;
import org.apache.ambari.server.state.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class YarnServiceGroupServerAction extends ServiceGroupServerAction {
  public static String COMMAND_PARAM_SERVICE_GROUP = "service_group";
  public static String COMMAND_PARAM_DESIRED_STATE = "desired_state";
  public static String COMMAND_PARAM_DASH_API_ENDPOINT = "dash_api_endpoint";

  @Override
  public CommandReport execute(ConcurrentMap<String, Object> requestSharedDataContext) throws AmbariException, InterruptedException {

    Cluster cluster = getCluster();
    if(cluster == null) {
      throw new AmbariException("Missing cluster object");
    }
    Map<String, String> commandParams = getCommandParameters();
    if(commandParams == null) {
      throw new AmbariException("Missing command params");
    }
    if(!commandParams.containsKey(COMMAND_PARAM_SERVICE_GROUP)) {
      throw new AmbariException("Missing service group");
    }
    if(!commandParams.containsKey(COMMAND_PARAM_DESIRED_STATE)) {
      throw new AmbariException("Missing desired state");
    }
    if(!commandParams.containsKey(COMMAND_PARAM_DASH_API_ENDPOINT)) {
      throw new AmbariException("Missing Dash API endpoint");
    }

    String serviceGroupName = commandParams.get(COMMAND_PARAM_SERVICE_GROUP);
    State desiredState = State.valueOf(commandParams.get(COMMAND_PARAM_DESIRED_STATE));
    String dashApiEndpoint = commandParams.get(COMMAND_PARAM_DASH_API_ENDPOINT);

    ServiceGroup sg = cluster.getServiceGroup(serviceGroupName);
    String sgType = sg.getServiceGroupType();

    if(!sgType.equalsIgnoreCase("YARN")) {
      throw new AmbariException("Invalid service group type " + sgType);
    }
    if(StringUtils.isBlank(sg.getAssemblyFile())) {
      throw new AmbariException("Assembly file contents not set");
    }


    actionLog.writeStdOut("=================================================");
    actionLog.writeStdOut("Service Group : " + serviceGroupName);
    actionLog.writeStdOut("Desired State : " + desiredState);
    actionLog.writeStdOut("Dash API Endpoint : " + dashApiEndpoint);
    actionLog.writeStdOut("=================================================");

    if(desiredState == State.INSTALLED && sg.getCurrentState() == State.INIT) {
      // Installing Service Group
      actionLog.writeStdOut("Installing service group " + serviceGroupName);
      try {
        URL url = new URL(dashApiEndpoint);
        HttpURLConnection httpRequest = (HttpURLConnection) url.openConnection();
        httpRequest.setDoOutput(true);
        httpRequest.setDoInput(true);
        httpRequest.setRequestMethod("POST");
        httpRequest.setRequestProperty("Content-Type", "application/json");
        httpRequest.setRequestProperty("Content-Length", String.valueOf(sg.getAssemblyFile().length()));
        httpRequest.setRequestProperty("Accept", "application/json");

        OutputStream os = httpRequest.getOutputStream();
        os.write(sg.getAssemblyFile().getBytes());
        int responseCode = httpRequest.getResponseCode();
        actionLog.writeStdOut("Dash response code = " + responseCode);
        if (responseCode != 202) {
          String errorMessage = "Failed to install service group " + serviceGroupName + " via Dash API endpoint " + dashApiEndpoint;
          actionLog.writeStdErr(errorMessage);
          throw new AmbariException(errorMessage);
        }
      } catch (Exception e) {
        String errorMessage = "Hit exception while installing service group " + serviceGroupName + " via Dash API endpoint " + dashApiEndpoint;
        actionLog.writeStdErr(errorMessage);
        actionLog.writeStdErr("Exception = " + e.getMessage());
        throw new AmbariException(errorMessage, e);
      }
      sg.setCurrentState(State.INSTALLED);
      actionLog.writeStdOut("Service group " + serviceGroupName + " successfully installed");
    } else if(desiredState == State.STARTED && sg.getCurrentState() == State.INSTALLED) {
      // Starting service group
      actionLog.writeStdOut("Waiting for service group " + serviceGroupName + " to start");
      String urlString = dashApiEndpoint + "/" + serviceGroupName.toLowerCase();
      Boolean started = false;
      while(!started) {
        try {
          URL url = new URL(urlString);
          HttpURLConnection httpRequest = (HttpURLConnection) url.openConnection();
          InputStream inputStream = httpRequest.getInputStream();
          String jsonResponse = IOUtils.toString(inputStream, "UTF-8");
          actionLog.writeStdOut("*****************************************************");
          actionLog.writeStdOut("Get Response:");
          actionLog.writeStdOut(jsonResponse);
          actionLog.writeStdOut("*****************************************************");
          Map<String, Object> responseMap = gson.fromJson(jsonResponse, new TypeToken<Map<String, Object>>() {
          }.getType());

          if (responseMap != null && responseMap.containsKey("state") && responseMap.containsKey("containers")) {
            started = true;
            String appState = (String) responseMap.get("state");
            if (appState.equalsIgnoreCase("RUNNING")) {
              for (Map<String, String> cMap : (ArrayList<Map<String, String>>) responseMap.get("containers")) {
                String containerState = cMap.get("state");
                if (!containerState.equalsIgnoreCase("READY")) {
                  started = false;
                  break;
                }
              }
            } else {
              started = false;
            }
          }
        } catch (Exception e) {
          String errorMessage = "Hit exception while waiting for service group " + serviceGroupName + " to start via Dash API endpoint " + dashApiEndpoint;
          actionLog.writeStdErr(errorMessage);
          actionLog.writeStdErr("Exception = " + e.getMessage());
          actionLog.writeStdErr("Will retry!");
        }
        if (!started) {
          Thread.sleep(1000);
        }
      }
      sg.setCurrentState(State.STARTED);
      actionLog.writeStdOut("Service group " + serviceGroupName + " successfully started");
    } else if(desiredState == State.INSTALLED && sg.getCurrentState() == State.STARTED) {
      // Stopping service group
      actionLog.writeStdOut("Stopping service group " + serviceGroupName);
      sg.setCurrentState(State.INSTALLED);
      actionLog.writeStdOut("Service group " + serviceGroupName + "successfully stopped");
    }
    sg.persist();
    return createCommandReport(0, HostRoleStatus.COMPLETED, "{}", actionLog.getStdOut(), actionLog.getStdErr());
  }

}
