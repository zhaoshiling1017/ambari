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

package org.apache.ambari.server.api.services;

import org.apache.ambari.server.api.resources.ResourceInstance;
import org.apache.ambari.server.api.util.ApiVersion;
import org.apache.ambari.server.controller.spi.Resource;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for service groups resource requests.
 */
public class ServiceGroupService extends BaseService {
  /**
   * Parent cluster name.
   */
  private String m_clusterName;

  /**
   * Constructor.
   *
   * @param clusterName cluster id
   */
  public ServiceGroupService(ApiVersion apiVersion, String clusterName) {
    super(apiVersion);
    m_clusterName = clusterName;
  }

  /**
   * Handles URL: /clusters/{clusterID}/servicegroups/{serviceGroupId}
   * Get a specific service group.
   *
   * @param headers     http headers
   * @param ui          uri info
   * @param serviceGroupName service group id
   * @return service group resource representation
   */
  @GET
  @Path("{serviceGroupName}")
  @Produces("text/plain")
  public Response getServiceGroup(String body, @Context HttpHeaders headers, @Context UriInfo ui,
                             @PathParam("serviceGroupName") String serviceGroupName) {

    return handleRequest(headers, body, ui, Request.Type.GET,
        createServiceGroupResource(m_clusterName, serviceGroupName));
  }

  /**
   * Handles URL: /clusters/{clusterId}/servicegroups
   * Get all service groups for a cluster.
   *
   * @param headers http headers
   * @param ui      uri info
   * @return service collection resource representation
   */
  @GET
  @Produces("text/plain")
  public Response getServiceGroups(String body, @Context HttpHeaders headers, @Context UriInfo ui) {
    return handleRequest(headers, body, ui, Request.Type.GET,
        createServiceGroupResource(m_clusterName, null));
  }

  /**
   * Handles: POST /clusters/{clusterId}/servicegroups/{serviceGroupId}
   * Create a specific service group.
   *
   * @param body        http body
   * @param headers     http headers
   * @param ui          uri info
   * @param serviceGroupName service group id
   * @return information regarding the created service
   */
  @POST
  @Path("{serviceGroupName}")
  @Produces("text/plain")
  public Response createService(String body, @Context HttpHeaders headers, @Context UriInfo ui,
                                @PathParam("serviceGroupName") String serviceGroupName) {

    return handleRequest(headers, body, ui, Request.Type.POST,
        createServiceGroupResource(m_clusterName, serviceGroupName));
  }

  /**
   * Handles: POST /clusters/{clusterId}/servicegroups
   * Create multiple service groups.
   *
   * @param body        http body
   * @param headers     http headers
   * @param ui          uri info
   * @return information regarding the created service groups
   */
  @POST
  @Produces("text/plain")
  public Response createServiceGroups(String body, @Context HttpHeaders headers, @Context UriInfo ui) {

    return handleRequest(headers, body, ui, Request.Type.POST,
        createServiceGroupResource(m_clusterName, null));
  }

  /**
   * Handles: PUT /clusters/{clusterId}/servicegroups/{serviceGroupId}
   * Update a specific service group.
   *
   * @param body        http body
   * @param headers     http headers
   * @param ui          uri info
   * @param serviceGroupName service group id
   * @return information regarding the updated service group
   */
  @PUT
  @Path("{serviceGroupName}")
  @Produces("text/plain")
  public Response updateServiceGroup(String body, @Context HttpHeaders headers, @Context UriInfo ui,
                                @PathParam("serviceGroupName") String serviceGroupName) {

    return handleRequest(headers, body, ui, Request.Type.PUT, createServiceGroupResource(m_clusterName, serviceGroupName));
  }

  /**
   * Handles: PUT /clusters/{clusterId}/servicegroups
   * Update multiple service groups.
   *
   * @param body        http body
   * @param headers     http headers
   * @param ui          uri info
   * @return information regarding the updated service groups
   */
  @PUT
  @Produces("text/plain")
  public Response updateServiceGroups(String body, @Context HttpHeaders headers, @Context UriInfo ui) {

    return handleRequest(headers, body, ui, Request.Type.PUT, createServiceGroupResource(m_clusterName, null));
  }

  /**
   * Handles: DELETE /clusters/{clusterId}/servicegroups/{serviceGroupId}
   * Delete a specific service group.
   *
   * @param headers     http headers
   * @param ui          uri info
   * @param serviceGroupName service group id
   * @return information regarding the deleted service group
   */
  @DELETE
  @Path("{serviceGroupName}")
  @Produces("text/plain")
  public Response deleteService(@Context HttpHeaders headers, @Context UriInfo ui,
                                @PathParam("serviceGroupName") String serviceGroupName) {

    return handleRequest(headers, null, ui, Request.Type.DELETE, createServiceGroupResource(m_clusterName, serviceGroupName));
  }


  /**
   * Create a service resource instance.
   *
   * @param clusterName  cluster name
   * @param serviceGroupName  service group name
   *
   * @return a service resource instance
   */
  ResourceInstance createServiceGroupResource(String clusterName, String serviceGroupName) {
    Map<Resource.Type,String> mapIds = new HashMap<>();
    mapIds.put(Resource.Type.Cluster, clusterName);
    mapIds.put(Resource.Type.ServiceGroup, serviceGroupName);

    return createResource(Resource.Type.ServiceGroup, mapIds);
  }
}
