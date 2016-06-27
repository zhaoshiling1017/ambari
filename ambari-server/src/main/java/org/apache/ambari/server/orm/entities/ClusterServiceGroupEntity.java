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

package org.apache.ambari.server.orm.entities;

import org.apache.ambari.server.state.State;

import javax.persistence.*;
import java.util.Collection;

@javax.persistence.IdClass(ClusterServiceGroupEntityPK.class)
@javax.persistence.Table(name = "clusterservicegroups")
@NamedQueries({
    @NamedQuery(name = "clusterServiceGroupByClusterAndServiceGroupNames", query =
        "SELECT clusterServiceGroup " +
            "FROM ClusterServiceGroupEntity clusterServiceGroup " +
            "JOIN clusterServiceGroup.clusterEntity cluster " +
            "WHERE clusterServiceGroup.serviceGroupName=:serviceGroupName AND cluster.clusterName=:clusterName")
})
@Entity
public class ClusterServiceGroupEntity {

  @Id
  @Column(name = "cluster_id", nullable = false, insertable = false, updatable = false, length = 10)
  private Long clusterId;

  @Id
  @Column(name = "service_group_name", nullable = false, insertable = true, updatable = true)
  private String serviceGroupName;

  @Column(name = "service_group_display_name", nullable = false, insertable = true, updatable = true)
  private String serviceGroupDisplayName;

  @ManyToOne
  @JoinColumn(name = "cluster_id", referencedColumnName = "cluster_id", nullable = false)
  private ClusterEntity clusterEntity;

  @Column(name = "service_group_type", nullable = false, insertable = true, updatable = true)
  private String serviceGroupType;

  @Lob
  @Column(name = "assembly_file", nullable = true, insertable = true, updatable = true)
  private String assemblyFile;

  @Column(name = "current_state", nullable = false, insertable = true, updatable = true)
  @Enumerated(value = EnumType.STRING)
  private State currentState = State.INIT;

  @Column(name = "desired_state", nullable = false, insertable = true, updatable = true)
  @Enumerated(value = EnumType.STRING)
  private State desiredState = State.INIT;

  public Long getClusterId() {
    return clusterId;
  }

  public void setClusterId(Long clusterId) {
    this.clusterId = clusterId;
  }


  public String getServiceGroupName() {
    return serviceGroupName;
  }

  public void setServiceGroupName(String serviceGroupName) {
    this.serviceGroupName = serviceGroupName;
  }

  public String getServiceGroupDisplayName() {
    return serviceGroupDisplayName;
  }

  public void setServiceGroupDisplayName(String serviceGroupDisplayName) {
    this.serviceGroupDisplayName = serviceGroupDisplayName;
  }

  public String getServiceGroupType() {
    return serviceGroupType;
  }

  public void setServiceGroupType(String serviceGroupType) {
    this.serviceGroupType = serviceGroupType;
  }

  public String getAssemblyFile() {
    return assemblyFile;
  }

  public void setAssemblyFile(String assemblyFile) {
    this.assemblyFile = assemblyFile;
  }

  public State getDesiredState() {
    return desiredState;
  }

  public void setDesiredState(State desiredState) {
    this.desiredState = desiredState;
  }

  public State getCurrentState() {
    return currentState;
  }

  public void setCurrentState(State currentState) {
    this.currentState = currentState;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ClusterServiceGroupEntity that = (ClusterServiceGroupEntity) o;

    if (clusterId != null ? !clusterId.equals(that.clusterId) : that.clusterId != null) return false;
    if (serviceGroupName != null ? !serviceGroupName.equals(that.serviceGroupName) : that.serviceGroupName != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = clusterId !=null ? clusterId.intValue() : 0;
    result = 31 * result + (serviceGroupName != null ? serviceGroupName.hashCode() : 0);
    return result;
  }

  public ClusterEntity getClusterEntity() {
    return clusterEntity;
  }

  public void setClusterEntity(ClusterEntity clusterEntity) {
    this.clusterEntity = clusterEntity;
  }

}
