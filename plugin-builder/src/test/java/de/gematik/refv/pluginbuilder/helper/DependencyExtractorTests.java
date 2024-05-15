/*
Copyright (c) 2023-2024 gematik GmbH

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
package de.gematik.refv.pluginbuilder.helper;

import de.gematik.refv.commons.configuration.PackageReference;
import de.gematik.refv.pluginbuilder.exceptions.DependencyExtractionException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        List<PackageReference> dependencies = dependencyExtractor.getAllDependenciesFromPackageJson("src/test/resources/package/");
        PackageReference packageReference = new PackageReference("test-dependency", "1.0.0");
        assertTrue(dependencies.contains(packageReference));
    }

    @Test
    @SneakyThrows
    void testNoDependenciesFromPackageJson() {
        List<PackageReference> dependencies = dependencyExtractor.getAllDependenciesFromPackageJson("src/test/resources/package-with-no-dependencies/");
        assertTrue(dependencies.isEmpty());
    }

    @Test
    @SneakyThrows
    void testGetAllDependenciesFromPackageJsonThrowsException() {
        assertThrows(DependencyExtractionException.class, () -> dependencyExtractor.getAllDependenciesFromPackageJson("src/test/resources/corrupted-package/"));
    }
}
