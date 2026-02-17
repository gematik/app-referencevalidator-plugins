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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.gematik.refv.plugins.configuration.DependencyList;
import de.gematik.refv.plugins.configuration.FhirPackage;
import de.gematik.refv.plugins.configuration.v2.PluginDefinition;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

/**
 * Handles the extraction of profile URLs of profiles that are part of the plugin, so they can be
 * provided for the <code>PluginTester</code>.
 */
@Slf4j
@UtilityClass
public class ProfileUrlExtractor {

  private static final JsonFactory jsonfactory = new JsonFactory();

  /**
   * Get all plugin profile urls of a fhir package.
   *
   * @param packageFolderPath The folder path that contains the fhir packages of a plugin.
   * @param fhirPackage The fhir package that should be scanned for its profile urls.
   * @return Returns a list of the profile urls.
   * @throws IOException If the IO actions to scan the fhir package for profile urls fail.
   */
  public static List<String> getAllPluginProfileUrls(
      String packageFolderPath, FhirPackage fhirPackage) throws IOException {
    String packageFilename =
        String.format("%s-%s.tgz", fhirPackage.getName(), fhirPackage.getVersion());
    return findAllProfileUrlsIn(packageFolderPath + packageFilename);
  }

  public static List<String> getAllPluginProfileUrlsFromInputStream(InputStream inputStream)
      throws IOException {
    List<String> profileUrls = new ArrayList<>();

    // Read all contents into memory
    byte[] zipData;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        baos.write(buffer, 0, bytesRead);
      }
      zipData = baos.toByteArray();
    }

    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipData);
        ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(byteArrayInputStream)) {

      var fhirPackages = getFhirPackageReferenceFromInputStream(zipInputStream);
      for (var fhirPackage : fhirPackages) {
        ZipArchiveInputStream resetZipInputStream =
            new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry;
        while ((entry = resetZipInputStream.getNextEntry()) != null) {
          if (entry.getName().equals("package/" + fhirPackage.toFilename())) {
            List<String> packageProfileUrls =
                extractProfileUrlsFromFhirPackage(resetZipInputStream);
            profileUrls.addAll(packageProfileUrls);
            break;
          }
        }
      }
    }

    return profileUrls;
  }

  private List<FhirPackage> getFhirPackageReferenceFromInputStream(
      ZipArchiveInputStream zipInputStream) throws IOException {
    String pluginDefinitionAsString = null;
    ZipArchiveEntry entry;
    while ((entry = zipInputStream.getNextEntry()) != null) {
      if (entry.getName().equals("config.yaml")) {
        StringBuilder yamlContentBuilder = new StringBuilder();
        try (BufferedReader reader =
            new BufferedReader(new InputStreamReader(zipInputStream, StandardCharsets.UTF_8))) {
          String line;
          while ((line = reader.readLine()) != null) {
            yamlContentBuilder.append(line).append("\n");
          }
        }
        pluginDefinitionAsString = yamlContentBuilder.toString();
        break;
      }
    }

    if (pluginDefinitionAsString == null) {
      throw new FileNotFoundException("config.yaml not found in the provided input stream");
    }

    ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
    PluginDefinition pluginDefinition =
        objectMapper.readValue(pluginDefinitionAsString, PluginDefinition.class);
    return pluginDefinition.getValidation().getDependencyLists().stream()
        .map(DependencyList::getFhirPackage)
        .collect(Collectors.toList());
  }

  private static List<String> findProfileUrlsInTarArchive(TarArchiveInputStream tarInputStream)
      throws IOException {
    List<String> profileUrls = new ArrayList<>();
    TarArchiveEntry currentEntry;
    while ((currentEntry = tarInputStream.getNextEntry()) != null) {
      if (!currentEntry.isDirectory()) {
        String jsonString =
            buildJsonString(new BufferedReader(new InputStreamReader(tarInputStream)));
        if (isStructureDefinition(jsonString) && isResource(jsonString)) {
          String url = parseJsonFieldAsString(jsonString, "url");
          String profileVersion = parseJsonFieldAsString(jsonString, "version");
          String profileUrl = String.format("%s|%s", url, profileVersion);
          profileUrls.add(profileUrl);
        }
      }
    }
    return profileUrls;
  }

  private static List<String> extractProfileUrlsFromFhirPackage(InputStream inputStream)
      throws IOException {
    List<String> profileUrls;
    try (TarArchiveInputStream tarInputStream =
        new TarArchiveInputStream(new GzipCompressorInputStream(inputStream))) {
      profileUrls = findProfileUrlsInTarArchive(tarInputStream);
    }
    return profileUrls;
  }

  private static List<String> findAllProfileUrlsIn(String packageFilenamePath) throws IOException {
    List<String> profileUrls;
    try (TarArchiveInputStream tarInputStream =
        new TarArchiveInputStream(
            new GzipCompressorInputStream(new FileInputStream(packageFilenamePath)))) {
      profileUrls = findProfileUrlsInTarArchive(tarInputStream);
    }
    return profileUrls;
  }

  /**
   * Checks if a given json resource is a StructureDefinition or not.
   *
   * @param jsonString The json resource as a String.
   * @return Returns true if it is a StructureDefinition. Else false.
   */
  private static boolean isStructureDefinition(String jsonString) throws IOException {
    String resourceTypeAsString = parseJsonFieldAsString(jsonString, "resourceType");
    return resourceTypeAsString.equals("StructureDefinition");
  }

  /**
   * Checks if a given json resource is of the kind "resource" or not.
   *
   * @param jsonString The json resource as a String.
   * @return Returns true if it is of the kind "resource". Else false.
   */
  private static boolean isResource(String jsonString) throws IOException {
    String resourceTypeAsString = parseJsonFieldAsString(jsonString, "kind");
    return resourceTypeAsString.equals("resource");
  }

  /**
   * Parses the given json resource for a specified field and gets its value.
   *
   * @param jsonString The json resource as a String.
   * @param wantedFieldName The wanted json field name.
   * @return The field value if its found. Else empty String ("").
   * @throws IOException in case of parse errors
   */
  private static String parseJsonFieldAsString(String jsonString, String wantedFieldName)
      throws IOException {
    try (JsonParser jsonParser = jsonfactory.createParser(jsonString)) {
      jsonParser.nextToken();
      while (jsonParser.hasCurrentToken()) {
        String fieldName = jsonParser.currentName();
        jsonParser.nextToken();
        if ("extension".equals(fieldName)) jsonParser.skipChildren();
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
