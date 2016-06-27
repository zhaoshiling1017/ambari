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
 * Usage in the templates:
 * <pre>
 *   {{view App.StoreAppBlockView storeAppBinding="storeApp"}}
 * </pre>
 *
 * @type {Em.View}
 */
App.StoreAppBlockView = Em.View.extend({

  classNames: ['store-app-block-view'],

  classNameBindings: ['storeApp.isActive:active'],

  templateName: require('templates/main/assemblies/store_app_block'),

  /**
   * Bound from template
   *
   * @type {App.StoreApp}
   */
  storeApp: null,

  /**
   * Coma-separated list of the category names that App belongs to
   *
   * @type {string}
   */
  categoryNames: function () {
    return this.get('storeApp.storeCategories').mapProperty('name').join(', ');
  }.property('storeApp.storeCategories.@each.name'),

  /**
   * Show only two first services for App
   *
   * @type {string}
   */
  shownServices: function () {
    return this.get('storeApp.services') ? this.get('storeApp.services').substring(0, 19) : '';
  }.property('storeApp.services'),

  /**
   * Should be '...' shown for services
   * true - if App belongs to more services
   * false - otherwise
   *
   * @type {boolean}
   */
  moreServices: Em.computed.gt('storeApp.services.length', 19),

  /**
   * Should be double-pane shown for App
   * true - if App belongs to more than one service
   * false - otherwise
   *
   * @type {boolean}
   */
  showBgBlock: function () {
    return this.get('storeApp.services') ? this.get('storeApp.services').split('|').length > 1 : false;
  }.property('storeApp.services.[]'),

});