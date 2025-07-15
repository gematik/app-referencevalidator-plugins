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

import de.gematik.refv.pluginbuilder.exceptions.DependencyExtractionException;
import de.gematik.refv.pluginbuilder.exceptions.DownloadFailedException;
import de.gematik.refv.plugins.configuration.FhirPackage;
import de.gematik.refv.plugins.configuration.MalformedPackageDeclarationException;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.utilities.npm.PackageClient;
import org.hl7.fhir.utilities.npm.PackageServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the download process of all fhir packages defined in a PluginDefinition (config.yaml)
 */
@Slf4j
@Getter
public class FhirPackageDownloader {

    private final List<PackageClient> packageClients = new LinkedList<>();
    private final List<FhirPackage> downloadedPackages = new ArrayList<>();
    private final DependencyExtractor dependencyGenerator = new DependencyExtractor();

    public FhirPackageDownloader(@NonNull List<PackageServer> packageServers) {
        if(packageServers.isEmpty())
            throw new IllegalArgumentException("At least one package server is required");

        packageServers.forEach(s -> packageClients.add(new PackageClient(s)));
    }

    public FhirPackageDownloader() {
        this(List.of(PackageServer.primaryServer(), PackageServer.secondaryServer()));
    }

    /**
     * Recursively downloads all fhir packages and adds fhir packages to the final DependencyList of the plugin.
     * @param fhirPackages List of fhir packages that should be downloaded
     * @param outputFolderPath The path to the folder where the downloaded fhir packages will be saved.
     * @throws DownloadFailedException If anything goes wrong during download process.
     * @throws MalformedPackageDeclarationException If a package declaration inside of <code>PluginDefinition</code> does not follow the correct pattern: <code>packageName#packageVersion</code>
     * @throws DependencyExtractionException If anything goes wrong during dependency extraction.
     */
    public void download(List<FhirPackage> fhirPackages, String outputFolderPath, Collection<File> allDownloadedFiles, Collection<FhirPackage> flatDependencyList) throws DownloadFailedException, MalformedPackageDeclarationException, DependencyExtractionException, IOException {
        flatDependencyList.addAll(fhirPackages.stream().filter(p -> !p.getVersion().endsWith("x")).collect(Collectors.toList())); // Skip wildcard versions, they will be resolved later
        List<FhirPackage> newPackages = getNewPackages(fhirPackages);

        if (newPackages.isEmpty()) {
            return;
        }

        File outputFolder = new File(outputFolderPath);
        if (!outputFolder.exists() && !outputFolder.mkdirs())
            throw new DownloadFailedException("Could not create output folder " + outputFolderPath);

        var downloadedFiles = new LinkedList<File>();
        for (FhirPackage pr : newPackages) {
            String packageVersion = pr.getVersion();
            if(packageVersion.toLowerCase().endsWith("x")) {
                for (var packageClient :
                        packageClients) {
                    packageVersion = packageClient.getLatestVersion(pr.getName(), packageVersion);
                    if(!StringUtils.isEmpty(packageVersion))
                        break;
                }
            }
            File downloadedFile = downloadPackage(new FhirPackage(pr.getName(), packageVersion), outputFolderPath);
            downloadedFiles.add(downloadedFile);
            allDownloadedFiles.add(downloadedFile);
            log.info("Successfully downloaded package: {}", downloadedFile.getName());
        }

        List<FhirPackage> dependencies = dependencyGenerator.getAllDependenciesFromPackageJson(downloadedFiles);
        download(dependencies, outputFolderPath, allDownloadedFiles, flatDependencyList);
    }

    /**
     * First trying with primaryPackageClient if this fails trying with secondaryPackageClient.
     * @param packageReference The PackageReference used to download a fhir package
     * @param outputFolderPath The path to the folder where the downloaded fhir packages will be saved.
     * @throws DownloadFailedException If anything goes wrong during the download of the fhir package.
     */
    private File downloadPackage(FhirPackage packageReference, String outputFolderPath) throws DownloadFailedException {
        String packageFilePath = String.format("%s%s", outputFolderPath, packageReference.toFilename());
        String packageVersion = packageReference.getVersion();
        File downloadedFile = new File(packageFilePath);
        if (downloadedFile.exists()) {
            log.info("Package '{}' already exists at '{}'.", packageFilePath, outputFolderPath);
            return downloadedFile;
        }

        boolean downloadSuccessful = false;
        for (var packageClient :
                packageClients) {
            try {
                try (InputStream inputStream = packageClient.fetch(packageReference.getName(), packageVersion);
                     FileOutputStream outputStream = new FileOutputStream(packageFilePath)) {
                    writePackage(inputStream, outputStream);
                }
                downloadSuccessful = true;
                break;
            } catch (IOException e) {
                log.warn("Could not download package {} from server {} (Cause: {}). Trying with the next one...", downloadedFile.getName(), packageClient.url("",""), e.getMessage());
            }
        }
        if(!downloadSuccessful)
            throw new DownloadFailedException(String.format("Download of package '%s' failed", packageReference.toFilename()));

        return downloadedFile;
    }

    /**
     * Writes the downloaded package into the final file.
     * @param inputStream The inputstream that provides the bytes to be written.
     * @param outputStream The outputstream used to write the bytes.
     * @throws IOException On stream read or write errors
     */
    private void writePackage(InputStream inputStream, FileOutputStream outputStream) throws IOException {
        byte[] buffer = new byte[8 * 1024];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
    }

    /**
     * Gets a list of new packages with each recursion, so no fhir package will be downloaded multiple times and to give a breaking condition for the recursion.
     * @param packages The list of packages that should be purged of already downloaded packages.
     * @return Returns a list of PackageReferences that haven't been downloaded yet.
     */
    private List<FhirPackage> getNewPackages(List<FhirPackage> packages) {
        return packages.stream()
                .filter(packageReference -> !downloadedPackages.contains(packageReference))
                .distinct()
                .collect(Collectors.toList());
    }
}
