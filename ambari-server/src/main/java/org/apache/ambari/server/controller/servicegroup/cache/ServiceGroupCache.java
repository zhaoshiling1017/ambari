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

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.LockTimeoutException;
import net.sf.ehcache.constructs.blocking.UpdatingCacheEntryFactory;
import net.sf.ehcache.constructs.blocking.UpdatingSelfPopulatingCache;
import net.sf.ehcache.statistics.StatisticsGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;

public class ServiceGroupCache extends UpdatingSelfPopulatingCache {

  private final static Logger LOG = LoggerFactory.getLogger(ServiceGroupCache.class);
  private static AtomicInteger printCacheStatsCounter = new AtomicInteger(0);

  /**
   * Creates a SelfPopulatingCache.
   *
   * @param cache @Cache
   * @param factory @CacheEntryFactory
   */
  public ServiceGroupCache(Ehcache cache, UpdatingCacheEntryFactory factory) throws CacheException {
    super(cache, factory);
  }



  public ServiceGroupCacheValue getServiceGroupCacheValue(ServiceGroupCacheKey key)
      throws IllegalArgumentException, IOException {

    LOG.debug("Fetching service group cache value with key: " + key);
    // Make sure key is valid
    validateKey(key);

    Element element = null;
    try {
      element = get(key);
    } catch (LockTimeoutException le) {
      // Ehcache masks the Socket Timeout to look as a LockTimeout
      Throwable t = le.getCause();
      if (t instanceof CacheException) {
        t = t.getCause();
        if (t instanceof SocketTimeoutException) {
          throw new SocketTimeoutException(t.getMessage());
        }
      }
    }

    ServiceGroupCacheValue cacheValue = null;
    if (element != null && element.getObjectValue() != null) {
      cacheValue = (ServiceGroupCacheValue) element.getObjectValue();
      LOG.debug("Returning service group value from cache: " + cacheValue);
    }
    return cacheValue;
  }

  public Map<String, Object> getResponseFromCache(ServiceGroupCacheKey key) throws IllegalArgumentException, IOException {
    LOG.debug("Fetching service group with key: " + key);

    // Make sure key is valid
    validateKey(key);

    Element element = null;
    try {
      element = get(key);
    } catch (LockTimeoutException le) {
      // Ehcache masks the Socket Timeout to look as a LockTimeout
      Throwable t = le.getCause();
      if (t instanceof CacheException) {
        t = t.getCause();
        if (t instanceof SocketTimeoutException) {
          throw new SocketTimeoutException(t.getMessage());
        }
      }
    }

    Map<String, Object> response = null;
    if (element != null && element.getObjectValue() != null) {
      ServiceGroupCacheValue value = (ServiceGroupCacheValue) element.getObjectValue();
      LOG.debug("Returning service group value from cache: " + value);
      response = value.getResponse();
    }
    return response;
  }


  private void validateKey(ServiceGroupCacheKey key) throws IllegalArgumentException {
    StringBuilder msg = new StringBuilder("Invalid service group key requested.");
    boolean throwException = false;

    if (key.getServiceGroupName() == null) {
      msg.append(" No service group name provided.");
      throwException = true;
    }

    if (throwException) {
      throw new IllegalArgumentException(msg.toString());
    }
  }
}
