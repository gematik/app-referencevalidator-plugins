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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.refv.pluginbuilder.exceptions.DependencyExtractionException;
import de.gematik.refv.commons.configuration.PackageReference;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the extraction of dependencies either from the <code>PluginConfigurationData</code> or from the <code>package.json</code> defined inside a fhir package.
 */
@Slf4j
@NoArgsConstructor
public class DependencyExtractor {

    /**
     * Gets the dependencies from the package.json of all packages defined at packageFolderPath.
     * @param packageFolderPath The path to the folder where the fhir packages are from which the dependencies should be extracted.
     * @return A list of all dependencies as PackageReferences
     * @throws DependencyExtractionException If anything goes wrong during the dependency extraction process.
     */
    public List<PackageReference> getAllDependenciesFromPackageJson(String packageFolderPath) throws DependencyExtractionException {
        List<PackageReference> allDependencies = new ArrayList<>();
        File packageFolder = new File(packageFolderPath);
        File[] tgzFiles = packageFolder.listFiles((dir, name) -> name.endsWith(".tgz"));
        if (tgzFiles != null) {
            for (File packageFile : tgzFiles) {
                List<PackageReference> dependencies = getDependenciesFor(packageFile.getAbsolutePath());
                allDependencies.addAll(dependencies);
            }
        }
        return allDependencies;
    }

    /**
     * Gets all dependencies defined in a package.json.
     * @param tgzFilePath The path to the fhir package from which the dependencies should be extracted.
     * @return A list of all dependencies as PackageReferences
     * @throws DependencyExtractionException If anything goes wrong during the dependency extraction process.
     */
    private List<PackageReference> getDependenciesFor(String tgzFilePath) throws DependencyExtractionException {
        List<PackageReference> dependencies = new ArrayList<>();
        try (FileInputStream fileInputStream = new FileInputStream(tgzFilePath);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             GzipCompressorInputStream gzipCompressorInputStream = new GzipCompressorInputStream(bufferedInputStream);
             TarArchiveInputStream tarInputStream = new TarArchiveInputStream(gzipCompressorInputStream)) {

            TarArchiveEntry entry;
            while ((entry = tarInputStream.getNextEntry()) != null) {
                if (entry.getName().equals("package/package.json")) {
                    String jsonContent = new String(tarInputStream.readAllBytes());
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(jsonContent);
                    JsonNode dependenciesNode = jsonNode.get("dependencies");

                    dependenciesNode.fields().forEachRemaining(dependencyEntry -> {
                        String packageName = dependencyEntry.getKey();
                        String version = dependencyEntry.getValue().asText();
                        if (!packageName.equals("hl7.fhir.r4.core")) {
                            dependencies.add(new PackageReference(packageName, version));
                        }
                    });
                    break;
                }
            }
        } catch (Exception e) {
            throw new DependencyExtractionException(String.format("Something went wrong during dependency extraction for %s", tgzFilePath), e);
        }
        return dependencies;
    }
}
