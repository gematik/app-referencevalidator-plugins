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

import de.gematik.refv.pluginbuilder.exceptions.DependencyExtractionException;
import de.gematik.refv.plugins.configuration.FhirPackage;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DependencyExtractorTests {

    private DependencyExtractor dependencyExtractor;

    @BeforeEach
    void setUp() {
        dependencyExtractor = new DependencyExtractor();
    }

    @Test
    @SneakyThrows
    void testGetAllDependenciesFromPackageJson() {
        List<FhirPackage> dependencies = dependencyExtractor.getAllDependenciesFromPackageJson(List.of(new File("src/test/resources/package/minimalvalidationmodule.test-1.0.0.tgz")));
        FhirPackage fhirPackage = new FhirPackage("test-dependency", "1.0.0");
        assertTrue(dependencies.contains(fhirPackage));
    }

    @Test
    @SneakyThrows
    void testNoDependenciesFromPackageJson() {
        List<FhirPackage> dependencies = dependencyExtractor.getAllDependenciesFromPackageJson(List.of(new File("src/test/resources/package-with-no-dependencies/minimalvalidationmodule.test-1.0.0.tgz")));
        assertTrue(dependencies.isEmpty());
    }

    @Test
    @SneakyThrows
    void testGetAllDependenciesFromPackageJsonThrowsException() {
        assertThrows(DependencyExtractionException.class, () -> dependencyExtractor.getAllDependenciesFromPackageJson(List.of(new File("src/test/resources/corrupted-package/minimalvalidationmodule.test-1.0.0-corrupted-package-json.tgz"))));
    }
}
