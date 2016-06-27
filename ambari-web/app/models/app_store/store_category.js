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

App.StoreCategory = DS.Model.extend({

  name: DS.attr('string'),
  isActive: DS.attr('boolean'),
  storeApps: DS.hasMany('App.StoreApp')

});

App.StoreCategory.FIXTURES = [
  {id: 1, name: 'All', is_active: false, store_apps: [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17]},
  {id: 2, name: 'Access', is_active: false, store_apps: []},
  {id: 3, name: 'Data Movement', is_active: false, store_apps: [4, 5, 8, 12]},
  {id: 4, name: 'Security', is_active: false, store_apps: [1, 3, 9]},
  {id: 5, name: 'Governance', is_active: false, store_apps: []},
  {id: 6, name: 'Operations', is_active: false, store_apps: [2, 6, 10, 11, 13, 15, 16, 17]},
  {id: 7, name: 'Storage', is_active: false, store_apps: [7, 14]}
];