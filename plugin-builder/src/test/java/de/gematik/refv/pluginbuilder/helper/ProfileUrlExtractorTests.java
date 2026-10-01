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

package de.gematik.refv.pluginbuilder.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.refv.plugins.configuration.FhirPackage;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProfileUrlExtractorTests {

    private static final String PACKAGE_FOLDER_PATH = "src/test/resources/package/";
    private static final String EXPECTED_PROFILE_URL =
            "http://example.gematik.de/fhir/StructureDefinition/some-profile|1.0.0";

    @Test
    void testGetAllPluginProfileUrls() throws IOException {
        List<String> profileUrls =
                ProfileUrlExtractor.getAllPluginProfileUrls(
                        PACKAGE_FOLDER_PATH, new FhirPackage("minimalvalidationmodule.test", "1.0.0"));
        assertTrue(
                profileUrls.contains(
                        "http://example.gematik.de/fhir/StructureDefinition/patient-with-birthdate|1.0.0"));
    }

    @Test
    void testNonJsonEntriesAreSkipped() throws IOException {
        List<String> profileUrls =
                ProfileUrlExtractor.getAllPluginProfileUrls(
                        PACKAGE_FOLDER_PATH, new FhirPackage("non.json.entries.test", "1.0.0"));
        assertEquals(List.of(EXPECTED_PROFILE_URL), profileUrls);
    }

    @Test
    void testJsonEntryWithByteOrderMarkIsParsed() throws IOException {
        List<String> profileUrls =
                ProfileUrlExtractor.getAllPluginProfileUrls(
                        PACKAGE_FOLDER_PATH, new FhirPackage("bom.entry.test", "1.0.0"));
        assertEquals(List.of(EXPECTED_PROFILE_URL), profileUrls);
    }
}