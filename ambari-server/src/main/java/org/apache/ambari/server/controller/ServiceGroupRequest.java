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



public class ServiceGroupRequest {

  private String clusterName; // REF
  private String serviceGroupName; // GET/CREATE/UPDATE/DELETE
  private String serviceGroupDisplayName; // GET/CREATE/UPDATE/DELETE
  private String serviceGroupType; // GET/CREATE/UPDATE/DELETE
  private String assemblyFile; // GET/CREATE/UPDATE/DELETE
  private String desiredState;
  private String currentState;

  public ServiceGroupRequest(String clusterName, String serviceGroupName, String serviceGroupDisplayName,
      String serviceGroupType, String assemblyFile, String desiredState, String currentState) {
    this.clusterName = clusterName;
    this.serviceGroupName = serviceGroupName;
    this.serviceGroupDisplayName = serviceGroupDisplayName;
    this.serviceGroupType = serviceGroupType;
    this.assemblyFile = assemblyFile;
    this.desiredState = desiredState;
    this.currentState = currentState;
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
   * @return the serviceGroupName
   */
  public String getServiceGroupName() {
    return serviceGroupName;
  }

  /**
   * @param serviceGroupName the service group name to set
   */
  public void setServiceGroupName(String serviceGroupName) {
    this.serviceGroupName = serviceGroupName;
  }

  /**
   * @return the serviceGroupDisplayName
   */
  public String getServiceGroupDisplayName() {
    return serviceGroupDisplayName;
  }

  /**
   * @param serviceGroupDisplayName the service group display name to set
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
   * @param desiredState the desired state of service group
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
   * @param currentState the current state of service group
   */
  public void setCurrentState(String currentState) {
    this.currentState = currentState;
  }

  /**
   * @return the serviceGroupType
   */
  public String getServiceGroupType() {
    return serviceGroupType;
  }

  /**
   * @param serviceGroupType the service group type to set
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
   * @param assemblyFile the assembly file to set
   */
  public void setAssemblyFile(String assemblyFile) {
    this.assemblyFile = assemblyFile;
  }


  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("clusterName=" + clusterName
        + ", serviceGroupName=" + serviceGroupName
        + ", serviceGroupDisplayName=" + serviceGroupDisplayName
        + ", serviceGroupType=" + serviceGroupType
        + ", desiredState=" + desiredState
        + ", assemblyFile=" + assemblyFile);
    return sb.toString();
  }
}
