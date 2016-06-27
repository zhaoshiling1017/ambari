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

App.MainAssembliesServiceGroupMenuView = Em.View.extend({

  templateName: require('templates/main/assemblies/service_groups/menu'),

  menu: function () {
    var currentPath = App.get('router.currentState.path');
    return [
      {title: Em.I18n.t('common.summary'), route: 'summary', isActive: currentPath.endsWith('summary')},
      {title: Em.I18n.t('common.settings'), route: 'settings', isActive: currentPath.endsWith('settings')},
      {title: Em.I18n.t('assemblies.serviceGroups.menu.detailedInfo'), route: 'detailedInfo', isActive: currentPath.endsWith('detailedInfo')}
    ];
  }.property('App.router.currentState.path'),

  didInsertElement: function () {
    App.tooltip(this.$("[rel='assembly-file-link']"));
  },

  willDestroyElement: function () {
    this.$("[rel='assembly-file-link']").tooltip('destroy');
  },

  /**
   * @param {{context: string}} event
   */
  moveTo: function (event) {
    var route = event.context;
    this.get('controller.target').transitionTo(route);
  }

});