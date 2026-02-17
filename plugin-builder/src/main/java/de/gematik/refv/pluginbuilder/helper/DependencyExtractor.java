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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.refv.pluginbuilder.exceptions.DependencyExtractionException;
import de.gematik.refv.plugins.configuration.FhirPackage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

/**
 * Handles the extraction of dependencies either from the <code>PluginConfigurationData</code> or
 * from the <code>package.json</code> defined inside a fhir package.
 */
@Slf4j
@NoArgsConstructor
public class DependencyExtractor {

  /**
   * Gets the dependencies from the package.json of all packages defined at packageFolderPath.
   *
   * @return A list of all dependencies as FhirPackages
   * @throws DependencyExtractionException If anything goes wrong during the dependency extraction
   *     process.
   */
  public List<FhirPackage> getAllDependenciesFromPackageJson(List<File> packageFiles)
      throws DependencyExtractionException {
    List<FhirPackage> allDependencies = new ArrayList<>();
    for (File packageFile : packageFiles) {
      List<FhirPackage> dependencies = getDependenciesFor(packageFile);
      allDependencies.addAll(dependencies);
    }
    return allDependencies;
  }

  /**
   * Gets all dependencies defined in a package.json.
   *
   * @param tgzFile The path to the fhir package from which the dependencies should be extracted.
   * @return A list of all dependencies as FhirPackages
   * @throws DependencyExtractionException If anything goes wrong during the dependency extraction
   *     process.
   */
  private List<FhirPackage> getDependenciesFor(File tgzFile) throws DependencyExtractionException {
    List<FhirPackage> dependencies = new ArrayList<>();
    try (FileInputStream fileInputStream = new FileInputStream(tgzFile);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        GzipCompressorInputStream gzipCompressorInputStream =
            new GzipCompressorInputStream(bufferedInputStream);
        TarArchiveInputStream tarInputStream =
            new TarArchiveInputStream(gzipCompressorInputStream)) {

      TarArchiveEntry entry;
      while ((entry = tarInputStream.getNextEntry()) != null) {
        if (entry.getName().equals("package/package.json")) {
          String jsonContent = new String(tarInputStream.readAllBytes());
          ObjectMapper objectMapper = new ObjectMapper();
          JsonNode jsonNode = objectMapper.readTree(jsonContent);
          JsonNode dependenciesNode = jsonNode.get("dependencies");

          if (dependenciesNode != null) {
            dependenciesNode
                .fields()
                .forEachRemaining(
                    dependencyEntry -> {
                      String packageName = dependencyEntry.getKey();
                      String version = dependencyEntry.getValue().asText();
                      if (!packageName.equals("hl7.fhir.r4.core")) {
                        dependencies.add(new FhirPackage(packageName, version));
                      }
                    });
          } else log.warn("Package {} does not contain any dependencies", tgzFile);

          break;
        }
      }
    } catch (Exception e) {
      throw new DependencyExtractionException(
          String.format("Something went wrong during dependency extraction for %s", tgzFile), e);
    }
    return dependencies;
  }
}
