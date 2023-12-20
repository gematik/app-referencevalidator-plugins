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
import de.gematik.refv.pluginbuilder.configuration.PluginDefinitionWrapper;
import de.gematik.refv.pluginbuilder.exceptions.DependencyExtractionException;
import de.gematik.refv.pluginbuilder.exceptions.DownloadFailedException;
import de.gematik.refv.plugins.configuration.MalformedPackageDeclarationException;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.utilities.npm.PackageClient;
import org.hl7.fhir.utilities.npm.PackageServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
    private final List<PackageReference> downloadedPackages = new ArrayList<>();
    private final DependencyExtractor dependencyGenerator = new DependencyExtractor();
    private List<PackageReference> snapshotDependencies = new ArrayList<>();

    public FhirPackageDownloader(@NonNull List<PackageServer> packageServers) {
        if(packageServers.isEmpty())
            throw new IllegalArgumentException("At least one package server is required");

        packageServers.forEach(s -> packageClients.add(new PackageClient(s)));
    }

    public FhirPackageDownloader() {
        this(List.of(PackageServer.primaryServer(), PackageServer.secondaryServer()));
    }

    /**
     * Public method to download fhir packages for a PluginDefinition.
     * @param outputFolderPath The path to the folder where the downloaded fhir packages will be saved.
     * @param pluginDefinition The PluginDefinition containing all information needed to download the packages.
     * @throws DownloadFailedException If anything goes wrong during download process.
     * @throws MalformedPackageDeclarationException If a package declaration inside of <code>PluginDefinition</code> does not follow the correct pattern: <code>packageName#packageVersion</code>
     * @throws DependencyExtractionException If anything goes wrong during dependency extraction.
     */
    public void download(String outputFolderPath, PluginDefinitionWrapper pluginDefinition) throws DownloadFailedException, MalformedPackageDeclarationException, DependencyExtractionException {
        List<PackageReference> fhirPackages = new ArrayList<>();
        fhirPackages.add(pluginDefinition.getValidationFhirPackageAsPackageReference());

        downloadAll(fhirPackages, outputFolderPath, pluginDefinition);
    }

    /**
     * Recursively downloads all fhir packages and adds fhir packages to the final DependencyList of the plugin.
     * @param fhirPackages List of fhir packages that should be downloaded
     * @param outputFolderPath The path to the folder where the downloaded fhir packages will be saved.
     * @param pluginDefinition The PluginDefinition containing all information needed to download the packages.
     * @throws DownloadFailedException If anything goes wrong during download process.
     * @throws MalformedPackageDeclarationException If a package declaration inside of <code>PluginDefinition</code> does not follow the correct pattern: <code>packageName#packageVersion</code>
     * @throws DependencyExtractionException If anything goes wrong during dependency extraction.
     */
    private void downloadAll(List<PackageReference> fhirPackages, String outputFolderPath, PluginDefinitionWrapper pluginDefinition) throws DownloadFailedException, MalformedPackageDeclarationException, DependencyExtractionException {
        List<PackageReference> newPackages = getNewPackages(fhirPackages);

        if (newPackages.isEmpty()) {
            return;
        }

        File outputFolder = new File(outputFolderPath);
        if (!outputFolder.exists() && !outputFolder.mkdirs())
            throw new DownloadFailedException("Could not create output folder " + outputFolderPath);

        for (PackageReference pr : newPackages) {
            String filename = String.format("%s-%s.tgz", pr.getPackageName(), pr.getPackageVersion());
            downloadPackage(pr, outputFolderPath, filename);

            log.info("Successfully downloaded package: {}-{}.tgz", pr.getPackageName(), pr.getPackageVersion());
            if(!snapshotDependencies.contains(pr)) {
                pluginDefinition.getDependenciesForDependencyList().add(String.format("%s-%s.tgz", pr.getPackageName(), pr.getPackageVersion()));
            }
            downloadedPackages.add(pr);
        }

        List<PackageReference> dependencies = dependencyGenerator.getAllDependenciesFromPackageJson(outputFolderPath);
        addDependenciesFromPluginDefinition(pluginDefinition, dependencies);
        snapshotDependencies = pluginDefinition.getSnapshotDependenciesAsPackageReferences();

        downloadAll(dependencies, outputFolderPath, pluginDefinition);
    }

    /**
     * Adding the dependencies specified in the PluginDefinition, so they are downloaded as well.
     * @param pluginDefinition The PluginDefinition containing all information needed to download the packages.
     * @param dependencies The list of dependencies where the dependencies from the PluginDefinition should be added.
     * @throws MalformedPackageDeclarationException If a package declaration inside of <code>PluginDefinition</code> does not follow the correct pattern: <code>packageName#packageVersion</code>
     */
    private void addDependenciesFromPluginDefinition(PluginDefinitionWrapper pluginDefinition, List<PackageReference> dependencies) throws MalformedPackageDeclarationException {
        List<PackageReference> dependenciesFromConfig = new ArrayList<>();
        dependenciesFromConfig.addAll(pluginDefinition.getSnapshotDependenciesAsPackageReferences());
        dependenciesFromConfig.addAll(pluginDefinition.getValidationDependenciesAsPackageReferences());

        for (PackageReference pr : dependenciesFromConfig) {
            if (!downloadedPackages.contains(pr)) {
                dependencies.add(pr);
            }
        }
    }

    /**
     * First trying with primaryPackageClient if this fails trying with secondaryPackageClient.
     * @param packageReference The PackageReference used to download a fhir package
     * @param outputFolderPath The path to the folder where the downloaded fhir packages will be saved.
     * @param filename The filename that the downloaded fhir package should have.
     * @throws DownloadFailedException If anything goes wrong during the download of the fhir package.
     */
    private void downloadPackage(PackageReference packageReference, String outputFolderPath, String filename) throws DownloadFailedException {
        boolean downloadSuccessful = false;
        for (var packageClient :
                packageClients) {
            try {
                downloadPackageWithClient(packageReference, outputFolderPath, filename, packageClient);
                downloadSuccessful = true;
                break;
            } catch (IOException e) {
                log.error("Something went wrong while downloading the package with {}. Trying with the next one...", packageClient.url("",""), e);
            }
        }
        if(!downloadSuccessful)
            throw new DownloadFailedException(String.format("Download of package '%s-%s.tgz' failed", packageReference.getPackageName(), packageReference.getPackageVersion()));

    }

    /**
     * Downloads a fhir package with the specified packageClient.
     * @param packageReference The PackageReference used to download a fhir package
     * @param outputFolderPath The path to the folder where the downloaded fhir packages will be saved.
     * @param filename The filename that the downloaded fhir package should have.
     * @param packageClient The specific package client used to download the fhir package.
     * @throws IOException If anything goes wrong during the download of the fhir package.
     */
    private void downloadPackageWithClient(PackageReference packageReference, String outputFolderPath, String filename, PackageClient packageClient) throws IOException {
        String packageFilePath = outputFolderPath + filename;
        if (new File(packageFilePath).exists()) {
            log.info("Package '{}' already exists at '{}'.", filename, outputFolderPath);
            return;
        }
        try (InputStream inputStream = packageClient.fetch(packageReference.getPackageName(), packageReference.getPackageVersion());
             FileOutputStream outputStream = new FileOutputStream(outputFolderPath + filename)) {
            writePackage(inputStream, outputStream);
        }
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
    private List<PackageReference> getNewPackages(List<PackageReference> packages) {
        return packages.stream()
                .map(packageReference -> {
                    String packageName = packageReference.getPackageName().toLowerCase();
                    String packageVersion = packageReference.getPackageVersion().toLowerCase();
                    return new PackageReference(packageName, packageVersion);
                })
                .filter(packageReference -> !downloadedPackages.contains(packageReference))
                .distinct()
                .collect(Collectors.toList());
    }
}
