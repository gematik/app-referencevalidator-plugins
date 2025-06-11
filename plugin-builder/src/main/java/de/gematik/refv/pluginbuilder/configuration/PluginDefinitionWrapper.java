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

package de.gematik.refv.pluginbuilder.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.gematik.refv.SupportedValidationModule;
import de.gematik.refv.commons.configuration.PackageReference;
import de.gematik.refv.pluginbuilder.exceptions.PluginIdentifierException;
import de.gematik.refv.plugins.configuration.MalformedPackageDeclarationException;
import de.gematik.refv.plugins.configuration.PluginDefinition;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Handles the <code>PluginDefinition</code> data defined in <code>config.yaml</code> during the <code>PluginBuilder</code> build process. Provides methods for interacting with <code>PluginDefintion</code>.
 */
@FieldNameConstants
public class PluginDefinitionWrapper extends PluginDefinition {

    @Getter
    private List<String> dependenciesForDependencyList = new LinkedList<>();
    private static final String MALFORMED_PACKAGE_DECLARATION_TEXT = "The declared package in config.yaml '%s' does not conform to the pattern: packageName#packageVersion";
    private static final String PACKAGE_FILENAME_FORMATTER = "%s-%s.tgz";

    public static PluginDefinitionWrapper createFrom(InputStream config) throws IOException, PluginIdentifierException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

        try(config) {
            PluginDefinitionWrapper configData = objectMapper.readValue(config, PluginDefinitionWrapper.class);
            var isSupportedModuleConflict = SupportedValidationModule.fromString(configData.getId()).isPresent();
            if(isSupportedModuleConflict)
                throw new PluginIdentifierException("Plugin ID " + configData.getId() + " is already in use. Please choose another one for the plugin");

            return configData;
        }
    }

    public static PluginDefinitionWrapper createFrom(String configFilePath) throws IOException, PluginIdentifierException {
        File configFile = new File(configFilePath);

        if(!configFile.exists()) {
            throw new FileNotFoundException("config.yaml is missing");
        }

        return PluginDefinitionWrapper.createFrom(new FileInputStream(configFile));
    }

    /**
     * @return Returns the <code>validation.fhirPackage</code> defined in the config.yaml as a PackageReference
     * @throws MalformedPackageDeclarationException If a package declaration inside of <code>PluginDefinition</code> does not follow the correct pattern: <code>packageName#packageVersion</code>
     */
    public PackageReference getValidationFhirPackageAsPackageReference() throws MalformedPackageDeclarationException {
        String validationFhirPackage = this.getValidation().getFhirPackage();
        String[] validationFhirPackageNameAndVersion = validationFhirPackage.split("#");
        if(validationFhirPackageNameAndVersion.length < 2) {
            throw new MalformedPackageDeclarationException(String.format(MALFORMED_PACKAGE_DECLARATION_TEXT, validationFhirPackage));
        }
        return new PackageReference(validationFhirPackageNameAndVersion[0], validationFhirPackageNameAndVersion[1]);
    }

    /**
     * @return Returns the <code>snapshotDependencies</code> defined in the config.yaml as a list of PackageReferences
     * @throws MalformedPackageDeclarationException If a package declaration inside of <code>PluginDefinition</code> does not follow the correct pattern: <code>packageName#packageVersion</code>
     */
    public List<PackageReference> getSnapshotDependenciesAsPackageReferences() throws MalformedPackageDeclarationException {
        return getAsPackageReferences(this.getSnapshotDependencies());
    }

    /**
     * @return Returns the <code>snapshotDependencies</code> defined in the config.yaml as a list of filenames in the format name-version.tgz
     * @throws MalformedPackageDeclarationException If a package declaration inside of <code>PluginDefinition</code> does not follow the correct pattern: <code>packageName#packageVersion</code>
     */
    public List<String> getSnapshotDependenciesAsFilenames() throws MalformedPackageDeclarationException {
        return getAsFilenames(this.getSnapshotDependencies());
    }

    /**
     * @return Returns the <code>validation.dependencies</code> defined in the config.yaml as a list of PackageReferences
     * @throws MalformedPackageDeclarationException If a package declaration inside of <code>PluginDefinition</code> does not follow the correct pattern: <code>packageName#packageVersion</code>
     */
    public List<PackageReference> getValidationDependenciesAsPackageReferences() throws MalformedPackageDeclarationException {
        return getAsPackageReferences(this.getValidation().getDependencies());
    }

    /**
     * @param list The list of fhir package Strings
     * @return Returns a list of filenames in the format name-version.tgz
     * @throws MalformedPackageDeclarationException If a package declaration inside of <code>PluginDefinition</code> does not follow the correct pattern: <code>packageName#packageVersion</code>
     */
    private List<String> getAsFilenames(List<String> list) throws MalformedPackageDeclarationException {
        if(list == null)
            return new LinkedList<>();

        List<String> result = new ArrayList<>();
        for(String s : list) {
            String[] split = s.split("#");
            if(split.length < 2) {
                throw new MalformedPackageDeclarationException(String.format(MALFORMED_PACKAGE_DECLARATION_TEXT, s));
            }
            String filename = String.format(PACKAGE_FILENAME_FORMATTER, split[0], split[1]);
            result.add(filename);
        }
        return result;
    }

    /**
     * @param list The list of fhir package Strings
     * @return Returns a list of PackageReferences
     * @throws MalformedPackageDeclarationException If a package declaration inside of <code>PluginDefinition</code> does not follow the correct pattern: <code>packageName#packageVersion</code>
     */
    private List<PackageReference> getAsPackageReferences(List<String> list) throws MalformedPackageDeclarationException {
        if(list == null)
            return new LinkedList<>();

        List<PackageReference> result = new ArrayList<>();
        for(String s : list) {
            String[] split = s.split("#");
            if(split.length < 2) {
                throw new MalformedPackageDeclarationException(String.format(MALFORMED_PACKAGE_DECLARATION_TEXT, s));
            }
            PackageReference pr = new PackageReference(split[0], split[1]);
            result.add(pr);
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
