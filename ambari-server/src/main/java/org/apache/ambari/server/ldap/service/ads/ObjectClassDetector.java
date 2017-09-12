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

package org.apache.ambari.server.ldap.service.ads;

import java.util.Map;

import javax.inject.Inject;

import org.apache.directory.api.ldap.model.entry.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class ObjectClassDetector extends OccurranceAndWeightBasedDetector {

  private static final Logger LOGGER = LoggerFactory.getLogger(ObjectClassDetector.class);
  private Map<String, Integer> occurranceMap = Maps.newHashMap();
  private Map<String, Integer> weightsMap = Maps.newHashMap();


  private enum ObjectClassValue {
    PERSON("person", 1),
    POSIX_ACCOUNT("posixAccount", 1);

    private String ocVal;
    private Integer weight;

    ObjectClassValue(String attr, Integer weght) {
      this.ocVal = attr;
      this.weight = weght;
    }

    Integer weight() {
      return this.weight;
    }

    String ocVal() {
      return this.ocVal;
    }

  }

  @Inject
  public ObjectClassDetector() {
    for (ObjectClassValue ocVal : ObjectClassValue.values()) {
      occurranceMap.put(ocVal.ocVal(), 0);
      weightsMap.put(ocVal.ocVal(), ocVal.weight());
    }

  }

  @Override
  protected Map<String, Integer> occurranceMap() {
    return occurranceMap;
  }

  @Override
  protected Map<String, Integer> weightsMap() {
    return weightsMap;
  }

  @Override
  protected boolean applies(Entry entry, String value) {
    LOGGER.info("Checking for object class [{}] in entry [{}]", value, entry.getDn());
    return entry.hasObjectClass(value);
  }

}
