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

App.ActiveStoreAppView = Em.View.extend({

  templateName: require('templates/main/assemblies/active_store_app'),

  /**
   * @type {string}
   */
  title: Em.computed.firstNotBlank('storeApp.title', 'storeApp.name'),

  /**
   * Bound from template
   *
   * @type {App.StoreApp}
   */
  storeApp: null,

  /**
   * Show services icons on the details popup
   *
   * @type {string}
   */
  shownServices: function () {
    var serviceNames = this.get('storeApp.services') ? this.get('storeApp.services').split('|').slice(0, 4) : [];
    var services = [];
    serviceNames.forEach(function(name) {
      var name = name.toString().trim();
      if (name != "LOG SEARCH") {
        services.push({
          name: name,
          url: "/img/" + name.toLowerCase() + "-color.png"
        });
      }
    });
    return services;
  }.property('storeApp.services')
});