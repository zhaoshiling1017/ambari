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
var validator = require('utils/validator');

App.DeployStoreAppView = Em.View.extend({

  templateName: require('templates/main/assemblies/deploy_store_app/config'),

  config: null,

  observeDisable: function() {
    this.get('parentView').$('.sliderBar').slider('option', 'disabled', this.get('isDeploying'));
  }.observes('isDeploying'),

  didInsertElement: function() {
    this.$('.sliderBar').slider({
      step: this.get('config.step'),
      min: this.get('config.minValue'),
      max: this.get('config.maxValue'),
      value: this.get('config.defaultValue'),
      slide: function( event, ui ) {
        this.set('config.value', ui.value);
        this.set('config.errorMessage', '');
      }.bind(this)
    });
  },

  sliderInputView: Em.TextField.extend({
    disabled: Em.computed.alias('parentView.isDeploying'),
    keyUp: function() {
      if (validator.isValidInt(this.get('value'))) {
        if (this.get('value') > this.get('parentView.config.maxValue')) {
          this.set('value', this.get('parentView.config.maxValue'));
        } else if (this.get('value') < this.get('parentView.config.minValue')) {
          this.set('value', this.get('parentView.config.minValue'));
        }
        this.get('parentView').$('.sliderBar').slider('value', this.get('value'));
        this.set('parentView.config.errorMessage', '');
      } else {
        this.set('parentView.config.errorMessage', Em.I18n.t('errorMessage.config.number.integer'));
      }
    }
  })

});