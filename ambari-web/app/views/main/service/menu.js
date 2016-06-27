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
var misc = require('utils/misc');

App.MainServiceMenuView = Em.CollectionView.extend({

  content: function () {
    var serviceGroup = this.get('serviceGroup');
    return serviceGroup ? misc.sortByOrder(App.StackService.find().mapProperty('serviceName'), serviceGroup.get('services').toArray()) : [];
  }.property('serviceGroup'),

  didInsertElement:function () {
    App.router.location.addObserver('lastSetURL', this, 'renderOnRoute');
    this.renderOnRoute();
    App.tooltip(this.$(".restart-required-service"), {html:true, placement:"right"});
  },

  willDestroyElement: function() {
    App.router.location.removeObserver('lastSetURL', this, 'renderOnRoute');
    this.$(".restart-required-service").tooltip('destroy');
  },

  activeServiceId: null,

  /**
   *    Syncs navigation menu with requested URL
   */
  renderOnRoute:function () {
    var lastUrl = App.router.location.lastSetURL || location.href.replace(/^[^#]*#/, '');
    if (lastUrl.substr(1, 4) !== 'main' || !this._childViews) {
      return;
    }
    var reg = /^\/main\/services\/(\S+)\//g;
    var subUrl = reg.exec(lastUrl);
    var serviceId = null != subUrl ? subUrl[1] : 1;
    this.set('activeServiceId', serviceId);
  },

  tagName:'ul',
  classNames:[ "nav", "nav-list", "nav-services"],

  itemViewClass:Em.View.extend({

    classNameBindings: ["active", "clients"],
    templateName: require('templates/main/service/menu_item'),
    restartRequiredMessage: null,

    shouldBeRestarted: Em.computed.someBy('content.hostComponents', 'staleConfigs', true),

    active:function () {
      return this.get('content.id') == this.get('parentView.activeServiceId') ? 'active' : '';
    }.property('content.id', 'parentView.activeServiceId'),

    alertsCount: function () {
      return this.get('content.alertsCount') > 99 ? "99+" : this.get('content.alertsCount') ;
    }.property('content.alertsCount'),

    hasCriticalAlerts: Em.computed.alias('content.hasCriticalAlerts'),

    isConfigurable: function () {
      return !App.get('services.noConfigTypes').contains(this.get('content.serviceName'));
    }.property('App.services.noConfigTypes','content.serviceName'),

    goToConfigs: function () {
      App.router.set('mainServiceItemController.routeToConfigs', true);
      App.router.transitionTo('services.service.configs', this.get('content'));
      App.router.set('mainServiceItemController.routeToConfigs', false);
    },

    link: function() {
      if (this.get('controller.name') && this.get('controller.name') == "mainAssembliesController" ) {
        return "javascript: void(0)";
      }
      var stateName = (['summary','configs'].contains(App.router.get('currentState.name')))
        ? this.get('isConfigurable') && this.get('parentView.activeServiceId') != this.get('content.id') ?  App.router.get('currentState.name') : 'summary' : 'summary';
      return "#/main/services/" + this.get('content.id') + "/" + stateName;
    }.property('App.router.currentState.name', 'parentView.activeServiceId', 'isConfigurable'),

    refreshRestartRequiredMessage: function() {
      var restarted, componentsCount, hostsCount, message, tHosts, tComponents;
      restarted = this.get('content.restartRequiredHostsAndComponents');
      componentsCount = 0;
      hostsCount = 0;
      message = '';
      for (var host in restarted) {
        hostsCount++;
        componentsCount += restarted[host].length;
      }
      tHosts = hostsCount > 1 ? Em.I18n.t('common.hosts') : Em.I18n.t('common.host');
      tComponents = componentsCount > 1 ? Em.I18n.t('common.components') : Em.I18n.t('common.component');
      message += componentsCount + ' ' + tComponents + ' ' + Em.I18n.t('on') + ' ' +
        hostsCount + ' ' + tHosts + ' ' + Em.I18n.t('services.service.config.restartService.needToRestartEnd');
      this.set('restartRequiredMessage', message);
    }.observes('content.restartRequiredHostsAndComponents')
  })

});

App.TopNavServiceMenuView = Em.CollectionView.extend({

  content: function () {
    if (!App.router.get('clusterController.isLoaded')) {
      return [];
    }
    var serviceGroup = App.ServiceGroup.find('CORE');
    return misc.sortByOrder(App.StackService.find().mapProperty('serviceName'), serviceGroup.get('services').toArray());
  }.property('App.router.clusterController.isLoaded'),

  didInsertElement:function () {
    App.router.location.addObserver('lastSetURL', this, 'renderOnRoute');
    this.renderOnRoute();
    App.tooltip(this.$(".restart-required-service"), {html:true, placement:"right"});
  },

  willDestroyElement: function() {
    App.router.location.removeObserver('lastSetURL', this, 'renderOnRoute');
    this.$(".restart-required-service").tooltip('destroy');
  },

  activeServiceId:null,
  /**
   *    Syncs navigation menu with requested URL
   */
  renderOnRoute:function () {
    var lastUrl = App.router.location.lastSetURL || location.href.replace(/^[^#]*#/, '');
    if (lastUrl.substr(1, 4) !== 'main' || !this._childViews) {
      return;
    }
    var reg = /^\/main\/services\/(\S+)\//g;
    var subUrl = reg.exec(lastUrl);
    var serviceId = null != subUrl ? subUrl[1] : 1;
    this.set('activeServiceId', serviceId);
  },

  tagName:'ul',
  classNames:[ "top-nav-dropdown-menu"],

  itemViewClass:Em.View.extend({

    classNameBindings:["active", "clients"],
    templateName:require('templates/main/service/menu_item'),
    restartRequiredMessage: null,

    shouldBeRestarted: Em.computed.someBy('content.hostComponents', 'staleConfigs', true),

    active:function () {
      return this.get('content.id') == this.get('parentView.activeServiceId') ? 'active' : '';
    }.property('parentView.activeServiceId'),

    alertsCount: Em.computed.alias('content.alertsCount'),

    hasCriticalAlerts: Em.computed.alias('content.hasCriticalAlerts'),

    isConfigurable: function () {
      return !App.get('services.noConfigTypes').contains(this.get('content.serviceName'));
    }.property('App.services.noConfigTypes','content.serviceName'),

    link: function() {
      var stateName = ['summary','configs'].contains(App.router.get('currentState.name'))
        ? this.get('isConfigurable') ? App.router.get('currentState.name') : 'summary'
        : 'summary';
      return "#/main/services/" + this.get('content.id') + "/" + stateName;
    }.property('App.router.currentState.name', 'parentView.activeServiceId','isConfigurable'),

    goToConfigs: function () {
      App.router.set('mainServiceItemController.routeToConfigs', true);
      App.router.transitionTo('services.service.configs', this.get('content'));
      App.router.set('mainServiceItemController.routeToConfigs', false);
    },

    refreshRestartRequiredMessage: function() {
      var restarted, componentsCount, hostsCount, message, tHosts, tComponents;
      restarted = this.get('content.restartRequiredHostsAndComponents');
      componentsCount = 0;
      hostsCount = 0;
      message = '';
      for (var host in restarted) {
        hostsCount++;
        componentsCount += restarted[host].length;
      }
      tHosts = hostsCount > 1 ? Em.I18n.t('common.hosts') : Em.I18n.t('common.host');
      tComponents = componentsCount > 1 ? Em.I18n.t('common.components') : Em.I18n.t('common.component');
      message += componentsCount + ' ' + tComponents + ' ' + Em.I18n.t('on') + ' ' +
        hostsCount + ' ' + tHosts + ' ' + Em.I18n.t('services.service.config.restartService.needToRestartEnd');
      this.set('restartRequiredMessage', message);
    }.observes('content.restartRequiredHostsAndComponents')
  })

});
