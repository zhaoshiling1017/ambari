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

App.AppsRowView = Em.View.extend({

  templateName: require('/templates/main/assemblies/apps_row'),

  didInsertElement: function () {
    App.tooltip($(".assemblies.icon-question-sign"), {
      placement: "top",
      title: Em.I18n.t('appStore.apps.title.assemblies.tooltip')
    });
    App.tooltip($(".components.icon-question-sign"), {
      placement: "top",
      title: Em.I18n.t('appStore.apps.title.components.tooltip')
    });
  },

  /**
   * Should be bound from the template
   *
   * @type {string}
   */
  rowTitle: '',

  /**
   * @type {number}
   */
  startIndex: 0,

  /**
   * @type {number}
   */
  endIndex: function() {
    return this.get('startIndex') + 5;
  }.property('startIndex'),

  /**
   * @type {App.StoreApp[]}
   */
  visibleApps: function () {
    return this.get('apps').slice(this.get('startIndex'), this.get('endIndex'));
  }.property('apps.[]', 'startIndex', 'endIndex'),

  /**
   * @type {boolean}
   */
  hasActiveApp: Em.computed.bool('activeApp'),

  /**
   * @type {App.StoreApp}
   */
  activeApp: Em.computed.findBy('visibleApps', 'isActive', true),

  /**
   * @type {boolean}
   */
  disabledLeft: Em.computed.equal('startIndex', 0),

  /**
   * @type {boolean}
   */
  disabledRight: Em.computed.gteProperties('endIndex', 'apps.length'),

  moveLeft: function() {
    if (!this.get('disabledLeft')) this.decrementProperty('startIndex');
  },

  moveRight: function() {
    if (!this.get('disabledRight')) this.incrementProperty('startIndex');
  }
});