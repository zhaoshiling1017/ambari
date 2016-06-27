/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

var App = require('app');

App.serviceGroupMapper = App.QuickDataMapper.create({
  model: App.ServiceGroup,
  config: {
    id: 'ServiceGroupInfo.service_group_name',
    cluster_name: 'ServiceGroupInfo.cluster_name',
    service_group_name: 'ServiceGroupInfo.service_group_name',
    service_group_display_name: 'ServiceGroupInfo.service_group_display_name',
    service_group_type: 'ServiceGroupInfo.service_group_type',
    current_state: 'ServiceGroupInfo.current_state',
    desired_state: 'ServiceGroupInfo.desired_state',
    assembly_file: 'ServiceGroupInfo.assembly_file',
    application_id: 'ServiceGroupInfo.application_id',
    quicklinks: 'ServiceGroupInfo.quicklinks',
    lifetime: 'ServiceGroupInfo.lifetime',
    containers_key: 'ServiceGroupInfo.containers',
    containers_type: 'array',
    containers: {
      item: ''
    },
    services_key: 'services',
    services_type: 'array',
    services: {
      item: 'id'
    }
  },

  mapServiceGroups: function(json) {
    this.clearStackModels();
    App.resetDsStoreTypeMap(App.ServiceGroup);
    this.map(json);
  },

  map: function (json) {
    console.time("App.serviceGroupMapper execution time");
    var self = this;
    var displayOrderLength = App.ServiceGroup.displayOrder.length;
    var items = json.items.map(function (item, index) {
      var displayOrderIndex = App.ServiceGroup.displayOrder.indexOf(item.ServiceGroupInfo.service_group_display_name);
      return $.extend(item, {
        index: displayOrderIndex == -1 ? displayOrderLength + index : displayOrderIndex
      });
    }).sortProperty('index');



    var item = {
      ServiceGroupInfo: {
        cluster_name: App.get('clusterName'),
        containers: [],
        service_group_name: "CORE"
      }
    };
    items.unshift(item);
    var serviceGroups = items.map(function (serviceGroup) {
      var services = [];
      if (serviceGroup.services) {
        services = serviceGroup.services.map(function(service) {
          return {id: service.ServiceInfo.service_name};
        });
      }
      else {
        //TODO remove after server API will be implemented
        services = App.Service.find()
          .filterProperty('serviceGroupName', serviceGroup.ServiceGroupInfo.service_group_name)
          .map(function(service) {
            return {
              id: service.get('id')
            }
          });
      }
      serviceGroup.services = services;
      return self.parseIt(serviceGroup, self.get('config'));
    });
    App.store.loadMany(this.get('model'), serviceGroups);
    App.store.commit();

    console.timeEnd("App.serviceGroupMapper execution time");
  },

  getJsonProperty: function (json, path) {
    if (!path) {
      return json;
    }
    return this._super(json, path);
  },

  /**
   * Clean store from already loaded data.
   **/
  clearStackModels: function () {
    var models = [App.ServiceGroup];
    models.forEach(function (model) {
      var records = App.get('store').findAll(model).filterProperty('id');
      records.forEach(function (rec) {
        Ember.run(this, function () {
          rec.deleteRecord();
          App.store.commit();
        });
      }, this);
    }, this);
  }

});
