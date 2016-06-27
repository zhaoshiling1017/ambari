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

package org.apache.ambari.server.controller;

import java.util.ArrayList;
import java.util.Map;


public class ServiceGroupResponse {

  private Long clusterId;
  private String clusterName;
  private String serviceGroupName;
  private String serviceGroupDisplayName;
  private String serviceGroupType;
  private String assemblyFile;
  private String desiredState;
  private String currentState;
  private String applicationId;
  private String lifetime = null;
  private Map<String, String> quicklinks;
  private Integer numContainers;
  private Integer expectedContainers;
  private ArrayList<Map<String, Object>> containers;

  public ServiceGroupResponse(Long clusterId, String clusterName, String serviceGroupName, String serviceGroupDisplayName,
                              String serviceGroupType, String assemblyFile, String desiredState, String currentState) {
    this.clusterId = clusterId;
    this.clusterName = clusterName;
    this.serviceGroupName = serviceGroupName;
    this.serviceGroupDisplayName = serviceGroupDisplayName;
    this.serviceGroupType = serviceGroupType;
    this.assemblyFile = assemblyFile;
    this.desiredState = desiredState;
    this.currentState = currentState;
  }

  /**
   * @return the clusterId
   */
  public Long getClusterId() {
    return clusterId;
  }

  /**
   * @param clusterId the clusterId to set
   */
  public void setClusterId(Long clusterId) {
    this.clusterId = clusterId;
  }

  /**
   * @return the clusterName
   */
  public String getClusterName() {
    return clusterName;
  }

  /**
   * @param clusterName the clusterName to set
   */
  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  /**
   * @return the service group name
   */
  public String getServiceGroupName() {
    return serviceGroupName;
  }

  /**
   * @param  serviceGroupName the service group name
   */
  public void setServiceGroupName(String serviceGroupName) {
    this.serviceGroupName = serviceGroupName;
  }

  /**
   * @return the service group display name
   */
  public String getServiceGroupDisplayName() {
    return serviceGroupDisplayName;
  }

  /**
   * @param  serviceGroupDisplayName the service group display name
   */
  public void setServiceGroupDisplayName(String serviceGroupDisplayName) {
    this.serviceGroupDisplayName = serviceGroupDisplayName;
  }

  /**
   * @return the desired state of the service group
   */
  public String getDesiredState() {
    return desiredState;
  }

  /**
   * @param  desiredState the desired state of the service group
   */
  public void setDesiredState(String desiredState) {
    this.desiredState = desiredState;
  }

  /**
   * @return the current state of the service group
   */
  public String getCurrentState() {
    return currentState;
  }

  /**
   * @param  currentState the current state of the service group
   */
  public void setCurrentState(String currentState) {
    this.currentState = currentState;
  }


  /**
   * @return the service group type
   */
  public String getServiceGroupType() {
    return serviceGroupType;
  }

  /**
   * @param  serviceGroupType the service group type
   */
  public void setServiceGroupType(String serviceGroupType) {
    this.serviceGroupType = serviceGroupType;
  }

  /**
   * @return the assembly file
   */
  public String getAssemblyFile() {
    return assemblyFile;
  }

  /**
   * @param  assemblyFile the assembly file
   */
  public void setAssemblyFile(String assemblyFile) {
    this.assemblyFile = assemblyFile;
  }

  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public Map<String, String> getQuickLinks() {
    return quicklinks;
  }

  public void setQuickLinks(Map<String, String> quicklinks) {
    this.quicklinks = quicklinks;
  }

  public ArrayList<Map<String, Object>> getContainers() {
    return containers;
  }

  public void setContainers(ArrayList<Map<String, Object>> containers) {
    this.containers = containers;
  }


  public Integer getNumContainers() {
    return numContainers;
  }

  public void setNumContainers(Integer numContainers) {
    this.numContainers = numContainers;
  }

  public Integer getExpectedContainers() {
    return expectedContainers;
  }

  public void setExpectedContainers(Integer expectedContainers) {
    this.expectedContainers = expectedContainers;
  }

  public String getLifetime() {
    return lifetime;
  }

  public void setLifetime(String lifetime) {
    this.lifetime = lifetime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ServiceGroupResponse that = (ServiceGroupResponse) o;

    if (clusterId != null ?
        !clusterId.equals(that.clusterId) : that.clusterId != null) {
      return false;
    }
    if (clusterName != null ?
        !clusterName.equals(that.clusterName) : that.clusterName != null) {
      return false;
    }
    if (serviceGroupName != null ?
        !serviceGroupName.equals(that.serviceGroupName) : that.serviceGroupName != null) {
      return false;
    }

    return true;
  }

}
