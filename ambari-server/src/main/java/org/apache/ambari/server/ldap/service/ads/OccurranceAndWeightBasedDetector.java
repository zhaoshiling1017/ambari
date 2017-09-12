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

import org.apache.ambari.server.ldap.service.AttributeDetector;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OccurranceAndWeightBasedDetector implements AttributeDetector<Entry> {

  private static final Logger LOGGER = LoggerFactory.getLogger(OccurranceAndWeightBasedDetector.class);

  protected abstract Map<String, Integer> occurranceMap();

  protected abstract Map<String, Integer> weightsMap();

  protected abstract boolean applies(Entry entry, String value);

  @Override
  public String detect() {
    Map.Entry<String, Integer> selectedEntry = null;

    for (Map.Entry<String, Integer> entry : occurranceMap().entrySet()) {
      if (selectedEntry == null) {

        selectedEntry = entry;
        LOGGER.info("Initial name attribute: {}", selectedEntry);
        continue;

      }

      if (selectedEntry.getValue() < entry.getValue()) {

        LOGGER.info("Changing potential name attribute from : [{}] to: [{}]", selectedEntry, entry);
        selectedEntry = entry;

      }
    }
    return selectedEntry.getKey();
  }

  @Override
  public void collect(Entry entry) {
    LOGGER.info("Detecting ldap attributes/values ...");

    for (String attributeValue : occurranceMap().keySet()) {
      if (applies(entry, attributeValue)) {

        Integer cnt = occurranceMap().get(attributeValue).intValue();
        if (weightsMap().containsKey(attributeValue)) {
          cnt = cnt + weightsMap().get(attributeValue);
        } else {
          cnt = cnt + 1;
        }
        occurranceMap().put(attributeValue, cnt);

        LOGGER.info("Collected potential name attr: {}, count: {}", attributeValue, cnt);

      } else {
        LOGGER.info("The result entry doesn't contain the attribute: [{}]", attributeValue);
      }
    }
  }


}
