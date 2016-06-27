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

/**
 * Check serviceGroup's <code>dependentKey</code>
 * If there is no serviceGroup, it means that view is used for the whole cluster and all service groups should be checked
 *
 * @param {string} dependentKey
 * @returns {Ember.ComputedProperty}
 */
function serviceGroupBased(dependentKey) {
  var key1 = 'serviceGroup.' + dependentKey;
  var key2 = 'serviceController.content.@each.' + dependentKey;
  return Ember.computed(key1, key2, function () {
    var serviceGroup = this.get('serviceGroup');
    if (!serviceGroup) {
      return this.get('serviceController.content').everyProperty(dependentKey, true);
    }
    return serviceGroup.get(dependentKey);
  });
}

/**
 * List of actions like "Start All", "Stop All" etc
 * Used for service groups and whole cluster
 *
 * @type {Em.View}
 */
App.AllServicesActionView = Em.View.extend({
  templateName: require('templates/main/service/all_services_actions'),

  isStopAllDisabled: serviceGroupBased('isStopAllDisabled'),
  isStartAllDisabled: serviceGroupBased('isStartAllDisabled'),
  isRestartAllRequiredDisabled: serviceGroupBased('isRestartAllRequiredDisabled'),

  serviceController: function () {
    return App.get('router.mainServiceController');
  }.property('App.router.mainServiceController')
});
