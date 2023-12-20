/*
Copyright (c) 2023 gematik GmbH

Licensed under the Apache License, Version 2.0 (the License);
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an 'AS IS' BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package de.gematik.refv.pluginbuilder.configuration;

import de.gematik.refv.commons.configuration.ValidationMessageTransformation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationTests {

    @Test
    void testAllArgsConstructorAndGetters() {
        Validation validation = new Validation("fhirPackage", List.of("dep1", "dep2"),
                true, true, List.of("encoding1", "encoding2"), List.of(new ValidationMessageTransformation("", "", "", "")),
                List.of("codeSystem1", "codeSystem2"), List.of("valueSet1", "valueSet2"));

        assertEquals("fhirPackage", validation.getFhirPackage());
        assertEquals(List.of("dep1", "dep2"), validation.getDependencies());
        assertTrue(validation.isErrorOnUnknownProfile());
        assertTrue(validation.isAnyExtensionsAllowed());
        assertEquals(List.of("encoding1", "encoding2"), validation.getAcceptedEncodings());
        assertEquals(List.of(new ValidationMessageTransformation("", "", "", "")), validation.getValidationMessageTransformations());
        assertEquals(List.of("codeSystem1", "codeSystem2"), validation.getIgnoredCodeSystems());
        assertEquals(List.of("valueSet1", "valueSet2"), validation.getIgnoredValueSets());
    }
}
