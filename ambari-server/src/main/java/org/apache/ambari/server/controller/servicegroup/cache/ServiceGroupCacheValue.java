/**
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
package org.apache.ambari.server.controller.servicegroup.cache;


import java.util.Map;
import java.util.ArrayList;

/**
 * Wrapper object for service group cache value.
 */
public class ServiceGroupCacheValue {
  private long fetchTime;
  private Map<String, Object> response = null;
  private String applicationId = null;
  private String state;
  private String lifetime = null;
  private Map<String, String> quicklinks = null;
  private Integer numContainers;
  private Integer expectedContainers;
  public ArrayList<Map<String, Object>> containers = null;


  public ServiceGroupCacheValue(long fetchTime, Map<String, Object>  response) {
    this.fetchTime = fetchTime;
    this.response = response;
    parseResponse();
  }

  public Map<String, Object> getResponse() {
    return response;
  }

  public void setResponse(Map<String, Object> response) {
    this.response = response;
    parseResponse();
  }

  public Long getFetchTime() {
    return fetchTime;
  }

  public void setFetchTime(Long fetchTime) {
    this.fetchTime = fetchTime;
  }

  public String getApplicationId() {
    return applicationId;
  }

  public String getState() {
    return state;
  }

  public Map<String, String> getQuicklinks() {
    return quicklinks;
  }

  public Integer getNumContainers() {
    return numContainers;
  }

  public Integer getExpectedContainers() {
    return expectedContainers;
  }

  public String getLifetime() {
    return lifetime;
  }

  public ArrayList<Map<String, Object>> getContainers() {
    return containers;
  }

  public void parseResponse() {
    if(response != null) {
      if(response.containsKey("id")) {
        applicationId = (String) response.get("id");
      }

      if(response.containsKey("quicklinks")) {
        quicklinks = (Map<String, String>) response.get("quicklinks");
      }

      if(response.containsKey("state")) {
        state = (String) response.get("state");
      }

      if(response.containsKey("lifetime")) {
        lifetime = (String) response.get("lifetime");
      }

      if(response.containsKey("number_of_containers")) {
        numContainers = ((Double) response.get("number_of_containers")).intValue();
      }

      if(response.containsKey("expected_number_of_containers")) {
        expectedContainers = ((Double) response.get("expected_number_of_containers")).intValue();
      }

      if(response.containsKey("containers")) {
        containers = (ArrayList<Map<String, Object>>) response.get("containers");

      }
    }
  }

  @Override
  public String toString() {
    return "ServiceGroupCacheValue { " +
        "fetchTime=" + fetchTime +
        ", applicationId=" + applicationId +
        ", state=" + state +
        ", quicklinks=" + quicklinks +
        ", lifetime=" + lifetime +
        ", numContainers=" + numContainers +
        ", expectedContainers=" + expectedContainers +
        " }";
  }
}
