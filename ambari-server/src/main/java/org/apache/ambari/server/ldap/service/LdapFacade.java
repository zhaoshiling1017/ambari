/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ambari.server.ldap.service;

import java.util.Map;

import org.apache.ambari.server.ldap.AmbariLdapConfiguration;

/**
 * The contract defining all the operations required by the application when communicating with an arbitrary LDAP server.
 * This interface is intended to decouple LDAP specific details from the application.
 */
public interface LdapFacade {

  /**
   * Tests the connection to the LDAP server based on the provided configuration.
   *
   * @param ambariLdapConfiguration the available ldap related configuration
   * @throws AmbariLdapException if the connection fails or other problems occur during the operation
   */
  void checkConnection(AmbariLdapConfiguration ambariLdapConfiguration) throws AmbariLdapException;


  /**
   * Runs the user and group attribute detection algorithms
   *
   * @param ambariLdapConfiguration
   */
  void detectAttributes(AmbariLdapConfiguration ambariLdapConfiguration);

  /**
   * Checks user and group related LDAP configuration attributes in the configuration object with the help of the provided parameters
   *
   * @param parameters              a map of property name and value pairs holding information to facilitate checking the attributes
   * @param ambariLdapConfiguration configutration instance with available attributes
   * @throws AmbariLdapException if the attribute checking fails
   */
  void checkLdapAttibutes(Map<String, Object> parameters, AmbariLdapConfiguration ambariLdapConfiguration) throws AmbariLdapException;
}
