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
 * Usage:
 * <pre>
 *   {{#view App.CollapsibleView}}
 *     <a {{action "toggleCollapse" target="view"}}>My Title</a>
 *     <div class="collapse">
 *      {{! some content }}
 *     </div>
 *   {{/view}}
 * </pre>
 * <b>IMPORTANT!</b>
 *  1. Collapsible content has to be wrapped with <code>.collapse</code>
 *  2. Link/button with action "toggleCollapse" targeted to the view has to be in the block-template
 *
 * @type {Em.View}
 */
App.CollapsibleView = Em.View.extend({

  layoutName: require('templates/common/collapsible'),

  /**
   * Determines if content-panel should be opened initially
   *
   * @type {boolean}
   * @default false
   */
  openOnInit: false,

  /**
   * Flag shows if content-panel is expanded or not
   *
   * @type {boolean}
   * @default false
   */
  expanded: false,

  content: null,

  didInsertElement: function () {
    if (this.get('openOnInit') || this.get('content.expanded')) {
      this.$('.collapse:first').collapse('show');
      this.set('expanded', true);
      var content = this.get('content');
      if (content) {
        Em.set(content, 'expanded', true);
      }
    }
  },

  willDestroyElement: function () {
    this.$('.collapse:first').off();
  },

  toggleCollapse: function () {
    this.set('expanded', !this.$('.collapse:first').hasClass('in'));
    var content = this.get('content');
    if (content) {
      Em.set(content, 'expanded', this.get('expanded'));
    }
    this.$('.collapse:first').collapse('toggle');
    return false;
  }

});