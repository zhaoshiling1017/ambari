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

var App = require('app');

App.ViewInstance = DS.Model.extend({
  id: DS.attr('string'),
  name: DS.attr('string'),
  displayName: DS.attr('string'),
  serviceName: DS.attr('string'),
  viewName: DS.attr('string'),
  version: DS.attr('string'),
  layoutName: DS.attr('string'),
  instanceName: function() {
    return this.get('name');
  }.property('name')
});

App.ViewInstance.FIXTURES = [
  {
    id: 'FILES_AUTO_FILES_INSTANCE_1.0.0',
    service_name: 'HDFS',
    name: 'AUTO_FILES_INSTANCE',
    display_name: 'View 1',
    view_name: 'FILES',
    version: '1.0.0',
    layout_name: 'service-tab'
  },
  {
    id: 'FILES_test1_1.0.0',
    service_name: 'HDFS',
    name: 'test1',
    display_name: 'View 2',
    view_name: 'FILES',
    version: '1.0.0',
    layout_name: 'service-tab'
  },
  {
    id: 'CAPACITY-SCHEDULER_AUTO_CS_INSTANCE_1.0.0',
    service_name: 'YARN',
    name : 'AUTO_CS_INSTANCE',
    display_name: 'View 1',
    view_name : 'CAPACITY-SCHEDULER',
    version : '1.0.0',
    layout_name: 'service-tab'
  },
  {
    id: 'HIVE_AUTO_HIVE_INSTANCE_1.0.0',
    service_name: 'HIVE',
    name : 'AUTO_HIVE_INSTANCE',
    display_name: 'View 1',
    view_name : 'HIVE',
    version : '1.0.0',
    layout_name : 'service-tab'
  },
  {
    id: 'HIVE_AUTO_HIVE_INSTANCE_2.0.0',
    service_name: 'HIVE',
    name : 'AUTO_HIVE_INSTANCE',
    display_name: 'View 2',
    view_name : 'HIVE',
    version : '2.0.0',
    layout_name : 'service-tab'
  },
  {
    id: 'TEZ_TEZ_CLUSTER_INSTANCE_0.7.0.2.5.0.0-5308',
    service_name: 'TEZ',
    name : 'TEZ_CLUSTER_INSTANCE',
    display_name: 'View 1',
    view_name : 'TEZ',
    version : '0.7.0.2.5.0.0-5308',
    layout_name : 'service-tab'
  }
];