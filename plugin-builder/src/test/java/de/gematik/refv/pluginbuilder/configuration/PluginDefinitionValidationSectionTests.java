/*-
 * #%L
 * plugin-builder
 * %%
 * Copyright (C) 2025 - 2026 gematik GmbH
 * %%
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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes
 * by gematik, find details in the "Readme" file.
 * #L%
 */

package de.gematik.refv.pluginbuilder.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.refv.commons.configuration.ValidationMessageTransformation;
import java.util.List;
import org.junit.jupiter.api.Test;

class PluginDefinitionValidationSectionTests {

  @Test
  void testAllArgsConstructorAndGetters() {
    PluginDefinitionValidationSection pluginDefinitionValidationSection =
        new PluginDefinitionValidationSection(
            "fhirPackage",
            List.of("dep1", "dep2"),
            true,
            true,
            List.of("encoding1", "encoding2"),
            List.of(new ValidationMessageTransformation("", "", "", "", "")),
            List.of("codeSystem1", "codeSystem2"),
            List.of("valueSet1", "valueSet2"));

    assertEquals("fhirPackage", pluginDefinitionValidationSection.getFhirPackage());
    assertEquals(List.of("dep1", "dep2"), pluginDefinitionValidationSection.getDependencies());
    assertTrue(pluginDefinitionValidationSection.isErrorOnUnknownProfile());
    assertTrue(pluginDefinitionValidationSection.isAnyExtensionsAllowed());
    assertEquals(
        List.of("encoding1", "encoding2"),
        pluginDefinitionValidationSection.getAcceptedEncodings());
    assertEquals(
        List.of(new ValidationMessageTransformation("", "", "", "", "")),
        pluginDefinitionValidationSection.getValidationMessageTransformations());
    assertEquals(
        List.of("codeSystem1", "codeSystem2"),
        pluginDefinitionValidationSection.getIgnoredCodeSystems());
    assertEquals(
        List.of("valueSet1", "valueSet2"), pluginDefinitionValidationSection.getIgnoredValueSets());
  }
}
