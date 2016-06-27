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

App.MainServiceController = Em.ArrayController.extend({

  name: 'mainServiceController',

  /**
   * @type {Ember.Object[]}
   */
  content: function () {
    if (!App.router.get('clusterController.isLoaded')) {
      return [];
    }
    return App.ServiceGroup.find();
  }.property('App.router.clusterController.isLoaded').volatile(),

  isAllCoreServicesInstalled: function() {
    var coreServiceGroup = this.get('content').findProperty('serviceGroupName', 'CORE');
    return coreServiceGroup ? this.get('content').findProperty('id', 'CORE').get('isAllServicesInstalled') : false;
  }.property('content.@each.isAllServicesInstalled'),

  /**
   * Current cluster
   * @type {Ember.Object}
   */
  cluster: function () {
    if (!App.router.get('clusterController.isClusterDataLoaded')) {
      return null;
    }
    return App.Cluster.find().objectAt(0);
  }.property('App.router.clusterController.isClusterDataLoaded'),

  /**
   * Callback for <code>start all service</code> button
   * @return {App.ModalPopup|null}
   * @method startAllService
   */
  startAllService: function (event) {
    var serviceGroup = event.context;
    if (!serviceGroup || !serviceGroup.get('isStartAllDisabled')) {
      return this.startStopAllService(serviceGroup, 'STARTED');
    }
  },

  /**
   * Callback for <code>stop all service</code> button
   * @return {App.ModalPopup|null}
   * @method stopAllService
   */
  stopAllService: function (event) {
    var serviceGroup = event.context;
    if (!serviceGroup || !serviceGroup.get('isStopAllDisabled')) {
      return this.startStopAllService(serviceGroup, 'INSTALLED');
    }
  },

  /**
   * Common method for "start-all", "stop-all" calls
   * @param {object} serviceGroup
   * @param {string} state 'STARTED|INSTALLED'
   * @returns {App.ModalPopup|null}
   * @method startStopAllService
   */
  startStopAllService: function(serviceGroup, state) {
    var self = this;
    var _serviceGroup = serviceGroup || Em.Object.create();
    var serviceGroupName = _serviceGroup.get('serviceGroupName');
    var isCoreServiceGroup = serviceGroupName === 'CORE';
    var confirmStopMsg, confirmStartMsg;
    if (serviceGroupName === 'CORE') {
      confirmStopMsg = Em.I18n.t('services.service.core.stopAll.confirmMsg');
      confirmStartMsg = Em.I18n.t('services.service.core.startAll.confirmMsg');
    } else {
      if(serviceGroupName) {
        confirmStopMsg = Em.I18n.t('services.service.stopAll.confirmMsg');
        confirmStartMsg = Em.I18n.t('services.service.startAll.confirmMsg');
      }
      else {
        confirmStopMsg = Em.I18n.t('services.service.cluster.stopAll.confirmMsg');
        confirmStartMsg = Em.I18n.t('services.service.cluster.startAll.confirmMsg');
      }
    }
    var bodyMessage = Em.Object.create({
      confirmMsg: state === 'INSTALLED' ? confirmStopMsg.format(serviceGroupName) : confirmStartMsg.format(serviceGroupName),
      confirmButton: state === 'INSTALLED' ? Em.I18n.t('services.service.stop.confirmButton') : Em.I18n.t('services.service.start.confirmButton')
    });

    if (isCoreServiceGroup && state === 'INSTALLED' && App.Service.find().filterProperty('serviceName', 'HDFS').someProperty('workStatus', App.HostComponentStatus.started)) {
      return App.router.get('mainServiceItemController').checkNnLastCheckpointTime(function () {
        return App.showConfirmationFeedBackPopup(function (query) {
          self.allServicesCall(state, query, _serviceGroup);
        }, bodyMessage);
      });
    }
    return App.showConfirmationFeedBackPopup(function (query) {
      self.allServicesCall(state, query, _serviceGroup);
    }, bodyMessage);
  },

  /**
   * Do request to server for "start|stop" all services
   * @param {string} state "STARTED|INSTALLED"
   * @param {object} query
   * @param {object} serviceGroup
   * @method allServicesCall
   * @return {$.ajax}
   */
  allServicesCall: function (state, query, serviceGroup) {
    var context = state === 'INSTALLED' ? App.BackgroundOperationsController.CommandContexts.STOP_ALL_SERVICES :
      App.BackgroundOperationsController.CommandContexts.START_ALL_SERVICES;
    var services = serviceGroup.get('services') || App.Service.find();
    var servicesList = services.mapProperty("serviceName").join(',');
    var data = {
      context: context,
        ServiceInfo: {
        state: state
      },
      urlParams: "ServiceInfo/service_name.in(" + servicesList + ")",
      query: query
    };

    return App.ajax.send({
      name: 'common.services.update',
      sender: this,
      data: data,
      success: 'allServicesCallSuccessCallback',
      error: 'allServicesCallErrorCallback'
    });
  },

  /**
   * Restart all services - stops all services, then starts them back
   */
  restartAllServices: function () {
    this.silentStopAllServices();
  },

  /**
   * Silent stop all services - without user confirmation
   * @returns {$.ajax}
   */
  silentStopAllServices: function () {
    return App.ajax.send({
      name: 'common.services.update',
      sender: this,
      data: {
        context: App.BackgroundOperationsController.CommandContexts.STOP_ALL_SERVICES,
        ServiceInfo: {
          state: 'INSTALLED'
        }
      },
      success: 'silentStopSuccess'
    });
  },

  isStopAllServicesFailed: function() {
    var workStatuses = App.Service.find().mapProperty('workStatus');
    for (var i = 0; i < workStatuses.length; i++) {
      if (workStatuses[i] !== 'INSTALLED' && workStatuses[i] !== 'STOPPING') {
        return true;
      }
    }
    return false;
  },

  /**
   * Success callback for silent stop
   */
  silentStopSuccess: function () {
    var self = this;

    App.router.get('userSettingsController').dataLoading('show_bg').done(function (initValue) {
      if (initValue) {
        App.router.get('backgroundOperationsController').showPopup();
      }

      Em.run.later(function () {
        self.set('shouldStart', true);
      }, App.bgOperationsUpdateInterval);
    });
  },

  /**
   * Silent start all services - without user confirmation
   */
  silentStartAllServices: function () {
    if (
      !App.router.get('backgroundOperationsController').get('allOperationsCount')
      && this.get('shouldStart')
      && !this.isStopAllServicesFailed()
    ) {
      this.set('shouldStart', false);
      return App.ajax.send({
        name: 'common.services.update',
        sender: this,
        data: {
          context: App.BackgroundOperationsController.CommandContexts.START_ALL_SERVICES,
          ServiceInfo: {
            state: 'STARTED'
          }
        },
        success: 'silentCallSuccessCallback'
      });
    }
  }.observes('shouldStart', 'controllers.backgroundOperationsController.allOperationsCount'),

  /**
   * Success callback for silent start
   */
  silentCallSuccessCallback: function () {
    // load data (if we need to show this background operations popup) from persist
    App.router.get('userSettingsController').dataLoading('show_bg').done(function (initValue) {
      if (initValue) {
        App.router.get('backgroundOperationsController').showPopup();
      }
    });
  },

  /**
   * Success-callback for all-services request
   * @param {object} data
   * @param {object} xhr
   * @param {object} params
   * @method allServicesCallSuccessCallback
   */
  allServicesCallSuccessCallback: function (data, xhr, params) {
    params.query.set('status', 'SUCCESS');

    // load data (if we need to show this background operations popup) from persist
    App.router.get('userSettingsController').dataLoading('show_bg').done(function (initValue) {
      if (initValue) {
        App.router.get('backgroundOperationsController').showPopup();
      }
    });
  },

  /**
   * Error-callback for all-services request
   * @param {object} request
   * @param {object} ajaxOptions
   * @param {string} error
   * @param {object} opt
   * @param {object} params
   * @method allServicesCallErrorCallback
   */
  allServicesCallErrorCallback: function (request, ajaxOptions, error, opt, params) {
    params.query.set('status', 'FAIL');
  },

  /**
   * "Add-service"-click handler
   * @method gotoAddService
   */
  gotoAddService: function () {
    if (this.get('isAllCoreServicesInstalled')) {
      return;
    }
    App.router.get('addServiceController').setDBProperty('onClosePath', 'main.services.index');
    App.router.transitionTo('main.serviceAdd');
  },

  /**
   * Show confirmation popup and send request to restart all host components with stale_configs=true
   */
  restartAllRequired: function (serviceGroup) {
    var self = this;
    if (!serviceGroup.get('isRestartAllRequiredDisabled')) {
      return App.showConfirmationPopup(function () {
            self.restartHostComponents();
          }, Em.I18n.t('services.service.refreshAll.confirmMsg').format(
              App.HostComponent.find().filterProperty('staleConfigs').mapProperty('service.displayName').uniq().join(', ')),
          null,
          null,
          Em.I18n.t('services.service.restartAll.confirmButton')
      );
    } else {
      return null;
    }
  },

  /**
   * Send request restart host components from hostComponentsToRestart
   * @returns {$.ajax}
   */
  restartHostComponents: function () {
    App.ajax.send({
      name: 'restart.staleConfigs',
      sender: this,
      success: 'restartAllRequiredSuccessCallback'
    });
  },

  /**
   * Success callback for restartAllRequired
   */
  restartAllRequiredSuccessCallback: function () {
    // load data (if we need to show this background operations popup) from persist
    App.router.get('userSettingsController').dataLoading('show_bg').done(function (initValue) {
      if (initValue) {
        App.router.get('backgroundOperationsController').showPopup();
      }
    });
  }
});
