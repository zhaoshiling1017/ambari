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

function getModel() {
  return App.ServiceGroup.createRecord();
}

describe('App.ServiceGroup', function () {

  App.TestAliases.testAsComputedOr(getModel(), 'isStartAllDisabled', ['isStartStopAllClicked', '!someServiceIsStopped']);

  App.TestAliases.testAsComputedOr(getModel(), 'isStopAllDisabled', ['isStartStopAllClicked', '!someServiceIsStarted']);

  App.TestAliases.testAsComputedEveryBy(getModel(), 'isRestartAllRequiredDisabled', 'services', 'isRestartRequired', false);

  App.TestAliases.testAsComputedSomeBy(getModel(), 'someServiceIsStarted', 'services', 'healthStatus', 'green');

  App.TestAliases.testAsComputedEveryBy(getModel(), 'allServicesAreStarted', 'services', 'healthStatus', 'green');

  App.TestAliases.testAsComputedNotEqual(getModel(), 'isStartStopAllClicked', 'App.router.backgroundOperationsController.allOperationsCount', 0);

  App.TestAliases.testAsComputedEqual(getModel(), 'isCoreServiceGroup', 'serviceGroupName', 'CORE');

  App.TestAliases.testAsComputedSumBy(getModel(), 'alertsCount', 'services', 'alertsCount');

  App.TestAliases.testAsComputedEqual(getModel(), 'isStopped', 'desiredState', 'INSTALLED');

  App.TestAliases.testAsComputedEqual(getModel(), 'isStarted', 'desiredState', 'STARTED');

});
