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

public class ServiceGroupCacheKey {
  private String serviceGroupName;

  public ServiceGroupCacheKey(String serviceGroupName) {
    this.serviceGroupName = serviceGroupName;
  }

  public String getServiceGroupName() {
    return serviceGroupName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ServiceGroupCacheKey that = (ServiceGroupCacheKey) o;

    if (!serviceGroupName.equals(that.serviceGroupName))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = serviceGroupName.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "ServiceGroupCacheKey { " +
      "serviceGroupName=" + serviceGroupName +
      " }";
  }
}
