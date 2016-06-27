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

App.ServiceGroup = DS.Model.extend({
  clusterName: DS.attr('string'),
  serviceGroupName: DS.attr('string'),
  serviceGroupDisplayName: DS.attr('string'),
  serviceGroupType: DS.attr('string'),
  currentState: DS.attr('string'),
  desiredState: DS.attr('string'),
  assemblyFile: DS.attr('string'),
  quicklinks: DS.attr('object'),
  applicationId: DS.attr('string'),
  lifetime: DS.attr('string'),
  services: DS.hasMany('App.Service'),
  isActive: false,

  containers: DS.attr('object'),

  businessImpact: {
    fraudTransactions: 46,
    fraudCost: '23,000'
  },

  configurations: function() {
    var serviceMetaData = App.ServiceGroup.metadata[this.get('serviceGroupDisplayName')];
    if (serviceMetaData && serviceMetaData.configurations) {
      return serviceMetaData.configurations;
    } else {
      return App.ServiceGroup.metadata["Credit Fraud Detection"].configurations;
    }
  }.property('id'),

  description : function() {
    var serviceMetaData = App.ServiceGroup.metadata[this.get('serviceGroupDisplayName')];
    if (serviceMetaData) {
      return serviceMetaData.description;
    } else {
      return Em.I18n.t('assembly.manage.summary.custom.description');
    }
  }.property('id'),

  /**
   * @type {string}
   */
  serviceGroupNameLower: function () {
    return (this.get('serviceGroupName') || '').toLowerCase();
  }.property('serviceGroupName'),

  /**
   * @type {string}
   */
  uri: Em.computed.format('/services/v1/applications/{0}', 'serviceGroupNameLower'),

  /**
   * @type {number}
   */
  alertsCount: Em.computed.sumBy('services', 'alertsCount'),

  /**
   * @type {number|string}
   */
  shownAlertsCount: function () {
    var alertsCount = this.get('alertsCount');
    return alertsCount > 99 ? '99+' : alertsCount;
  }.property('alertsCount'),

  /**
   * @type {boolean}
   */
  isStopped: Em.computed.equal('desiredState', 'INSTALLED'),

  /**
   * @type {boolean}
   */
  isStarted: Em.computed.equal('desiredState', 'STARTED'),

  /**
   * Should "Start All"-button be disabled
   * @type {bool}
   */
  isStartAllDisabled: Em.computed.or('isStartStopAllClicked', '!someServiceIsStopped'),

  /**
   * @type {boolean}
   */
  someServiceIsStopped: function () {
    return this.get('services').filter(function (_service) {
        return _service.get('healthStatus') === 'red' && !App.get('services.clientOnly').contains(_service.get('serviceName'));
      }).length > 0;
  }.property('services.@each.healthStatus'),

  /**
   * Should "Stop All"-button be disabled
   * @type {bool}
   */
  isStopAllDisabled: Em.computed.or('isStartStopAllClicked', '!someServiceIsStarted'),

  /**
   * @type {boolean}
   */
  someServiceIsStarted: Em.computed.someBy('services', 'healthStatus', 'green'),

  /**
   * @type {boolean}
   */
  allServicesAreStarted: Em.computed.everyBy('services', 'healthStatus', 'green'),

  /**
   * Should "Refresh All"-button be disabled
   * @type {bool}
   */
  isRestartAllRequiredDisabled: Em.computed.everyBy('services', 'isRestartRequired', false),

  /**
   * @type {bool}
   */
  isStartStopAllClicked: Em.computed.notEqual('App.router.backgroundOperationsController.allOperationsCount', 0),

  /**
   * Check if all services are installed
   * true - all installed, false - not all
   * @type {bool}
   */
  isAllServicesInstalled: function () {
    var sLength = this.get('services.content.length');
    if (!sLength) return false;
    var availableServices = App.StackService.find().mapProperty('serviceName');
    return sLength === availableServices.length;
  }.property('services.[]', 'services.content.length'),

  /**
   * Check if Core service group
   * true - all installed, false - not all
   * @type {bool}
   */
  isCoreServiceGroup: Em.computed.equal('serviceGroupName', 'CORE'),

  /**
   * Check if CF-MONITOR service group
   * true - all installed, false - not all
   * @type {bool}
   */
  isCfMonitorServiceGroup: Em.computed.equal('serviceGroupDisplayName', 'Credit Fraud Detection'),

  /**
   * @type {boolean}
   */
  someConfigurationInvalid: function () {
    return this.get('configurations').someProperty('errorMessage');
  }.property('configurations.@each.errorMessage'),

  init: function () {
    this._super();
    var configurations = this.get('configurations');
    this.set('initConfigurations', JSON.parse(JSON.stringify(configurations)));
  },

  assemblyLinks: function() {
    var quickLinks = [];
    var yarnService = App.YARNService.find().objectAt(0);
    if (yarnService) {
      var activeRm = yarnService.get('hostComponents').filterProperty('componentName', 'RESOURCEMANAGER').findProperty('haStatus', 'ACTIVE');
      if (activeRm) {
        var activeRmHostName = activeRm.get('hostName');
        var applicationLink = 'http://' + activeRmHostName + ':8088/proxy/' + this.get('applicationId') ;
        var YarnLinkObject = {
          label: Em.I18n.t('assembly.manage.summary.yarn.ui.quick.link'),
          url: applicationLink
        };
        quickLinks.pushObject(YarnLinkObject);
      }
    }
    if (this.get('isCfMonitorServiceGroup')) {
      var grafanaLinkObject = {
        label: Em.I18n.t('assembly.manage.summary.grafana.quick.link'),
        url: 'http://cn005.l42scl.hortonworks.com:3000/dashboard/db/cf-monitor'
      };
      quickLinks.pushObject(grafanaLinkObject);
    }
    var componentLinks = this.get('quicklinks');
    if (!Em.isNone(componentLinks)) {
      Object.keys(componentLinks).forEach(function (key) {
        var value = componentLinks[key];
        quickLinks.pushObject({
          label: key,
          url: value
        });
      }, this);
    }
    return quickLinks;
  }.property('App.router.clusterController.isServiceMetricsLoaded', 'serviceGroupName')

});

App.ServiceGroup.FIXTURES = [];

App.ServiceGroup.displayOrder = [
  'Credit Fraud Detection',
  'Logsearch Stable'
];

App.ServiceGroup.metadata = {
  "Credit Fraud Detection": {
    description: Em.I18n.t('assembly.manage.summary.description'),
    configurations: [
      {
        name: Em.I18n.t('assemblies.app.deploy.popup.config.maxTransactions'),
        minValue: 100,
        maxValue: 100000,
        step: 100,
        defaultValue: 76000,
        value: 76000
      },
      {
        name: Em.I18n.t('assemblies.app.deploy.popup.config.maxAnalysts'),
        minValue: 100,
        maxValue: 100000,
        step: 100,
        defaultValue: 300,
        value: 300
      }
    ]
  },
  "Logsearch Stable": {
    description: Em.I18n.t('assembly.manage.summary.logsearch.description'),
    configurations: [
      {
        name: Em.I18n.t('assemblies.app.deploy.popup.logsearh.config.SourceComponents'),
        minValue: 1,
        maxValue: 10000,
        step: 100,
        defaultValue: 1000,
        value: 1000
      },
      {
        name: Em.I18n.t('assemblies.app.deploy.popup.logsearch.config.logEvents'),
        minValue: 100,
        maxValue: 300000,
        step: 100,
        defaultValue: 150000,
        value: 150000
      }
    ]
  },
  "HBase Stable": {
    description: Em.I18n.t('assembly.manage.summary.hbase.description')
  },
  "Vinod's Zookeeper": {
    description: Em.I18n.t('assembly.manage.summary.zookeeper.description')
  }
};
