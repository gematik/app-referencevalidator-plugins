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

import de.gematik.refv.commons.configuration.PackageReference;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the extraction of profile URLs of profiles that are part of the plugin, so they can be provided for the <code>PluginTester</code>.
 */
@Slf4j
@UtilityClass
public class ProfileUrlExtractor {

    private static final JsonFactory jsonfactory = new JsonFactory();

    /**
     * Get all plugin profile urls of a fhir package.
     * @param packageFolderPath The folder path that contains the fhir packages of a plugin.
     * @param fhirPackage The fhir package that should be scanned for its profile urls.
     * @return Returns a list of the profile urls.
     * @throws IOException If the IO actions to scan the fhir package for profile urls fail.
     */
    public static List<String> getAllPluginProfileUrls(String packageFolderPath, PackageReference fhirPackage) throws IOException {
        String packageFilename = String.format("%s-%s.tgz", fhirPackage.getPackageName(), fhirPackage.getPackageVersion());
        return findAllProfileUrlsIn(packageFolderPath + packageFilename);
    }

    /**
     * Reads all the profile urls from a given fhir package.
     * @param packageFilenamePath File path to the fhir package that should be scanned for its profile urls.
     * @return List of profile urls.
     * @throws IOException If the IO actions to scan the fhir package for profile urls fail.
     */
    private static List<String> findAllProfileUrlsIn(String packageFilenamePath) throws IOException {
        List<String> profileUrls = new ArrayList<>();
        try (TarArchiveInputStream tarInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(packageFilenamePath)));
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(tarInputStream))) {
            TarArchiveEntry currentEntry = tarInputStream.getNextEntry();
            while (currentEntry != null) {
                String jsonString = buildJsonString(bufferedReader);
                if(isStructureDefinition(jsonString) && isResource(jsonString)) {

                    String url = parseJsonFieldAsString(jsonString, "url");
                    String profileVersion = parseJsonFieldAsString(jsonString, "version");
                    String profileUrl = String.format("%s|%s", url, profileVersion);
                    profileUrls.add(profileUrl);

                }
                currentEntry = tarInputStream.getNextEntry();
            }
        }
        return profileUrls;
    }

    /**
     * Checks if a given json resource is a StructureDefinition or not.
     * @param jsonString The json resource as a String.
     * @return Returns true if it is a StructureDefinition. Else false.
     */
    private static boolean isStructureDefinition(String jsonString) throws IOException {
        String resourceTypeAsString = parseJsonFieldAsString(jsonString, "resourceType");
        return resourceTypeAsString.equals("StructureDefinition");
    }

    /**
     * Checks if a given json resource is of the kind "resource" or not.
     * @param jsonString The json resource as a String.
     * @return Returns true if it is of the kind "resource". Else false.
     */
    private static boolean isResource(String jsonString) throws IOException {
        String resourceTypeAsString = parseJsonFieldAsString(jsonString, "kind");
        return resourceTypeAsString.equals("resource");
    }

    /**
     * Parses the given json resource for a specified field and gets its value.
     * @param jsonString The json resource as a String.
     * @param wantedFieldName The wanted json field name.
     * @return The field value if its found. Else empty String ("").
     * @throws IOException in case of parse errors
     */
    private static String parseJsonFieldAsString(String jsonString, String wantedFieldName) throws IOException {
        try (JsonParser jsonParser = jsonfactory.createParser(jsonString)) {
            jsonParser.nextToken();
            while (jsonParser.hasCurrentToken()) {
                String fieldName = jsonParser.getCurrentName();
                jsonParser.nextToken();
                if("extension".equals(fieldName))
                    jsonParser.skipChildren();
                if (wantedFieldName.equals(fieldName) && jsonParser.hasTextCharacters())
                    return jsonParser.getText();
            }
        }
        return "";
    }

    /**
     * @param bufferedReader The BufferedReader of the resource.
     * @return Returns the read resource as a String.
     */
    private static String buildJsonString(BufferedReader bufferedReader) {
        return bufferedReader.lines().collect(Collectors.joining());
    }
}
