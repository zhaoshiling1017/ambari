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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.SizeOfPolicyConfiguration;
import net.sf.ehcache.config.SizeOfPolicyConfiguration.MaxDepthExceededBehavior;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import org.apache.ambari.server.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.sf.ehcache.config.PersistenceConfiguration.Strategy;

@Singleton
public class ServiceGroupCacheProvider {
  private ServiceGroupCache serviceGroupCache;
  private volatile boolean isCacheInitialized = false;
  public static final String SERVICE_GROUP_CACHE_MANAGER_NAME = "ServiceGroupCacheManager";
  public static final String SERVICE_GROUP_CACHE_INSTANCE_NAME = "ServiceGroupCache";

  Configuration configuration;
  ServiceGroupCacheEntryFactory cacheEntryFactory;
  private final static Logger LOG = LoggerFactory.getLogger(ServiceGroupCacheProvider.class);

  @Inject
  public ServiceGroupCacheProvider(Configuration configuration,
                                     ServiceGroupCacheEntryFactory cacheEntryFactory) {
    this.configuration = configuration;
    this.cacheEntryFactory = cacheEntryFactory;
  }

  private synchronized void initializeCache() {
    // Check in case of contention to avoid ObjectExistsException
    if (isCacheInitialized) {
      return;
    }

    System.setProperty("net.sf.ehcache.skipUpdateCheck", "true");

    net.sf.ehcache.config.Configuration managerConfig =
      new net.sf.ehcache.config.Configuration();
    managerConfig.setName(SERVICE_GROUP_CACHE_MANAGER_NAME);

    // Set max heap available to the cache manager
    managerConfig.setMaxBytesLocalHeap("10%");

    //Create a singleton CacheManager using defaults
    CacheManager manager = CacheManager.create(managerConfig);

    // TODO: Add service group cache timeout configs
    LOG.info("Creating service group cache with timeouts => ttl = " +
      configuration.getMetricCacheTTLSeconds() + ", idle = " +
      configuration.getMetricCacheIdleSeconds());

    // Create a Cache specifying its configuration.
    CacheConfiguration cacheConfiguration = createCacheConfiguration();
    Cache cache = new Cache(cacheConfiguration);

    // Decorate with UpdatingSelfPopulatingCache
    serviceGroupCache = new ServiceGroupCache(cache, cacheEntryFactory);

    LOG.info("Registering service group cache with provider: name = " +
      cache.getName() + ", guid: " + cache.getGuid());
    manager.addCache(serviceGroupCache);
    isCacheInitialized = true;
  }

  // Having this as a separate public method for testing/mocking purposes
  public CacheConfiguration createCacheConfiguration() {

    CacheConfiguration cacheConfiguration = new CacheConfiguration()
      .name(SERVICE_GROUP_CACHE_INSTANCE_NAME)
      .timeToLiveSeconds(configuration.getMetricCacheTTLSeconds()) // 1 hour
      .timeToIdleSeconds(configuration.getMetricCacheIdleSeconds()) // 5 minutes
      .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU)
      .sizeOfPolicy(new SizeOfPolicyConfiguration() // Set sizeOf policy to continue on max depth reached - avoid OOM
        .maxDepth(10000)
        .maxDepthExceededBehavior(MaxDepthExceededBehavior.CONTINUE))
      .eternal(false)
      .persistence(new PersistenceConfiguration()
        .strategy(Strategy.NONE.name()));

    return cacheConfiguration;
  }

  public ServiceGroupCache getServiceGroupCache() {
    if (!isCacheInitialized) {
      initializeCache();
    }
    return serviceGroupCache;
  }

}
