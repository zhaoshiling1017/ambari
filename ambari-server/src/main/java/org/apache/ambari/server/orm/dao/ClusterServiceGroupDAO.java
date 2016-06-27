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

package org.apache.ambari.server.orm.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.ambari.server.orm.RequiresSession;
import org.apache.ambari.server.orm.entities.ClusterServiceGroupEntity;
import org.apache.ambari.server.orm.entities.ClusterServiceGroupEntityPK;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

@Singleton
public class ClusterServiceGroupDAO {
  @Inject
  Provider<EntityManager> entityManagerProvider;
  @Inject
  DaoUtils daoUtils;

  @RequiresSession
  public ClusterServiceGroupEntity findByPK(ClusterServiceGroupEntityPK clusterServiceGroupEntityPK) {
    return entityManagerProvider.get().find(ClusterServiceGroupEntity.class, clusterServiceGroupEntityPK);
  }

  @RequiresSession
  public ClusterServiceGroupEntity findByClusterAndServiceGroupNames(String  clusterName, String serviceGroupName) {
    TypedQuery<ClusterServiceGroupEntity> query = entityManagerProvider.get()
        .createNamedQuery("clusterServiceGroupByClusterAndServiceGroupNames", ClusterServiceGroupEntity.class);
    query.setParameter("clusterName", clusterName);
    query.setParameter("serviceGroupName", serviceGroupName);

    try {
      return query.getSingleResult();
    } catch (NoResultException ignored) {
      return null;
    }
  }

  @RequiresSession
  public List<ClusterServiceGroupEntity> findAll() {
    return daoUtils.selectAll(entityManagerProvider.get(), ClusterServiceGroupEntity.class);
  }

  @Transactional
  public void refresh(ClusterServiceGroupEntity clusterServiceGroupEntity) {
    entityManagerProvider.get().refresh(clusterServiceGroupEntity);
  }

  @Transactional
  public void create(ClusterServiceGroupEntity clusterServiceGroupEntity) {
    entityManagerProvider.get().persist(clusterServiceGroupEntity);
  }

  @Transactional
  public ClusterServiceGroupEntity merge(ClusterServiceGroupEntity clusterServiceGroupEntity) {
    return entityManagerProvider.get().merge(clusterServiceGroupEntity);
  }

  @Transactional
  public void remove(ClusterServiceGroupEntity clusterServiceGroupEntity) {
    entityManagerProvider.get().remove(merge(clusterServiceGroupEntity));
  }

  @Transactional
  public void removeByPK(ClusterServiceGroupEntityPK clusterServiceGroupEntityPK) {
    ClusterServiceGroupEntity entity = findByPK(clusterServiceGroupEntityPK);
    entityManagerProvider.get().remove(entity);
  }

}
