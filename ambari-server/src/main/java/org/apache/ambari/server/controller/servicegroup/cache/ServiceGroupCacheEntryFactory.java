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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.sf.ehcache.constructs.blocking.UpdatingCacheEntryFactory;
import org.apache.ambari.server.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.Map;
import org.apache.commons.io.IOUtils;

@Singleton
public class ServiceGroupCacheEntryFactory implements UpdatingCacheEntryFactory {
  private final static Logger LOG = LoggerFactory.getLogger(ServiceGroupCacheEntryFactory.class);
  private final String dashEndpoint;
  private final Gson gson;

  @Inject
  public ServiceGroupCacheEntryFactory(Configuration configuration) {
    gson = new Gson();
    dashEndpoint = configuration.getDashApiEndpoint();
  }

  @Override
  public Object createEntry(Object key) throws Exception {
    LOG.debug("Creating cache entry since none exists, key = " + key);
    ServiceGroupCacheKey serviceGroupCacheKey = (ServiceGroupCacheKey) key;

    Map<String, Object> response = null;
    String urlString = dashEndpoint + "/" +  serviceGroupCacheKey.getServiceGroupName().toLowerCase();
    try {
      URL url = new URL(urlString);
      HttpURLConnection httpRequest = (HttpURLConnection) url.openConnection();
      InputStream inputStream = httpRequest.getInputStream();
      String jsonResponse = IOUtils.toString(inputStream, "UTF-8");
      response = gson.fromJson(jsonResponse, new TypeToken<Map<String, Object>>() {
      }.getType());
    } catch (Exception e) {
      LOG.error("Failed to get response from DASH endpoint " + dashEndpoint + "Exception : " + e.getMessage());
      response = null;
    }
    ServiceGroupCacheValue value = null;
    if (response != null) {
      value = new ServiceGroupCacheValue(System.currentTimeMillis(), response);
      LOG.debug("Created cache entry: " + value);
    }
    return value;
  }

  @Override
  public void updateEntryValue(Object key, Object value) throws Exception {
    ServiceGroupCacheKey serviceGroupCacheKey = (ServiceGroupCacheKey) key;
    ServiceGroupCacheValue existingValue = (ServiceGroupCacheValue) value;
    long fetchTime = existingValue.getFetchTime();
    if(System.currentTimeMillis() - fetchTime > 60000) {
      LOG.debug("Updating service group cache entry for key = " + key);
      Map<String, Object> response = null;
      String urlString = dashEndpoint + "/" +  serviceGroupCacheKey.getServiceGroupName().toLowerCase();
      try {
        URL url = new URL(urlString);
        HttpURLConnection httpRequest = (HttpURLConnection) url.openConnection();
        InputStream inputStream = httpRequest.getInputStream();
        String jsonResponse = IOUtils.toString(inputStream, "UTF-8");
        response = gson.fromJson(jsonResponse, new TypeToken<Map<String, Object>>() {
        }.getType());
      } catch (Exception e) {
        LOG.error("Failed to get response from Dash endpoint " + dashEndpoint + "Exception : " + e.getMessage());
        response = null;
      }
      if (response != null) {
        existingValue.setFetchTime(System.currentTimeMillis());
        existingValue.setResponse(response);
        LOG.debug("Updated cache entry for key = " + key + ", newValue = " + value);
      }
    } else {
      LOG.debug("Skipping updating service group cache entry for key = " + key);
    }
  }
}
