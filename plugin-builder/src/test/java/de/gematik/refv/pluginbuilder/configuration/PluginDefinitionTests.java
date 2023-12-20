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

import de.gematik.refv.commons.configuration.PackageReference;
import de.gematik.refv.commons.configuration.ValidationMessageTransformation;
import de.gematik.refv.plugins.configuration.MalformedPackageDeclarationException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginDefinitionTests {

    private PluginDefinitionWrapper pluginDefinition;

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        String testConfigString = "src/test/resources/plugin-definition.yaml";
        pluginDefinition = PluginDefinitionWrapper.createFrom(testConfigString);
    }

    @Test
    @SneakyThrows
    void testReadConfigFile() {
        // Überprüfe, ob die Deserialisierung korrekt war
        assertEquals("1.0", pluginDefinition.getConfigSpecVersion());
        assertEquals("test", pluginDefinition.getId());
        assertEquals("1.0.0", pluginDefinition.getVersion());

        // Überprüfe die Validation-Eigenschaften
        var validation = pluginDefinition.getValidation();
        assertNotNull(validation);
        assertEquals("test-package#1.0.0", validation.getFhirPackage());
        assertEquals("test-dependency#1.0.0", validation.getDependencies().get(0));
        assertFalse(validation.isErrorOnUnknownProfile());
        assertTrue(validation.isAnyExtensionsAllowed());
        assertEquals("xml", validation.getAcceptedEncodings().get(0));

        // Überprüfe validationMessageTransformations
        ValidationMessageTransformation messageTransformation = validation.getValidationMessageTransformations().get(0);
        assertNotNull(messageTransformation);
        assertEquals("error", messageTransformation.getSeverityLevelFrom());
        assertEquals("information", messageTransformation.getSeverityLevelTo());
        assertEquals("test-locator", messageTransformation.getLocatorString());
        assertEquals("test-message-id", messageTransformation.getMessageId());

        // Überprüfe ignoredCodeSystems und ignoredValueSets
        assertTrue(validation.getIgnoredCodeSystems().contains("http://ignored/codesystem"));
        assertTrue(validation.getIgnoredValueSets().contains("http://ignored/valueset"));

        // Überprüfe snapshotDependencies
        List<String> snapshotDependencies = pluginDefinition.getSnapshotDependencies();
        assertNotNull(snapshotDependencies);
        assertEquals("test-snapshot-dependency#1.0.0", snapshotDependencies.get(0));
    }

    @Test
    @SneakyThrows
    void testReadConfigThrowsFileNotFoundException() {
        Assertions.assertThrows(FileNotFoundException.class, () ->
                PluginDefinitionWrapper.createFrom("non-existent-file-path"));
    }

    @Test
    @SneakyThrows
    void testGetValidationFhirPackageAsPackageReference() {
        PackageReference test = new PackageReference("test-package", "1.0.0");
        assertEquals(test, pluginDefinition.getValidationFhirPackageAsPackageReference());
    }

    @Test
    @SneakyThrows
    void testGetSnapshotDependenciesAsPackageReferences() {
        PackageReference test = new PackageReference("test-snapshot-dependency", "1.0.0");
        List<PackageReference> snapshotDependenciesAsPackageReferences = pluginDefinition.getSnapshotDependenciesAsPackageReferences();
        assertTrue(snapshotDependenciesAsPackageReferences.contains(test));
    }

    @Test
    @SneakyThrows
    void testGetSnapshotDependenciesAsFilenames() {
        String test = "test-snapshot-dependency-1.0.0.tgz";
        List<String> snapshotDependenciesAsFilenames = pluginDefinition.getSnapshotDependenciesAsFilenames();
        assertTrue(snapshotDependenciesAsFilenames.contains(test));
    }

    @Test
    @SneakyThrows
    void testGetSnapshotDependenciesAsPackageReferencesThrowsMalformedPackageDeclarationException() {
        String testConfigString = "src/test/resources/ConfigData-with-malformed-package-declaration.yaml";
        PluginDefinitionWrapper data = PluginDefinitionWrapper.createFrom(testConfigString);
        Assertions.assertThrows(MalformedPackageDeclarationException.class, data::getSnapshotDependenciesAsPackageReferences);
    }

    @Test
    @SneakyThrows
    void testGetSnapshotDependenciesAsFilenamesThrowsMalformedPackageDeclarationException() {
        String testConfigString = "src/test/resources/ConfigData-with-malformed-package-declaration.yaml";
        PluginDefinitionWrapper data = PluginDefinitionWrapper.createFrom(testConfigString);
        Assertions.assertThrows(MalformedPackageDeclarationException.class, data::getSnapshotDependenciesAsFilenames);
    }

    @Test
    @SneakyThrows
    void testGetValidationDependenciesAsPackageReferences() {
        PackageReference test = new PackageReference("test-dependency", "1.0.0");
        List<PackageReference> validationDependenciesAsPackageReferences = pluginDefinition.getValidationDependenciesAsPackageReferences();
        assertTrue(validationDependenciesAsPackageReferences.contains(test));
    }

    @Test
    @SneakyThrows
    void testGetValidationDependenciesAsPackageReferencesThrowsMalformedPackageDeclarationException() {
        String testConfigString = "src/test/resources/ConfigData-with-malformed-package-declaration.yaml";
        PluginDefinitionWrapper data = PluginDefinitionWrapper.createFrom(testConfigString);
        Assertions.assertThrows(MalformedPackageDeclarationException.class, data::getValidationDependenciesAsPackageReferences);
    }
}
