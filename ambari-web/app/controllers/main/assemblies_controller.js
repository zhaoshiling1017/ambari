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

App.MainAssembliesController = Em.Controller.extend({

  name: 'mainAssembliesController',

  /**
   * Filter value for Apps Names
   *
   * @type {string}
   */
  filterString: '',

  /**
   * @type {boolean}
   */
  showFilterString: true,

  /**
   * @type {App.ServiceGroup[]}
   */
  serviceGroups: function () {
    if (!App.router.get('clusterController.isLoaded')) {
      return [];
    }
    return App.ServiceGroup.find();
  }.property('App.router.clusterController.isLoaded'),

  /**
   * @type {App.ServiceGroup[]}
   */
  visibleServiceGroups: Em.computed.filterBy('serviceGroups', 'isCoreServiceGroup', false),

  /**
   * @type {App.ServiceGroup?}
   */
  activeServiceGroup: Em.computed.findBy('serviceGroups', 'isActive', true),

  /**
   * All Store Categories
   *
   * @type {App.StoreCategory[]}
   */
  storeCategories: App.StoreCategory.find(),

  /**
   * All Store Apps
   *
   * @type {App.StoreApp[]}
   */
  storeApps: App.StoreApp.find(),

  /**
   * All Store Collections
   *
   * @type {App.StoreCollection[]}
   */
  storeCollections: App.StoreCollection.find(),

  /**
   * Selected Store Collection
   *
   * @type {App.StoreCollection?}
   */
  activeStoreCollection: Em.computed.findBy('storeCollections', 'isActive', true),

  /**
   * Checks if there is no selected Store Category
   *
   * @type {boolean}
   */
  noCategorySelected: Em.computed.everyBy('storeCategories', 'isActive', false),

  /**
   * Checks if there is no selected Store Collection
   *
   * @type {boolean}
   */
  noCollectionSelected: Em.computed.everyBy('storeCollections', 'isActive', false),

  /**
   * Checks if there is some selected Store Category
   *
   * @type {boolean}
   */
  isSomeCategorySelected: Em.computed.someBy('storeCategories', 'isActive', true),

  /**
   * Checks if there is some selected Store Category
   *
   * @type {boolean}
   */
  isSomeCollectionSelected: Em.computed.someBy('storeCollections', 'isActive', true),

  /**
   * All Store Apps are shown if there is no selected Store Category and Store Collection
   *
   * @type {boolean}
   */
  allAppsAreShown: Em.computed.and('noCategorySelected', 'noCollectionSelected'),

  /**
   * Placeholder for search-input
   *
   * @type {string}
   */
  searchString: Em.I18n.t('common.search.small'),

  /**
   * The active menu subtitle eg. "> Discover" or "> Manage"
   *
   * @type {string}
   */
  subtitle: function() {
    var subtitle = "";
    if (this.get('isSomeCategorySelected')) {
      subtitle = ">   " + Em.I18n.t('appStore.menu.header.discover');
    } else if (this.get('isSomeCollectionSelected') || this.get('activeServiceGroupId')) {
      subtitle = ">   " + Em.I18n.t('appStore.menu.header.manage');
    }
    return subtitle;
  }.property('isSomeCategorySelected', 'isSomeCollectionSelected', 'activeServiceGroupId'),

  /**
   * @param {{context: App.StoreCategory}} event
   */
  selectCategory: function (event) {
    this.showAllApps();
    var storeCategory = event.context;
    storeCategory.set('isActive', true);
    this.closeManageAssembliesPanels();
    this.target.transitionTo('main.assemblies.categories.details', storeCategory);
  },

  /**
   * This method hides MANAGE->ASSEMBLIES and MANAGE->COLLECTIONS panels
   * @private
   * @method {closeManageAssembliesPanels}
   */
  closeManageAssembliesPanels: function() {
    var appStoreEl = $("#apps-store");
    var deployedAssembliesEl = appStoreEl.find("#manage-deployed-assemblies-label");
    var deployedAssembliesContent = appStoreEl.find("#manage-deployed-assemblies-content");
    var collectionsAssembliesEl = appStoreEl.find("#manage-assemblies-collection-label");
    var collectionsAssembliesContent = appStoreEl.find("#manage-assemblies-collection-content");
    if (deployedAssembliesContent.hasClass('in')) {
      deployedAssembliesEl.trigger('click');
    }

    if (collectionsAssembliesContent.hasClass('in')) {
      collectionsAssembliesEl.trigger('click');
    }
  },

  /**
   * Deselect all Store Categories and Collections
   */
  showAllApps: function () {
    this.get('storeCategories').setEach('isActive', false);
    this.get('storeCollections').setEach('isActive', false);
    this.get('storeApps').setEach('isActive', false);
  },

  /**
   *
   * @param {{context: App.StoreCollection}} event
   */
  selectCollection: function (event) {
    this.showAllApps();
    var storeCollection = event.context;
    storeCollection.set('isActive', true);
    this.target.transitionTo('main.assemblies.collections.details', storeCollection);
  },

  /**
   * {{context: App.ServiceGroup}} @param event
   */
  makeServiceGroupActive: function(event) {
    var serviceGroup = event.context;
    this.set('activeServiceGroupId', serviceGroup.get('id'));
    this.setActiveServiceGroupId();
    this.closeNonServiceGroupsPanels();
    this.target.transitionTo('main.assemblies.serviceGroups.details.summary', serviceGroup);
  },

  setActiveServiceGroupId: function() {
    var serviceGroupId = this.get('activeServiceGroupId');
    var visibleServiceGroups = this.get('visibleServiceGroups');
    visibleServiceGroups.forEach(function(item) {
      item.set('isActive', serviceGroupId === item.get('id'));
    });
  }.observes('activeServiceGroupId'),

  /**
   * This method hides DISCOVER->CATEGORIES and MANAGE->COLLECTIONS panels
   * @private
   * @method {closeNonServiceGroupsPanels}
   */
  closeNonServiceGroupsPanels: function() {
    var appStoreEl = $("#apps-store");
    var discoverAssembliesEl = appStoreEl.find("#discover-assemblies-label");
    var discoverAssembliesContent = appStoreEl.find("#discover-assemblies-content");
    var collectionsAssembliesEl = appStoreEl.find("#manage-assemblies-collection-label");
    var collectionsAssembliesContent = appStoreEl.find("#manage-assemblies-collection-content");
    if (discoverAssembliesContent.hasClass('in')) {
      discoverAssembliesEl.trigger('click');
    }

    if (collectionsAssembliesContent.hasClass('in')) {
      collectionsAssembliesEl.trigger('click');
    }
  }


});