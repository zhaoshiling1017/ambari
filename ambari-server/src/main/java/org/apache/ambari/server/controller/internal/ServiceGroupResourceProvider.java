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
package org.apache.ambari.server.controller.internal;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import jline.internal.Log;
import org.apache.ambari.server.*;
import org.apache.ambari.server.api.services.*;
import org.apache.ambari.server.controller.*;
import org.apache.ambari.server.controller.servicegroup.cache.ServiceGroupCacheKey;
import org.apache.ambari.server.controller.servicegroup.cache.ServiceGroupCacheProvider;
import org.apache.ambari.server.controller.spi.*;
import org.apache.ambari.server.controller.spi.Request;
import org.apache.ambari.server.controller.utilities.PropertyHelper;
import org.apache.ambari.server.controller.servicegroup.cache.ServiceGroupCache;
import org.apache.ambari.server.controller.servicegroup.cache.ServiceGroupCacheValue;
import org.apache.ambari.server.controller.servicegroup.cache.ServiceGroupCacheProvider;
import org.apache.ambari.server.security.authorization.AuthorizationException;
import org.apache.ambari.server.security.authorization.AuthorizationHelper;
import org.apache.ambari.server.security.authorization.ResourceType;
import org.apache.ambari.server.security.authorization.RoleAuthorization;
import org.apache.ambari.server.serveraction.kerberos.KerberosAdminAuthenticationException;
import org.apache.ambari.server.serveraction.kerberos.KerberosInvalidConfigurationException;
import org.apache.ambari.server.serveraction.kerberos.KerberosMissingAdminCredentialsException;
import org.apache.ambari.server.state.*;
import org.apache.ambari.server.utils.MapUtils;
import org.apache.ambari.server.utils.StageUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.net.URLEncoder;
import java.io.OutputStream;

/**
 * Resource provider for service resources.
 */
public class ServiceGroupResourceProvider extends AbstractControllerResourceProvider {


  // ----- Property ID constants ---------------------------------------------

  // Services
  public static final String SERVICE_GROUP_CLUSTER_NAME_PROPERTY_ID    = PropertyHelper.getPropertyId("ServiceGroupInfo", "cluster_name");
  public static final String SERVICE_GROUP_SERVICE_GROUP_NAME_PROPERTY_ID    = PropertyHelper.getPropertyId("ServiceGroupInfo", "service_group_name");
  public static final String SERVICE_GROUP_SERVICE_GROUP_DISPLAY_NAME_PROPERTY_ID    = PropertyHelper.getPropertyId("ServiceGroupInfo", "service_group_display_name");
  public static final String SERVICE_GROUP_SERVICE_GROUP_TYPE_PROPERTY_ID    = PropertyHelper.getPropertyId("ServiceGroupInfo", "service_group_type");
  public static final String SERVICE_GROUP_ASSEMBLY_FILE_PROPERTY_ID    = PropertyHelper.getPropertyId("ServiceGroupInfo", "assembly_file");
  public static final String SERVICE_GROUP_DESIRED_STATE_PROPERTY_ID = PropertyHelper.getPropertyId("ServiceGroupInfo", "desired_state");
  public static final String SERVICE_GROUP_CURRENT_STATE_PROPERTY_ID = PropertyHelper.getPropertyId("ServiceGroupInfo", "current_state");
  public static final String SERVICE_GROUP_APPLICATION_ID_PROPERTY_ID = PropertyHelper.getPropertyId("ServiceGroupInfo", "application_id");
  public static final String SERVICE_GROUP_LIFETIME_PROPERTY_ID = PropertyHelper.getPropertyId("ServiceGroupInfo", "lifetime");
  public static final String SERVICE_GROUP_QUICKLINKS_PROPERTY_ID = PropertyHelper.getPropertyId("ServiceGroupInfo", "quicklinks");
  public static final String SERVICE_GROUP_CONTAINERS_PROPERTY_ID = PropertyHelper.getPropertyId("ServiceGroupInfo", "containers");
  public static final String SERVICE_GROUP_NUMBER_CONTAINERS_PROPERTY_ID = PropertyHelper.getPropertyId("ServiceGroupInfo", "number_of_containers");
  public static final String SERVICE_GROUP_EXPECTED_CONTAINERS_PROPERTY_ID = PropertyHelper.getPropertyId("ServiceGroupInfo", "expected_number_of_containers");


  private static Set<String> pkPropertyIds =
      new HashSet<String>(Arrays.asList(new String[]{
          SERVICE_GROUP_CLUSTER_NAME_PROPERTY_ID,
          SERVICE_GROUP_SERVICE_GROUP_NAME_PROPERTY_ID}));

  private static Map<String, String> componentNameMappings = MapUtils.fillMap("/var/lib/ambari-server/resources/componentsMap.dat");

  private static Gson gson = StageUtils.getGson();

  /**
   * kerberos helper
   */
  @Inject
  private KerberosHelper kerberosHelper;

  private ServiceGroupCacheProvider cacheProvider;
  private ServiceGroupCache cache;

  // ----- Constructors ----------------------------------------------------

  /**
   * Create a  new resource provider for the given management controller.
   *
   * @param propertyIds           the property ids
   * @param keyPropertyIds        the key property ids
   * @param managementController  the management controller
   */
  @AssistedInject
  public ServiceGroupResourceProvider(@Assisted Set<String> propertyIds,
                                      @Assisted Map<Resource.Type, String> keyPropertyIds,
                                      @Assisted AmbariManagementController managementController) {
    super(propertyIds, keyPropertyIds, managementController);

    setRequiredCreateAuthorizations(EnumSet.of(RoleAuthorization.SERVICE_ADD_DELETE_SERVICES));
    setRequiredUpdateAuthorizations(RoleAuthorization.AUTHORIZATIONS_UPDATE_SERVICE);
    setRequiredGetAuthorizations(RoleAuthorization.AUTHORIZATIONS_VIEW_SERVICE);
    setRequiredDeleteAuthorizations(EnumSet.of(RoleAuthorization.SERVICE_ADD_DELETE_SERVICES));

    this.cacheProvider = managementController.getServiceGroupCacheProvider();
    this.cache = cacheProvider.getServiceGroupCache();
  }

  // ----- ResourceProvider ------------------------------------------------

  @Override
  protected RequestStatus createResourcesAuthorized(Request request)
      throws SystemException,
             UnsupportedPropertyException,
             ResourceAlreadyExistsException,
             NoSuchParentResourceException {

    final Set<ServiceGroupRequest> requests = new HashSet<>();
    for (Map<String, Object> propertyMap : request.getProperties()) {
      requests.add(getRequest(propertyMap));
    }
    createResources(new Command<Void>() {
      @Override
      public Void invoke() throws AmbariException, AuthorizationException {
        createServiceGroups(requests);
        return null;
      }
    });
    notifyCreate(Resource.Type.ServiceGroup, request);

    return getRequestStatus(null);
  }

  @Override
  protected Set<Resource> getResourcesAuthorized(Request request, Predicate predicate) throws
      SystemException, UnsupportedPropertyException, NoSuchResourceException, NoSuchParentResourceException {

    final Set<ServiceGroupRequest> requests = new HashSet<>();

    for (Map<String, Object> propertyMap : getPropertyMaps(predicate)) {
      requests.add(getRequest(propertyMap));
    }

    Set<ServiceGroupResponse> responses = getResources(new Command<Set<ServiceGroupResponse>>() {
      @Override
      public Set<ServiceGroupResponse> invoke() throws AmbariException {
        return getServiceGroups(requests);
      }
    });

    Set<String>   requestedIds = getRequestPropertyIds(request, predicate);
    Set<Resource> resources    = new HashSet<Resource>();

    for (ServiceGroupResponse response : responses) {
      Resource resource = new ResourceImpl(Resource.Type.ServiceGroup);
      setResourceProperty(resource, SERVICE_GROUP_CLUSTER_NAME_PROPERTY_ID,
          response.getClusterName(), requestedIds);
      setResourceProperty(resource, SERVICE_GROUP_SERVICE_GROUP_NAME_PROPERTY_ID,
          response.getServiceGroupName(), requestedIds);
      setResourceProperty(resource, SERVICE_GROUP_SERVICE_GROUP_DISPLAY_NAME_PROPERTY_ID,
          response.getServiceGroupDisplayName(), requestedIds);
      setResourceProperty(resource, SERVICE_GROUP_SERVICE_GROUP_TYPE_PROPERTY_ID,
          response.getServiceGroupType(), requestedIds);
      setResourceProperty(resource, SERVICE_GROUP_ASSEMBLY_FILE_PROPERTY_ID,
          response.getAssemblyFile(), requestedIds);
      setResourceProperty(resource, SERVICE_GROUP_DESIRED_STATE_PROPERTY_ID,
          response.getDesiredState(), requestedIds);
      setResourceProperty(resource, SERVICE_GROUP_CURRENT_STATE_PROPERTY_ID,
          response.getDesiredState(), requestedIds);
      setResourceProperty(resource, SERVICE_GROUP_APPLICATION_ID_PROPERTY_ID,
          response.getApplicationId(), requestedIds);
      setResourceProperty(resource, SERVICE_GROUP_LIFETIME_PROPERTY_ID,
          response.getLifetime(), requestedIds);
      setResourceProperty(resource, SERVICE_GROUP_QUICKLINKS_PROPERTY_ID,
          response.getQuickLinks(), requestedIds);
      setResourceProperty(resource, SERVICE_GROUP_CONTAINERS_PROPERTY_ID,
          response.getContainers(), requestedIds);
      setResourceProperty(resource, SERVICE_GROUP_NUMBER_CONTAINERS_PROPERTY_ID,
          response.getNumContainers(), requestedIds);
      setResourceProperty(resource, SERVICE_GROUP_EXPECTED_CONTAINERS_PROPERTY_ID,
          response.getExpectedContainers(), requestedIds);
      resources.add(resource);
    }
    return resources;
  }

  @Override
  protected RequestStatus updateResourcesAuthorized(final Request request, Predicate predicate)
      throws SystemException, UnsupportedPropertyException, NoSuchResourceException, NoSuchParentResourceException {

    RequestStageContainer requestStages = doUpdateResources(null, request, predicate);

    RequestStatusResponse response = null;
    if (requestStages != null) {
      try {
        requestStages.persist();
      } catch (AmbariException e) {
        throw new SystemException(e.getMessage(), e);
      }
      response = requestStages.getRequestStatusResponse();
    }
    notifyUpdate(Resource.Type.ServiceGroup, request, predicate);

    return getRequestStatus(response);
  }

  @Override
  protected RequestStatus deleteResourcesAuthorized(Request request, Predicate predicate)
      throws SystemException, UnsupportedPropertyException, NoSuchResourceException, NoSuchParentResourceException {

    final Set<ServiceGroupRequest> requests = new HashSet<>();
    for (Map<String, Object> propertyMap : getPropertyMaps(predicate)) {
      requests.add(getRequest(propertyMap));
    }
    RequestStatusResponse response = modifyResources(new Command<RequestStatusResponse>() {
      @Override
      public RequestStatusResponse invoke() throws AmbariException, AuthorizationException {
        return deleteServiceGroups(requests);
      }
    });

    notifyDelete(Resource.Type.ServiceGroup, predicate);
    return getRequestStatus(response);
  }

  @Override
  public Set<String> checkPropertyIds(Set<String> propertyIds) {
    propertyIds = super.checkPropertyIds(propertyIds);

    if (propertyIds.isEmpty()) {
      return propertyIds;
    }
    Set<String> unsupportedProperties = new HashSet<String>();
    return unsupportedProperties;
  }


  // ----- AbstractResourceProvider ----------------------------------------

  @Override
  protected Set<String> getPKPropertyIds() {
    return pkPropertyIds;
  }

  // ----- utility methods -------------------------------------------------

  private RequestStageContainer doUpdateResources(final RequestStageContainer stages, final Request request, Predicate predicate)
      throws UnsupportedPropertyException, SystemException, NoSuchResourceException, NoSuchParentResourceException {

    final Set<ServiceGroupRequest> requests = new HashSet<>();
    RequestStageContainer requestStages = null;

    Iterator<Map<String,Object>> iterator = request.getProperties().iterator();
    if (iterator.hasNext()) {
      for (Map<String, Object> propertyMap : getPropertyMaps(iterator.next(), predicate)) {
        requests.add(getRequest(propertyMap));
      }

      requestStages = modifyResources(new Command<RequestStageContainer>() {
        @Override
        public RequestStageContainer invoke() throws AmbariException, AuthorizationException {
          return updateServiceGroup(stages, requests, request.getRequestInfoProperties());
        }
      });
    }
    return requestStages;
  }

  // Update services based on the given requests.
  protected synchronized RequestStageContainer updateServiceGroup(RequestStageContainer requestStages,
      Set<ServiceGroupRequest> requests, Map<String, String> requestProperties) throws AmbariException, AuthorizationException {

    AmbariManagementController controller = getManagementController();
    if (requests.isEmpty()) {
      LOG.warn("Received an empty requests set");
      return null;
    }

    Map<String, Set<String>> serviceGroupNames = new HashMap<>();
    Map<State, List<ServiceGroup>> changedServiceGroups = new EnumMap<>(State.class);
    Set<State> seenNewStates = new HashSet<>();
    Clusters  clusters = controller.getClusters();
    Set<String> clusterNames = new HashSet<>();

    for (ServiceGroupRequest request : requests) {
      if (request.getClusterName() == null
          || request.getClusterName().isEmpty()
          || request.getServiceGroupName() == null
          || request.getServiceGroupName().isEmpty()) {
        throw new IllegalArgumentException("Invalid arguments, cluster name"
            + " and service group name should be provided to update service groups");
      }

      LOG.info("Received a updateServiceGroup request"
          + ", clusterName=" + request.getClusterName()
          + ", serviceGroupName=" + request.getServiceGroupName()
          + ", request=" + request.toString());

      clusterNames.add(request.getClusterName());

      if (clusterNames.size() > 1) {
        throw new IllegalArgumentException("Updates to multiple clusters is not"
            + " supported");
      }

      if (!serviceGroupNames.containsKey(request.getClusterName())) {
        serviceGroupNames.put(request.getClusterName(), new HashSet<String>());
      }
      if (serviceGroupNames.get(request.getClusterName())
          .contains(request.getServiceGroupName())) {
        // TODO throw single exception
        throw new IllegalArgumentException("Invalid request contains duplicate"
            + " service group names");
      }
      serviceGroupNames.get(request.getClusterName()).add(request.getServiceGroupName());

      Cluster cluster = clusters.getCluster(request.getClusterName());
      ServiceGroup sg = cluster.getServiceGroup(request.getServiceGroupName());
      State oldState = sg.getCurrentState();
      State newState = null;
      if (request.getDesiredState() != null) {
        newState = State.valueOf(request.getDesiredState());
        if (!newState.isValidDesiredState()) {
          throw new IllegalArgumentException("Invalid arguments, invalid"
              + " desired state, desiredState=" + newState);
        }
      }

      boolean persist = false;
      if(request.getServiceGroupDisplayName() != null && !request.getServiceGroupDisplayName().equals(sg.getServiceGroupDisplayName())) {
        sg.setServiceGroupDisplayName(request.getServiceGroupDisplayName());
        persist = true;
      }

      if(request.getAssemblyFile() != null && !request.getAssemblyFile().equals(sg.getAssemblyFile())) {
        sg.setAssemblyFile(request.getAssemblyFile());
        persist = true;
      }

      if(persist) {
        sg.persist();
        LOG.info("Updated properties for service group " + sg.getName());
      }

      if (newState == null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Nothing to do for new updateServiceGroup request"
              + ", clusterName=" + request.getClusterName()
              + ", serviceName=" + request.getServiceGroupName()
              + ", newDesiredState=null");
        }
        continue;
      }

      seenNewStates.add(newState);
      if (newState != oldState) {
        // The if user is trying to start or stop the service, ensure authorization
        if (((newState == State.INSTALLED) || (newState == State.STARTED)) &&
            !AuthorizationHelper.isAuthorized(ResourceType.CLUSTER, cluster.getResourceId(), RoleAuthorization.SERVICE_START_STOP)) {
          throw new AuthorizationException("The authenticated user is not authorized to start or stop services");
        }

        if (!State.isValidDesiredStateTransition(oldState, newState)) {
          throw new AmbariException("Invalid transition for"
              + " service group"
              + ", clusterName=" + cluster.getClusterName()
              + ", clusterId=" + cluster.getClusterId()
              + ", serviceGroupName=" + sg.getName()
              + ", currentDesiredState=" + oldState
              + ", newDesiredState=" + newState);
        }
        // TODO: Remove block for stopping service groups
        if (newState  == State.INSTALLED && oldState == State.STARTED ) {
          throw new AmbariException("Stopping service groups not supported for"
              + " service group"
              + ", clusterName=" + cluster.getClusterName()
              + ", clusterId=" + cluster.getClusterId()
              + ", serviceGroupName=" + sg.getName()
              + ", currentDesiredState=" + oldState
              + ", newDesiredState=" + newState);
        }
        if (!changedServiceGroups.containsKey(newState)) {
          changedServiceGroups.put(newState, new ArrayList<ServiceGroup>());
        }
        changedServiceGroups.get(newState).add(sg);
      }
    }
    if (seenNewStates.size() > 1) {
      // TODO should we handle this scenario
      throw new IllegalArgumentException("Cannot handle different desired state"
          + " changes for a set of services at the same time");
    }
    Cluster cluster = clusters.getCluster(clusterNames.iterator().next());

    return controller.addServiceGroupStages(requestStages, cluster, requestProperties,
        null, changedServiceGroups);
  }

  /**
   * Get a service group request object from a map of property values.
   *
   * @param properties  the predicate
   *
   * @return the service request object
   */
  private ServiceGroupRequest getRequest(Map<String, Object> properties) {
    ServiceGroupRequest svcRequest = new ServiceGroupRequest(
        (String) properties.get(SERVICE_GROUP_CLUSTER_NAME_PROPERTY_ID),
        (String) properties.get(SERVICE_GROUP_SERVICE_GROUP_NAME_PROPERTY_ID),
        (String) properties.get(SERVICE_GROUP_SERVICE_GROUP_DISPLAY_NAME_PROPERTY_ID),
        (String) properties.get(SERVICE_GROUP_SERVICE_GROUP_TYPE_PROPERTY_ID),
        (String) properties.get(SERVICE_GROUP_ASSEMBLY_FILE_PROPERTY_ID),
        (String) properties.get(SERVICE_GROUP_DESIRED_STATE_PROPERTY_ID),
        (String) properties.get(SERVICE_GROUP_CURRENT_STATE_PROPERTY_ID));
    return svcRequest;
  }

  // Create services from the given request.
  public synchronized void createServiceGroups(Set<ServiceGroupRequest> requests)
      throws AmbariException, AuthorizationException {

    if (requests.isEmpty()) {
      LOG.warn("Received an empty requests set");
      return;
    }
    AmbariManagementController controller = getManagementController();
    Clusters clusters = controller.getClusters();
    for(ServiceGroupRequest request: requests) {
      if(StringUtils.isBlank(request.getServiceGroupDisplayName())) {
        request.setServiceGroupDisplayName(request.getServiceGroupName());
      }
    }

    // do all validation checks
    validateCreateRequests(requests, clusters);

    ServiceGroupFactory serviceGroupFactory = controller.getServiceGroupFactory();
    ServiceFactory serviceFactory = getManagementController().getServiceFactory();
    ServiceComponentFactory serviceComponentFactory = getManagementController().getServiceComponentFactory();
    for (ServiceGroupRequest request : requests) {
      Cluster cluster = clusters.getCluster(request.getClusterName());

      State desiredState = State.INIT;
      State currentState = State.INIT;
      if (!request.getServiceGroupType().equalsIgnoreCase("AMBARI")) {
        ServiceGroupCacheKey cacheKey = new ServiceGroupCacheKey(request.getServiceGroupName());
        ServiceGroupCacheValue cacheValue = null;
        try {
          cacheValue = cache.getServiceGroupCacheValue(cacheKey);
        } catch (Exception e) {
          LOG.error("Hit exception when retrieving service group from cache. Exception: " + e.getMessage());
        }
        if (cacheValue != null) {
          // YarnApp already deployed (Ambari takeover)
          desiredState = State.STARTED;
          currentState = State.STARTED;
        }
      }
      // Already checked that service group does not exist
      ServiceGroup sg = serviceGroupFactory.createNew(cluster, request.getServiceGroupName(),
          request.getServiceGroupDisplayName(), request.getServiceGroupType(), request.getAssemblyFile(), desiredState, currentState);
      sg.persist();

      try {
        if (!request.getServiceGroupType().equalsIgnoreCase("AMBARI")) {
          Map<String, Map<String, Integer>> serviceComponentsMap = parseAssemblyFile(request.getAssemblyFile(), cluster);
          if (serviceComponentsMap != null) {
            for (Map.Entry<String, Map<String, Integer>> serviceEntry : serviceComponentsMap.entrySet()) {
              String stackServiceName = serviceEntry.getKey();
              String serviceName = stackServiceName + "_" + request.getServiceGroupName();
              LOG.info("Creating service " + serviceName + " in service group " + request.getServiceGroupName());
              State state = State.INIT;
              // Already checked that service does not exist
              Service s = serviceFactory.createNew(
                  cluster, serviceName, stackServiceName, request.getServiceGroupName());
              s.setDesiredState(state);
              s.setDesiredStackVersion(cluster.getDesiredStackVersion());
              cluster.addService(s);
              s.persist();
              LOG.info("Created service " + serviceName + " in service group " + request.getServiceGroupName());

              for (Map.Entry<String, Integer> componentEntry : serviceEntry.getValue().entrySet()) {
                String componentName = componentEntry.getKey();
                Integer desiredCount = componentEntry.getValue();
                LOG.info("Creating service component " + componentName +
                    " in service " + serviceName +
                    " with desired count " + desiredCount);
                ServiceComponent sc = serviceComponentFactory.createNew(s, componentName);
                sc.setDesiredState(s.getDesiredState());
                sc.setDesiredCount(desiredCount);
                s.addServiceComponent(sc);
                sc.persist();
                LOG.info("Created service component " + componentName +
                    " in service " + serviceName +
                    " with desired count " + desiredCount);
              }
            }
          }
        }
      } catch (Exception e) {
        LOG.error("Failed to create service components for service group " + sg.getName());
        LOG.error("Ignoring Exception : " + e.getMessage());
      }
    }
  }

  // Get services from the given set of requests.
  protected Set<ServiceGroupResponse> getServiceGroups(Set<ServiceGroupRequest> requests)
      throws AmbariException {
    Set<ServiceGroupResponse> response = new HashSet<ServiceGroupResponse>();
    for (ServiceGroupRequest request : requests) {
      try {
        response.addAll(getServiceGroups(request));
      } catch (ServiceGroupNotFoundException e) {
        if (requests.size() == 1) {
          // only throw exception if 1 request.
          // there will be > 1 request in case of OR predicate
          throw e;
        }
      }
    }
    return response;
  }

  // Get services from the given request.
  private Set<ServiceGroupResponse> getServiceGroups(ServiceGroupRequest request)
      throws AmbariException {
    if (request.getClusterName() == null
        || request.getClusterName().isEmpty()) {
      throw new AmbariException("Invalid arguments, cluster name"
          + " cannot be null");
    }
    AmbariManagementController controller = getManagementController();
    Clusters clusters    = controller.getClusters();
    String   clusterName = request.getClusterName();

    final Cluster cluster;
    try {
      cluster = clusters.getCluster(clusterName);
    } catch (ObjectNotFoundException e) {
      throw new ParentObjectNotFoundException("Parent Cluster resource doesn't exist", e);
    }

    Set<ServiceGroupResponse> response = new HashSet<>();
    if (request.getServiceGroupName() != null) {
      ServiceGroup sg = cluster.getServiceGroup(request.getServiceGroupName());
      ServiceGroupResponse serviceGroupResponse = sg.convertToResponse();
      if(sg != null && !sg.getServiceGroupType().equalsIgnoreCase("AMBARI")) {
        ServiceGroupCacheKey cacheKey = new ServiceGroupCacheKey(sg.getName());
        ServiceGroupCacheValue cacheValue = null;
        try {
          cacheValue = cache.getServiceGroupCacheValue(cacheKey);
        } catch (Exception e) {
          LOG.error("Hit exception when retrieving service group from cache. Exception: " + e.getMessage());
        }
        if(cacheValue != null) {
          serviceGroupResponse.setApplicationId(cacheValue.getApplicationId());
          serviceGroupResponse.setLifetime(cacheValue.getLifetime());
          serviceGroupResponse.setQuickLinks(cacheValue.getQuicklinks());
          serviceGroupResponse.setContainers(cacheValue.getContainers());
          serviceGroupResponse.setNumContainers(cacheValue.getNumContainers());
          serviceGroupResponse.setExpectedContainers(cacheValue.getExpectedContainers());
        }
      }
      response.add(serviceGroupResponse);
      return response;
    }

    for (ServiceGroup sg : cluster.getServiceGroups().values()) {
      ServiceGroupResponse serviceGroupResponse = sg.convertToResponse();
      if(!sg.getServiceGroupType().equalsIgnoreCase("AMBARI")) {
        ServiceGroupCacheKey cacheKey = new ServiceGroupCacheKey(sg.getName());
        ServiceGroupCacheValue cacheValue = null;
        try {
          cacheValue = cache.getServiceGroupCacheValue(cacheKey);
        } catch (Exception e) {
          LOG.error("Hit exception when retrieving service group from cache. Exception: " + e.getMessage());
        }
        if(cacheValue != null) {
          serviceGroupResponse.setApplicationId(cacheValue.getApplicationId());
          serviceGroupResponse.setLifetime(cacheValue.getLifetime());
          serviceGroupResponse.setQuickLinks(cacheValue.getQuicklinks());
          serviceGroupResponse.setContainers(cacheValue.getContainers());
          serviceGroupResponse.setNumContainers(cacheValue.getNumContainers());
          serviceGroupResponse.setExpectedContainers(cacheValue.getExpectedContainers());
        }
      }
      response.add(serviceGroupResponse);
    }
    return response;
  }


  // Delete services based on the given set of requests
  protected RequestStatusResponse deleteServiceGroups(Set<ServiceGroupRequest> request)
      throws AmbariException, AuthorizationException {

    Clusters clusters    = getManagementController().getClusters();

    Set<ServiceGroup> removable = new HashSet<>();

    for (ServiceGroupRequest serviceGroupRequest : request) {
      if (StringUtils.isEmpty(serviceGroupRequest.getClusterName())
          || StringUtils.isEmpty(serviceGroupRequest.getServiceGroupName())) {
        // FIXME throw correct error
        throw new AmbariException("invalid arguments");
      } else {

        if(!AuthorizationHelper.isAuthorized(
            ResourceType.CLUSTER, getClusterResourceId(serviceGroupRequest.getClusterName()),
            RoleAuthorization.SERVICE_ADD_DELETE_SERVICES)) {
          throw new AuthorizationException("The user is not authorized to delete service groups");
        }

        ServiceGroup serviceGroup = clusters.getCluster(
            serviceGroupRequest.getClusterName()).getServiceGroup(
            serviceGroupRequest.getServiceGroupName());

        // TODO: Add check to validate there are no services in the service group
        removable.add(serviceGroup);
      }
    }

    for (ServiceGroup serviceGroup : removable) {
      serviceGroup.getCluster().deleteServiceGroup(serviceGroup.getName());
    }

    return null;
  }


  private void validateCreateRequests(Set<ServiceGroupRequest> requests, Clusters clusters)
          throws AuthorizationException, AmbariException {

    AmbariMetaInfo ambariMetaInfo = getManagementController().getAmbariMetaInfo();
    Map<String, Set<String>> serviceGroupNames = new HashMap<>();
    Set<String> duplicates = new HashSet<>();
    for (ServiceGroupRequest request : requests) {
      final String clusterName = request.getClusterName();
      final String serviceGroupName = request.getServiceGroupName();

      Validate.notEmpty(clusterName, "Cluster name should be provided when creating a service group");
      Validate.notEmpty(serviceGroupName, "Service group name should be provided when creating a service group");

      if (LOG.isDebugEnabled()) {
        LOG.debug("Received a createServiceGroup request"
            + ", clusterName=" + ", serviceGroupName=" + serviceGroupName + ", request=" + request);
      }

      if(!AuthorizationHelper.isAuthorized(ResourceType.CLUSTER,
          getClusterResourceId(clusterName), RoleAuthorization.SERVICE_ADD_DELETE_SERVICES)) {
        throw new AuthorizationException("The user is not authorized to create service groups");
      }

      if (!serviceGroupNames.containsKey(clusterName)) {
        serviceGroupNames.put(clusterName, new HashSet<String>());
      }

      if (serviceGroupNames.get(clusterName).contains(serviceGroupName)) {
        // throw error later for dup
        duplicates.add(serviceGroupName);
        continue;
      }
      serviceGroupNames.get(clusterName).add(serviceGroupName);

      Cluster cluster;
      try {
        cluster = clusters.getCluster(clusterName);
      } catch (ClusterNotFoundException e) {
        throw new ParentObjectNotFoundException("Attempted to add a service group to a cluster which doesn't exist", e);
      }
      try {
        ServiceGroup sg = cluster.getServiceGroup(serviceGroupName);
        if (sg != null) {
          // throw error later for dup
          duplicates.add(serviceGroupName);
          continue;
        }
      } catch (ServiceGroupNotFoundException e) {
        // Expected
      }
    }
    // ensure only a single cluster update
    if (serviceGroupNames.size() != 1) {
      throw new IllegalArgumentException("Invalid arguments, updates allowed"
              + "on only one cluster at a time");
    }

    // Validate dups
    if (!duplicates.isEmpty()) {
      String clusterName = requests.iterator().next().getClusterName();
      String msg = "Attempted to create a service group which already exists: "
              + ", clusterName=" + clusterName  + " serviceGroupName=" + StringUtils.join(duplicates, ",");

      throw new DuplicateResourceException(msg);
    }
  }

  private Map<String, Map<String, Integer>> parseAssemblyFile(String assemblyFile, Cluster cluster) throws AmbariException {
    StackId stackId = cluster.getDesiredStackVersion();
    AmbariMetaInfo ambariMetaInfo = getManagementController().getAmbariMetaInfo();
    if(StringUtils.isBlank(assemblyFile)) {
      return null;
    }
    Map<String, Map<String, Integer>> serviceComponentsMap = new HashMap<>();
    Map<String, Object> assemblyMap = gson.fromJson(assemblyFile,
        new TypeToken<Map<String, Object>>() {
        }.getType());
    if(assemblyMap != null && assemblyMap.containsKey("components")) {
      for (Map<String, Object> componentMap : (ArrayList<Map<String, Object>>) assemblyMap.get("components")) {
        if(componentMap.containsKey("name")) {
          String stackComponentName = null;
          String stackServiceName = null;
          Integer desiredCount = 0;
          String assemblyComponentName = (String)componentMap.get("name");
          for(Map.Entry<String, String> componentEntry : componentNameMappings.entrySet()) {
            if(componentEntry.getValue().equalsIgnoreCase(assemblyComponentName)) {
              stackComponentName = componentEntry.getKey();
              break;
            }
          }
          if(stackComponentName != null) {
            stackServiceName =
                ambariMetaInfo.getComponentToService(stackId.getStackName(),
                    stackId.getStackVersion(), stackComponentName);
            if(componentMap.containsKey("number_of_containers")) {
              desiredCount = ((Double)componentMap.get("number_of_containers")).intValue();
            }
          }
          if(stackComponentName != null && stackServiceName != null) {
            if(!serviceComponentsMap.containsKey(stackServiceName)) {
              serviceComponentsMap.put(stackServiceName, new HashMap<String, Integer>());
            }
            Map<String, Integer> componentsMap = serviceComponentsMap.get(stackServiceName);
            componentsMap.put(stackComponentName, desiredCount);
          }
        }
      }
    }
    return serviceComponentsMap;
  }
}
