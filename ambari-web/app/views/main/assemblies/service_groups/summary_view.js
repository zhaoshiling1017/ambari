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

App.MainAssembliesServiceGroupSummaryView = Em.View.extend({

  templateName: require('templates/main/assemblies/service_groups/summary'),

  /**
   * Contain array with list of master components from <code>App.Service.hostComponets</code> which are
   * <code>App.HostComponent</code> models.
   *
   * @type {App.HostComponent[]}
   */
  mastersObj: [],
  mastersLength: 0,

  /**
   * Contain array with list of slave components models <code>App.SlaveComponent</code>.
   *
   * @type {App.SlaveComponent[]}
   */
  slavesObj: [],
  slavesLength: 0,

  /**
   * Contain array with list of client components models <code>App.ClientComponent</code>.
   *
   * @type {App.ClientComponent[]}
   */
  clientObj: [],
  clientsLength: 0,

  /**
   * Show spinner while components are loading
   *
   * @type {boolean}
   * @default true
   */
  showSpinner: true,

  didInsertElement: function() {
    var services = this.get('controller.content.services');
    if (services.content.length) {
      this.set('controller.activeService', services.objectAt(0));
    }
    var shownServices = this.get('controller.content.services').map(function(_service, index) {
      return Em.Object.create({
        id:  _service.get('id'),
        displayName: _service.get('displayName'),
        url: _service.get('logo'),
        isActive: !!index
      });
    });
    this.set('shownServices', shownServices);
    this.set('activeService', shownServices.objectAt(0));
  },

  willDestroyElement: function() {
    this.get('mastersObj').clear();
    this.get('slavesObj').clear();
    this.get('clientObj').clear();
  },


  /**
   * Show services icons
   *
   * @type {{displayName: string, url: string}[]}
   */
  shownServices: [],

  /**
   * {{context: App.ServiceGroup}} @param event
   */
  makeServiceActive: function(event) {
    var service = event.context;
    var controller= this.get('controller');
    service.set('isActive', true);
    this.set('activeService', service);
  },

  setActiveService: function() {
    var activeService = this.get('activeService');
    var services = this.get('shownServices');
    services.forEach(function(item) {
      item.set('isActive', activeService.get('id') === item.get('id'));
    });
  }.observes('activeService'),


  serviceSummaryView: Em.View.extend(App.MainDashboardServiceViewWrapper, {
    templateName: 'templates/main/service/info/summary/base',
    isFullWidth: true
  }),

  componentsLengthDidChange: function() {
    var self = this;
    var service = this.get('controller.activeService');
    if (!service || service.get('deleteInProgress')) return;
    Em.run.once(self, 'setComponentsContent');
  }.observes('activeService','activeService.hostComponents.length', 'activeService.slaveComponents.@each.totalCount', 'activeService.clientComponents.@each.totalCount'),

  setComponentsContent: function() {
    Em.run.next(function() {
      var activeService = this.get('activeService');

      var service = App.Service.find().findProperty('id',activeService.get('id'));
      if (Em.isNone(service)) {
        return;
      }

      var masters = service.get('hostComponents').filterProperty('isMaster');
      var slaves = service.get('slaveComponents').toArray();
      var clients = service.get('clientComponents').toArray();

      this.get('mastersObj').clear();
      this.get('slavesObj').clear();
      this.get('clientObj').clear();
      this.updateComponentList(this.get('mastersObj'), masters);
      this.updateComponentList(this.get('slavesObj'), slaves);
      this.updateComponentList(this.get('clientObj'), clients);
      this.set('showSpinner', !(masters.length || slaves.length || clients.length));

    }.bind(this));
  },

  updateComponentList: function(source, data) {
    var sourceIds = source.mapProperty('id');
    var dataIds = data.mapProperty('id');
    if (!sourceIds.length) {
      source.pushObjects(data);
    }
    if (source.length > data.length) {
      sourceIds.forEach(function(item, index) {
        if (!dataIds.contains(item)) {
          source.removeAt(index);
        }
      });
    }
    else {
      if (source.length < data.length) {
        dataIds.forEach(function (item, index) {
          if (!sourceIds.contains(item)) {
            source.pushObject(data.objectAt(index));
          }
        });
      }
    }
  },

  /**
   * Open another browser window with assembly file content highlighted with highlight.js
   */
  showAssemblyFile: function () {
    var assemblyFile = this.get('controller.content.assemblyFile');
    var assemblyFileContent = '';
    try {
      assemblyFileContent = JSON.stringify(JSON.parse(assemblyFile), null, 2);
    }
    catch(e) {
      assemblyFileContent = assemblyFile;
    }
    var newwindow = window.open();
    var newdocument = newwindow.document;
    newdocument.write('<head><link rel="stylesheet" href="stylesheets/vendor.css"></head><body><pre>' + hljs.highlight('json', assemblyFileContent).value + '</pre></body>');
    newdocument.close();
  }

});