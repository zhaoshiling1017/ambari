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

package org.apache.ambari.server.state;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.persist.Transactional;
import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.api.services.AmbariMetaInfo;
import org.apache.ambari.server.configuration.Configuration;
import org.apache.ambari.server.controller.ServiceGroupResponse;
import org.apache.ambari.server.events.MaintenanceModeEvent;
import org.apache.ambari.server.events.ServiceGroupInstalledEvent;
import org.apache.ambari.server.events.ServiceGroupRemovedEvent;
import org.apache.ambari.server.events.publishers.AmbariEventPublisher;
import org.apache.ambari.server.orm.dao.ClusterDAO;
import org.apache.ambari.server.orm.dao.ClusterServiceGroupDAO;
import org.apache.ambari.server.orm.entities.ClusterConfigEntity;
import org.apache.ambari.server.orm.entities.ClusterConfigMappingEntity;
import org.apache.ambari.server.orm.entities.ClusterEntity;
import org.apache.ambari.server.orm.entities.ClusterServiceGroupEntity;
import org.apache.ambari.server.orm.entities.ClusterServiceGroupEntityPK;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ServiceGroupImpl implements ServiceGroup {
  private final ReadWriteLock clusterGlobalLock;
  private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
  // Cached entity has only 1 getter for name
  private ClusterServiceGroupEntity serviceGroupEntity;
  private ClusterServiceGroupEntityPK serviceGroupEntityPK;

  private static final Logger LOG = LoggerFactory.getLogger(ServiceImpl.class);

  private volatile boolean persisted = false;
  private final Cluster cluster;

  @Inject
  private ClusterServiceGroupDAO clusterServiceGroupDAO;
  @Inject
  private ClusterDAO clusterDAO;
  @Inject
  private AmbariMetaInfo ambariMetaInfo;

  /**
   * Used to publish events relating to service group CRUD operations.
   */
  @Inject
  private AmbariEventPublisher eventPublisher;

  private final Gson gson;
  private Map<String, Object> responseMap = null;
  private long lastFetchTimestamp = 0;

  private void init() {
    // TODO load from DB during restart?
  }

  @AssistedInject
  public ServiceGroupImpl(@Assisted Cluster cluster,
      @Assisted("serviceGroupName") String serviceGroupName,
      @Assisted("serviceGroupDisplayName") String serviceGroupDisplayName,
      @Assisted("serviceGroupType") String serviceGroupType,
      @Assisted("assemblyFile") String assemblyFile,
      @Assisted("desiredState") State desiredState,
      @Assisted("currentState") State currentState,
      Injector injector) throws AmbariException {
    injector.injectMembers(this);
    gson = injector.getInstance(Gson.class);

    clusterGlobalLock = cluster.getClusterGlobalLock();
    serviceGroupEntity = new ClusterServiceGroupEntity();
    serviceGroupEntity.setClusterId(cluster.getClusterId());
    serviceGroupEntity.setServiceGroupName(serviceGroupName);
    serviceGroupEntity.setServiceGroupDisplayName(serviceGroupDisplayName);
    serviceGroupEntity.setServiceGroupType(serviceGroupType);
    serviceGroupEntity.setAssemblyFile(assemblyFile);
    serviceGroupEntity.setDesiredState(desiredState);
    serviceGroupEntity.setCurrentState(currentState);
    this.cluster = cluster;
    init();
  }

  private void populateResponseMap() {
    Map<String, String> configs = ambariMetaInfo.getAmbariServerProperties();
    String dashEndpoint = configs.containsKey(Configuration.YARN_DASH_API_ENDPOINT)?
        configs.get(Configuration.YARN_DASH_API_ENDPOINT) : Configuration.YARN_DASH_API_ENDPOINT_DEFAULT;
    String urlString = dashEndpoint + "/" +  getName().toLowerCase();
    try {
      URL url = new URL(urlString);
      HttpURLConnection httpRequest = (HttpURLConnection) url.openConnection();
      InputStream inputStream = httpRequest.getInputStream();
      String jsonResponse = IOUtils.toString(inputStream, "UTF-8");
      responseMap = gson.fromJson(jsonResponse, new TypeToken<Map<String, Object>>() {
      }.getType());
      lastFetchTimestamp = System.currentTimeMillis();
    } catch (Exception e) {
      LOG.error("Failed to get response from DASH endpoint " + dashEndpoint);
      responseMap = null;
    }
  }

  @AssistedInject
  public ServiceGroupImpl(@Assisted Cluster cluster, @Assisted ClusterServiceGroupEntity
      serviceGroupEntity, Injector injector) throws AmbariException {
    injector.injectMembers(this);
    gson = injector.getInstance(Gson.class);
    clusterGlobalLock = cluster.getClusterGlobalLock();
    this.serviceGroupEntity = serviceGroupEntity;
    this.cluster = cluster;
    persisted = true;
  }

  @Override
  public ReadWriteLock getClusterGlobalLock() {
    return clusterGlobalLock;
  }

  @Override
  public String getName() {
    return serviceGroupEntity.getServiceGroupName();
  }

  @Override
  public String getServiceGroupDisplayName() {
    return serviceGroupEntity.getServiceGroupDisplayName();
  }

  @Override
  public void setServiceGroupDisplayName(String displayName) {
    serviceGroupEntity.setServiceGroupDisplayName(displayName);
  }

  @Override
  public String getServiceGroupType() {
    return serviceGroupEntity.getServiceGroupType();
  }

  @Override
  public String getAssemblyFile() {
    return serviceGroupEntity.getAssemblyFile();
  }

  @Override
  public void setAssemblyFile(String assemblyFile) {
    serviceGroupEntity.setAssemblyFile(assemblyFile);
  }

  @Override
  public long getClusterId() {
    return cluster.getClusterId();
  }

  @Override
  public State getDesiredState() {
    return serviceGroupEntity.getDesiredState();
  }

  @Override
  public void setDesiredState(State state) {
    serviceGroupEntity.setDesiredState(state);
  }

  @Override
  public State getCurrentState() {
    return serviceGroupEntity.getCurrentState();
  }

  @Override
  public void setCurrentState(State state) {
    serviceGroupEntity.setCurrentState(state);
  }

  @Override
  public ServiceGroupResponse convertToResponse() {
    readWriteLock.readLock().lock();
    try {
      ServiceGroupResponse r = new ServiceGroupResponse(cluster.getClusterId(),
          cluster.getClusterName(), getName(), getServiceGroupDisplayName(),
          getServiceGroupType(), getAssemblyFile(), getDesiredState().toString(), getCurrentState().toString());
      return r;
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public Cluster getCluster() {
    return cluster;
  }

  @Override
  public void debugDump(StringBuilder sb) {
    readWriteLock.readLock().lock();
    try {
      sb.append("ServiceGroup={ serviceGroupName=" + getName()  + ", clusterName="
          + cluster.getClusterName() + ", clusterId=" + cluster.getClusterId() + "}");
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isPersisted() {
    // a lock around this internal state variable is not required since we
    // have appropriate locks in the persist() method and this member is
    // only ever false under the condition that the object is new
    return persisted;
  }

  /**
   * {@inheritDoc}
   * <p/>
   * This method uses Java locks and then delegates to internal methods which
   * perform the JPA merges inside of a transaction. Because of this, a
   * transaction is not necessary before this calling this method.
   */
  @Override
  public void persist() {
    clusterGlobalLock.writeLock().lock();
    try {
      readWriteLock.writeLock().lock();
      try {
        if (!persisted) {
          persistEntities();
          refresh();
          // There refresh calls are no longer needed with cached references
          // not used on getters/setters
          // cluster.refresh();
          persisted = true;

          // publish the service installed event
          StackId stackId = cluster.getDesiredStackVersion();
          cluster.addServiceGroup(this);

          ServiceGroupInstalledEvent event = new ServiceGroupInstalledEvent(
              getClusterId(), getName());
          eventPublisher.publish(event);
        } else {
          saveIfPersisted();
        }
      } finally {
        readWriteLock.writeLock().unlock();
      }
    } finally {
      clusterGlobalLock.writeLock().unlock();
    }
  }

  @Transactional
  protected void persistEntities() {
    long clusterId = cluster.getClusterId();

    ClusterEntity clusterEntity = clusterDAO.findById(clusterId);
    serviceGroupEntity.setClusterEntity(clusterEntity);
    clusterServiceGroupDAO.create(serviceGroupEntity);
    clusterEntity.getClusterServiceGroupEntities().add(serviceGroupEntity);
    clusterDAO.merge(clusterEntity);
    clusterServiceGroupDAO.merge(serviceGroupEntity);
  }

  @Transactional
  void saveIfPersisted() {
    if (isPersisted()) {
      clusterServiceGroupDAO.merge(serviceGroupEntity);
    }
  }

  @Override
  @Transactional
  public void refresh() {
    readWriteLock.writeLock().lock();
    try {
      if (isPersisted()) {
        ClusterServiceGroupEntityPK pk = new ClusterServiceGroupEntityPK();
        pk.setClusterId(getClusterId());
        pk.setServiceGroupName(getName());
        serviceGroupEntity = clusterServiceGroupDAO.findByPK(pk);
        clusterServiceGroupDAO.refresh(serviceGroupEntity);
      }
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  @Override
  public boolean canBeRemoved() {
    clusterGlobalLock.readLock().lock();
    try {
      readWriteLock.readLock().lock();
      try {
        // TODO: Add check for services in the service group
        return true;
      } finally {
        readWriteLock.readLock().unlock();
      }
    } finally {
      clusterGlobalLock.readLock().unlock();
    }
  }


  @Override
  @Transactional
  public void delete() throws AmbariException {
    clusterGlobalLock.writeLock().lock();
    try {
      readWriteLock.writeLock().lock();
      try {

        if (persisted) {
          removeEntities();
          persisted = false;

          // publish the service removed event
          ServiceGroupRemovedEvent event = new ServiceGroupRemovedEvent(getClusterId(), getName());

          eventPublisher.publish(event);
        }
      } finally {
        readWriteLock.writeLock().unlock();
      }
    } finally {
      clusterGlobalLock.writeLock().unlock();
    }


  }

  @Transactional
  protected void removeEntities() throws AmbariException {

    ClusterServiceGroupEntityPK pk = new ClusterServiceGroupEntityPK();
    pk.setClusterId(getClusterId());
    pk.setServiceGroupName(getName());
    clusterServiceGroupDAO.removeByPK(pk);
  }
}
