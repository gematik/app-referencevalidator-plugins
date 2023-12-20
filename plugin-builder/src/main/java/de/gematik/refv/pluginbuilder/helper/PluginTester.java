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
package de.gematik.refv.pluginbuilder.helper;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.EncodingEnum;
import de.gematik.refv.Plugin;
import de.gematik.refv.ValidationModuleFactory;
import de.gematik.refv.commons.Profile;
import de.gematik.refv.commons.configuration.PackageReference;
import de.gematik.refv.commons.exceptions.ValidationModuleInitializationException;
import de.gematik.refv.commons.validation.ValidationModule;
import de.gematik.refv.commons.validation.ValidationResult;
import de.gematik.refv.pluginbuilder.exceptions.PluginTestFailedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the testing process during the <code>PluginBuilder</code> build process and provides users with information about the test results and also about missing test resources for profiles that are part of their plugin.
*/
@Slf4j
public class PluginTester {

    private final List<String> allProfileUrls;
    private final Map<String, ValidationResult> failingTests = new HashMap<>();
    private final List<String> testedValidPluginProfileUrls = new ArrayList<>();
    private final List<String> testedInvalidPluginProfileUrls = new ArrayList<>();
    private final ValidationModuleFactory validationModuleFactory = new ValidationModuleFactory();

    public PluginTester(String packageFolderPath, PackageReference validationFhirPackageReference) throws IOException {
        this.allProfileUrls = ProfileUrlExtractor.getAllPluginProfileUrls(packageFolderPath, validationFhirPackageReference);
    }

    /**
     * Handles the testing of a given plugin.
     * @param pluginFileToTest The File object of the plugin that should be tested.
     * @return Returns true if the plugin was tested successfully. False if not.
     * @throws IOException If the creation of the ValidationModule fails or if the processing of test files fails.
     * @throws PluginTestFailedException If test-files folder is missing, if not at least one valid/invalid test exists in test-files folder or if any test failed.
     * @throws ValidationModuleInitializationException If the initialization of the ValidationModule fails.
     */
    public boolean testPlugin(File pluginFileToTest) throws IOException, PluginTestFailedException, ValidationModuleInitializationException {
        log.info("Started testing plugin: '{}'", pluginFileToTest.getName());
        Plugin plugin = Plugin.createFromZipFile(new java.util.zip.ZipFile(pluginFileToTest));
        ValidationModule validationModule = validationModuleFactory.createValidationModuleFromPlugin(plugin);

        processTestFiles(pluginFileToTest, validationModule);

        if(testedValidPluginProfileUrls.isEmpty() || testedInvalidPluginProfileUrls.isEmpty()) {
            throw new PluginTestFailedException("You must add at least one valid and one invalid test resource to your test-files directory!");
        }

        logMissingExamples();

        if (!failingTests.isEmpty()) {
            logFailingTests();
            return false;
        }

        log.info("Finished testing plugin '{}' successfully.", pluginFileToTest.getName());
        return true;
    }

    /**
     * Logs information for the user about profiles in the plugin where a test case is missing.
     */
    private void logMissingExamples() {
        List<String> missingProfilesValid = getMissingProfiles(true);
        List<String> missingProfilesInvalid = getMissingProfiles(false);
        if(!missingProfilesValid.isEmpty() || !missingProfilesInvalid.isEmpty()){
            log.warn("Some of the profiles in your plugin have no corresponding test-files (valid missing: {} / {} | invalid missing: {} / {}):", missingProfilesValid.size(), allProfileUrls.size(), missingProfilesInvalid.size(), allProfileUrls.size());
            for (String profile : missingProfilesValid) {
                log.warn("No valid test example found for : {}", profile);
            }
            for (String profile : missingProfilesInvalid) {
                log.warn("No invalid test example found for : {}", profile);
            }
        }
    }

    /**
     * Gets a list of profiles where test cases are missing.
     * @param validity The validity (valid = true, invalid = false)
     * @return Returns a list of profiles where test cases are missing.
     */
    private List<String> getMissingProfiles(boolean validity) {
        List<String> missingProfiles = new ArrayList<>();
        List<String> testedProfiles = validity ? testedValidPluginProfileUrls : testedInvalidPluginProfileUrls;

        for (String profileString : allProfileUrls) {
            Profile profile = Profile.parse(profileString);
            if (!testedProfiles.contains(profile.getBaseCanonical())) {
                missingProfiles.add(profile.getBaseCanonical());
            }
        }

        return missingProfiles;
    }

    /**
     * Logs information about failing test cases.
     */
    private void logFailingTests() {
        log.error("There were issues with some of your test files. See more info below:");
        for (Map.Entry<String, ValidationResult> entry : failingTests.entrySet()) {
            String fileName = entry.getKey();
            ValidationResult result = entry.getValue();
            boolean expectedValidity = fileName.contains("/valid/");
            log.error("File: {} | Expected validity: {} | Validation result: {}", fileName, expectedValidity, result);
        }
    }

    /**
     * Processes the test-files folder and validates them.
     * @param zipFile The plugin file.
     * @param validationModule The ValidationModule used for validating the test-files.
     * @throws IOException If IO actions with plugin zipFile fail.
     * @throws PluginTestFailedException If a test case fails.
     */
    private void processTestFiles(File zipFile, ValidationModule validationModule) throws IOException, PluginTestFailedException {
        boolean testFilesDirExists = false;
        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<? extends ZipArchiveEntry> fileEntries = zip.getEntries();
            while (fileEntries.hasMoreElements()) {
                ZipArchiveEntry fileEntry = fileEntries.nextElement();
                if (!fileEntry.isDirectory() && fileEntry.getName().startsWith("test-files/")) {
                    testFilesDirExists = true;
                    processTestFile(zip, fileEntry, validationModule);
                }
            }
        }
        if(!testFilesDirExists)
            throw new PluginTestFailedException("Test files directory is missing in " + zipFile.getName());
    }

    /**
     * Processes a single test case from the test-files folder.
     * @param zip The plugin ZipFile.
     * @param fileEntry The current zip entry representing a test case.
     * @param validationModule The ValidationModule used for validating the test-files.
     * @throws IOException If IO actions with the zip entries fail.
     */
    private void processTestFile(ZipFile zip, ZipArchiveEntry fileEntry, ValidationModule validationModule) throws IOException {
        try (InputStream inputStream = zip.getInputStream(fileEntry)) {
            String fileContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

            ValidationResult result = validationModule.validateString(fileContent);
            String fileName = fileEntry.getName();
            boolean isValid = result.isValid();
            boolean expectedValidity = fileName.contains("/valid/");

            IParser parser = EncodingEnum.detectEncoding(fileContent).newParser(FhirContext.forR4());
            IBaseResource resource = parser.parseResource(fileContent);
            var profile = resource.getMeta().getProfile().get(0);

            if(profile.hasValue() && expectedValidity){
                String profileUrl = getProfileValueAsString(profile);
                testedValidPluginProfileUrls.add(profileUrl);
            }
            if(profile.hasValue() && !expectedValidity) {
                String profileUrl = getProfileValueAsString(profile);
                testedInvalidPluginProfileUrls.add(profileUrl);
            }

            if (isValid == expectedValidity) {
                log.info("Test for {} succeeded!", fileName);
            } else {
                log.error("Test for {} failed. Expected validity: {} | Validation result: {} ", fileName, expectedValidity, isValid);
                failingTests.put(fileName, result);
            }
        }
    }

    /**
     * @param profile The profile.
     * @return Returns the baseCanonical of the profile.
     */
    private String getProfileValueAsString(IPrimitiveType<String> profile) {
        String profileAsString = profile.getValueAsString();
        return Profile.parse(profileAsString).getBaseCanonical();
    }
}
