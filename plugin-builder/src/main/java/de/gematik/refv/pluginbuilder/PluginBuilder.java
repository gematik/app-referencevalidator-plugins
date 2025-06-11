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

package de.gematik.refv.pluginbuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.gematik.refv.commons.configuration.PackageReference;
import de.gematik.refv.commons.exceptions.ValidationModuleInitializationException;
import de.gematik.refv.pluginbuilder.configuration.PluginDefinitionWrapper;
import de.gematik.refv.pluginbuilder.exceptions.DependencyExtractionException;
import de.gematik.refv.pluginbuilder.exceptions.DownloadFailedException;
import de.gematik.refv.pluginbuilder.exceptions.PluginIdentifierException;
import de.gematik.refv.pluginbuilder.exceptions.PluginTestFailedException;
import de.gematik.refv.pluginbuilder.helper.FhirPackageDownloader;
import de.gematik.refv.pluginbuilder.helper.FileNameToPackageNameHelper;
import de.gematik.refv.pluginbuilder.helper.PluginTester;
import de.gematik.refv.pluginbuilder.helper.PluginZipper;
import de.gematik.refv.plugins.configuration.MalformedPackageDeclarationException;
import de.gematik.fhir.snapshots.SnapshotGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles the building of plugins. Keeps track of already existing fhir-packages inside a given plugin definition.
 * Combines the functionalities of <code>FhirPackageDownloader</code>, <code>DependencyExtractor</code>,
 * <code>ProfileUrlExtractor</code>, <code>PluginZipper</code>, <code>ConfigurationDataHandler</code> to enable the complete
 * build process of a new plugin.
*/
@Slf4j
public class PluginBuilder {

    private final List<String> existingFhirPackages = new ArrayList<>();
    private final FhirPackageDownloader fhirPackageDownloader;
    private final PluginZipper pluginZipper;
    private PluginDefinitionWrapper pluginDefinition;
    private String temporaryExtractionFolder;
    private String packageFolderPath;
    private String patchesFolderPath;

    private static final String SRC_PACKAGE_DIR_NAME = "src-packages";
    private static final String TEST_FILES_DIR_NAME = "test-files";

    public PluginBuilder(FhirPackageDownloader fhirPackageDownloader, PluginZipper pluginZipper) {
        this.fhirPackageDownloader = fhirPackageDownloader;
        this.pluginZipper = pluginZipper;
    }

    /**
     * Builds a plugin based on the specified plugin definition directory path and puts it in the target folder.
     * Initializes paths, copies the plugin directory, finds existing FHIR packages, reads the configuration file,
     * downloads FHIR packages, generates snapshots, tests the plugin, and creates the final plugin.
     *
     * @param pluginDefinitionDirectoryPath The path to the plugin definition directory.
     * @param targetFolderPath The target folder path for storing the built plugin.
     * @return The path to the built plugin.
     * @throws IOException If an I/O error occurs.
     * @throws DownloadFailedException If the download process is unsuccessful.
     * @throws PluginTestFailedException If the plugin testing process fails.
     * @throws MalformedPackageDeclarationException If the FHIR-package declarations in config.yaml do not follow the correct pattern: packageName#packageVersion.
     * @throws PluginIdentifierException If the specified ID inside PluginConfigurationData is equal to one of the IDs of the embedded validation modules from the reference validator.
     * @throws ValidationModuleInitializationException If anything goes wrong during the initialization of ValidationModule while testing the plugin.
     * @throws DependencyExtractionException If anything goes wrong during file I/O operations while extracting the dependencies for the current plugin.
     */
    public PluginBuilderResult buildPlugin(String pluginDefinitionDirectoryPath, String targetFolderPath) throws IOException, DownloadFailedException, PluginTestFailedException, MalformedPackageDeclarationException, PluginIdentifierException, ValidationModuleInitializationException, DependencyExtractionException {
        File pluginDefinitionDirectory = new File(pluginDefinitionDirectoryPath);
        if (!pluginDefinitionDirectory.exists()) {
            throw new IllegalArgumentException(String.format("No such file or directory: %s", pluginDefinitionDirectoryPath));
        }
        if (!pluginDefinitionDirectory.isDirectory()) {
            throw new IllegalArgumentException("Please only pass directories for plugin building!");
        }

        initializePaths(targetFolderPath);
        copyPluginDirectory(pluginDefinitionDirectory);
        pluginDefinition = PluginDefinitionWrapper.createFrom(getConfigFilePath());
        findExistingFhirPackagesAndAddToThePluginDefinition(packageFolderPath);
        String pluginName = pluginDefinition.getId();
        String pluginVersion = pluginDefinition.getVersion();
        log.info("Started building plugin: {}", pluginName);

        try {
            // Download Fhir Packages
            fhirPackageDownloader.download(packageFolderPath, pluginDefinition);
            log.info("Finished downloading packages for: {}", pluginName);

            // Snapshot Generation
            movePatchesToPackageFolder(patchesFolderPath, packageFolderPath);
            generateSnapshots(packageFolderPath);
            writeCompleteValidationDependenciesList();

            // Plugin Testing
            List<String> excludedFilesAndFolders = pluginDefinition.getSnapshotDependenciesAsFilenames();
            excludedFilesAndFolders.add(SRC_PACKAGE_DIR_NAME);
            var temporaryPluginFile = pluginZipper.zipPlugin(pluginDefinition.getId(), pluginDefinition.getVersion(), temporaryExtractionFolder, targetFolderPath, excludedFilesAndFolders);
            PackageReference validationFhirPackageReference = pluginDefinition.getValidationFhirPackageAsPackageReference();
            PluginTester pluginTester = new PluginTester(packageFolderPath, validationFhirPackageReference);
            boolean isTestedSuccessful = pluginTester.testPlugin(temporaryPluginFile);

            if (!isTestedSuccessful) {
                log.error("Testing of plugin '{}' was unsuccessful! See more info above.", pluginName);
            }

            Files.delete(temporaryPluginFile.toPath());

            // Create Final Plugin
            excludedFilesAndFolders.add(TEST_FILES_DIR_NAME);
            File finalPlugin = pluginZipper.zipPlugin(pluginName, pluginVersion, temporaryExtractionFolder, targetFolderPath, excludedFilesAndFolders);
            log.info("Finished building plugin '{}' at {}", pluginName, targetFolderPath);
            return new PluginBuilderResult(isTestedSuccessful, finalPlugin.getAbsolutePath());
        } finally {
            // Cleanup
            try {
                FileUtils.deleteDirectory(new File(temporaryExtractionFolder));
            } catch (Exception e) {
                log.error("Could not delete temporary extraction folder", e);
            }
        }
    }

    private String getConfigFilePath() {
        return temporaryExtractionFolder + File.separator + "config.yaml";
    }

    private void writeCompleteValidationDependenciesList() throws IOException {
        var validationDependencies = pluginDefinition.getDependenciesForDependencyList().stream()
                .filter(d -> !d.equals(pluginDefinition.getValidationFhirPackageAsFilename()))
                .map(FileNameToPackageNameHelper::fileNameToPackageName)
                .collect(Collectors.toList());

        pluginDefinition.getValidation().setDependencies(validationDependencies);

        ObjectMapper objectMapper = new YAMLMapper();
        Map<String, Object> configYaml = objectMapper.readValue(new File(getConfigFilePath()),
                new TypeReference<>() {
                });
        var validationSection = (Map<String, Object>) configYaml.get("validation");
        var validationDependenciesSection = (List<String>) validationSection.get("dependencies");
        if(validationDependenciesSection == null) {
            validationDependenciesSection = new LinkedList<>();
            validationSection.put("dependencies", validationDependenciesSection);
        }
        validationDependenciesSection.clear();
        validationDependenciesSection.addAll(validationDependencies);
        objectMapper.writeValue(new File(getConfigFilePath()), configYaml);
    }

    /**
     * Initializes paths for the plugin build process.
     *
     * @param targetFolderPath The target folder path for storing the built plugin.
     */
    private void initializePaths(String targetFolderPath) {
        temporaryExtractionFolder = targetFolderPath + File.separator + UUID.randomUUID();
        packageFolderPath = temporaryExtractionFolder + File.separator + SRC_PACKAGE_DIR_NAME + File.separator;
        patchesFolderPath = temporaryExtractionFolder + File.separator + "patches" + File.separator;
    }

    /**
     * Copies the plugin directory to the temporary extraction folder.
     *
     * @param dir The plugin definition directory.
     * @throws IOException If an I/O error occurs during the copy operation.
     */
    private void copyPluginDirectory(File dir) throws IOException {
        FileUtils.copyDirectory(dir, new File(temporaryExtractionFolder));
    }

    /**
     * Moves patches to the package folder.
     *
     * @param patchesFolderPath The path to the patches folder.
     * @param packageFolderPath The path to the package folder.
     * @throws IOException If an I/O error occurs during the move operation.
     */
    private void movePatchesToPackageFolder(String patchesFolderPath, String packageFolderPath) throws IOException {
        File packageFolder = new File(packageFolderPath);
        File patchesFolder = new File(patchesFolderPath);
        if(!patchesFolder.exists()) {
            return;
        }
        Files.move(patchesFolder.toPath(), packageFolder.toPath().resolve(patchesFolder.getName()),
                StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Generates snapshots for the package folder.
     *
     * @param packageFolderPath The path to the package folder.
     * @throws IOException If an I/O error occurs during snapshot generation.
     * @throws MalformedPackageDeclarationException If a package declaration inside PluginDefinition does not follow the correct pattern: packageName#packageVersion.
     */
    private void generateSnapshots(String packageFolderPath) throws IOException, MalformedPackageDeclarationException {
        SnapshotGenerator snapshotGenerator = new SnapshotGenerator();
        snapshotGenerator.generateSnapshots(packageFolderPath, packageFolderPath.replace(SRC_PACKAGE_DIR_NAME, "package"), "");
    }

    /**
     * Retrieves already existing FHIR package filenames and adds them to the PluginDefinition.
     *
     * @param packageFolderPath The path to the package folder.
     */
    private void findExistingFhirPackagesAndAddToThePluginDefinition(String packageFolderPath) {
        existingFhirPackages.addAll(findExistingFilenames(packageFolderPath));
        pluginDefinition.getDependenciesForDependencyList().addAll(existingFhirPackages);
    }

    /**
     * Finds existing filenames in the specified folder path.
     *
     * @param folderPath The folder path to search for existing filenames.
     * @return The list of existing filenames.
     */
    private List<String> findExistingFilenames(String folderPath) {
        List<String> list = new ArrayList<>();
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    list.add(file.getName());
                }
            }
        }
        return list;
    }
}
