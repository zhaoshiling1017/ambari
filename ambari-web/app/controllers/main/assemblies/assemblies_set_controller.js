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

App.MainAssembliesSetController = Em.Controller.extend({

  name: 'mainAssembliesSetController',

  /**
   * @type {boolean}
   */
  storeAppsEmpty: Em.computed.equal('content.storeApps.length', 0),

  /**
   * Selected Store App
   *
   * @type {App.StoreApp?}
   */
  activeStoreApp: Em.computed.findBy('content.storeApps', 'isActive', true),

  /**
   * Filter all Store Apps by selected Store Category or Store Collection
   * and <code>filterString</code> (used for App Names only)
   *
   * @type {App.StoreApp[]}
   */
  filteredStoreApps: function () {
    var filterString = App.get('router.mainAssembliesController.filterString').toLowerCase();
    var storeApps = this.get('content.storeApps') || [];
    return Em.isEmpty(filterString) ? storeApps : storeApps.filter(function (storeApp) {
      return storeApp.get('name').toLowerCase().indexOf(filterString) !== -1;
    });
  }.property('content.storeApps.[]', 'App.router.mainAssembliesController.filterString'),

  /**
   * @type {string}
   */
  componentsTitle: Em.I18n.t('appStore.apps.title.components'),

  /**
   * @type {App.StoreApp[]}
   */
  components: Em.computed.filterBy('filteredStoreApps', 'isComponent', true),

  /**
   * @type {string}
   */
  assembliesTitle: Em.I18n.t('appStore.apps.title.assemblies'),

  /**
   * @type {App.StoreApp[]}
   */
  assemblies: Em.computed.filterBy('filteredStoreApps', 'isComponent', false),

  /**
   * @param {{context: App.StoreApp}} event
   */
  showDetails: function (event) {
    event.preventDefault();
    event.stopPropagation();
    var storeApp = event.context;
    this.get('content.storeApps').setEach('isActive', false);
    storeApp.set('isActive', true);
  },

  closeDetails: function () {
    this.get('content.storeApps').setEach('isActive', false);
  },

  /**
   * @param {{context: App.StoreApp}} event
   */
  startDeploy: function (event) {
    var storeApp = this.get('activeStoreApp');
    if (!storeApp) {
      return false;
    }
    return App.ModalPopup.show({
      classNames: ['deploy-app-configs-modal'],
      storeApp: storeApp,
      isDeploying: false,
      progress: 0,
      progressStyle: Em.computed.format('width: {0}%;', 'progress'),
      headerClass: Ember.View.extend({
        templateName: require('templates/main/assemblies/deploy_store_app/deploy_store_app_header'),
        categories: function() {
          return this.get('parentView.storeApp.storeCategories').mapProperty('name').join(', ')
        }.property('parentView.storeApp.storeCategories.@each.name')
      }),
      bodyClass: Ember.View.extend({
        templateName:  require('templates/main/assemblies/deploy_store_app')
      }),

      footerClass: Ember.View.extend({
        templateName: require('templates/main/assemblies/deploy_store_app/deploy_store_app_footer')
      }),

      primary: Em.I18n.t('common.deploy'),

      startDeploy: function() {
        //TODO when api will be ready
        this.set('isDeploying', true);
        this.set('progress', 0);
        this.pollDeployProgress();
      },

      pollDeployProgress: function() {
        //TODO when api will be ready
        var self = this;
        if (this.get('isDeploying')) {
          setTimeout(function() {
            if (self.get('progress') === 100) {
              self.set('isDeploying', false);
              self.hide();
            } else {
              self.set('progress', self.get('progress') + 5);
              self.pollDeployProgress();
            }
          }, 1000);
        }
      },

      onPrimary: function () {
        this.startDeploy();
      }

    });
  },

  addToCollection: function () {

  }

});