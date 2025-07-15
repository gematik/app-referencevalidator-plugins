/*-
 * #%L
 * plugin-builder
 * %%
 * Copyright (C) 2025 gematik GmbH
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
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 * #L%
 */

package de.gematik.refv.pluginbuilder.helper;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.EncodingEnum;
import de.gematik.refv.plugins.configuration.FhirPackage;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestedProfileTrackerTests {

    private TestedProfileTracker testedProfileTracker;

    @BeforeEach
    @SneakyThrows
    void setUp() {
        String packageFolderPath = "src/test/resources/package/";
        FhirPackage packageReference = new FhirPackage("minimalvalidationmodule.test", "1.0.0");
        testedProfileTracker = new TestedProfileTracker(packageFolderPath, List.of(packageReference));
    }

    @Test
    @SneakyThrows
    void testTrackProfiles() {
        File file = new File("src/test/resources/multiple-referenced-profiles.json");
        String fileContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        IParser parser = EncodingEnum.detectEncoding(fileContent).newParser(FhirContext.forR4());
        IBaseResource resource = parser.parseResource(fileContent);
        testedProfileTracker.trackProfiles(resource, true);
        assertThat(testedProfileTracker.getValidProfileUrlsFoundInTestFiles())
                .hasSize(3)
                .contains("https://referenced-profile1")
                .contains("https://referenced-profile2")
                .contains("https://referenced-profile3");
    }
}
