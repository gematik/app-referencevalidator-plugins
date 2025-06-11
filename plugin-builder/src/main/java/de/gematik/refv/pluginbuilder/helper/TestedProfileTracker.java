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

import de.gematik.refv.commons.Profile;
import de.gematik.refv.commons.configuration.PackageReference;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Slf4j
public class TestedProfileTracker {

    private List<String> allProfileUrls;
    private final List<String> validProfileUrlsFoundInTestFiles = new ArrayList<>();
    private final List<String> invalidProfileUrlsFoundInTestFiles = new ArrayList<>();
    private final String packageFolderPath;
    private final InputStream pluginInputStream;
    private final PackageReference validationFhirPackageReference;

    public TestedProfileTracker(String packageFolderPath, PackageReference validationFhirPackageReference) {
        this.packageFolderPath = packageFolderPath;
        this.pluginInputStream = null;
        this.validationFhirPackageReference = validationFhirPackageReference;
    }

    public TestedProfileTracker(InputStream pluginInputStream) {
        this.pluginInputStream = pluginInputStream;
        this.packageFolderPath = null;
        this.validationFhirPackageReference = null;
    }

    /**
     * Tracks the profiles associated with the provided resource and updates the lists of tested profiles accordingly.
     * If the expected validity is true, the profiles are added to the list of tested valid plugin profile URLs,
     * otherwise, they are added to the list of tested invalid plugin profile URLs.
     *
     * @param resource          The resource to track profiles for.
     * @param expectedValidity The expected validity of the resource against the profiles.
     */
    public void trackProfiles(IBaseResource resource, boolean expectedValidity) {
        List<? extends IPrimitiveType<String>> profiles = resource.getMeta().getProfile();
        List<String> profilesAsString = profiles.stream()
                .map(this::getProfileValueAsString)
                .collect(Collectors.toList());

        if (!profilesAsString.isEmpty()) {
            if (expectedValidity)
                validProfileUrlsFoundInTestFiles.addAll(profilesAsString);
            if (!expectedValidity)
                invalidProfileUrlsFoundInTestFiles.addAll(profilesAsString);
        }
    }

    public void logMissingTestCaseExamples() throws IOException {
        if (packageFolderPath != null) {
            assert validationFhirPackageReference != null;
            allProfileUrls = ProfileUrlExtractor.getAllPluginProfileUrls(packageFolderPath, validationFhirPackageReference);
        } else {
            assert pluginInputStream != null;
            allProfileUrls = ProfileUrlExtractor.getAllPluginProfileUrlsFromInputStream(pluginInputStream);
        }
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
        List<String> testedProfiles = validity ? validProfileUrlsFoundInTestFiles : invalidProfileUrlsFoundInTestFiles;

        for (String profileString : allProfileUrls) {
            Profile profile = Profile.parse(profileString);
            if (!testedProfiles.contains(profile.getBaseCanonical())) {
                missingProfiles.add(profile.getBaseCanonical());
            }
        }

        return missingProfiles;
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
