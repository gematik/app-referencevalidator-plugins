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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import de.gematik.refv.pluginbuilder.TestResourceLoader;
import de.gematik.refv.pluginbuilder.configuration.PluginDefinitionFactory;
import de.gematik.refv.pluginbuilder.exceptions.DownloadFailedException;
import de.gematik.refv.plugins.configuration.FhirPackage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.utilities.npm.PackageServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;

class FhirPackageDownloaderTests {

  private static FhirPackageDownloader fhirPackageDownloader;

  private static final ClientAndServer mockServer = new ClientAndServer("localhost", 0);

  @BeforeAll
  static void beforeAll() {
    String serverUrl = "http://localhost:" + mockServer.getLocalPort();
    fhirPackageDownloader = new FhirPackageDownloader(List.of(new PackageServer(serverUrl)));
  }

  @BeforeEach
  @SneakyThrows
  void beforeEach() {
    mockServer.reset();
  }

  @SneakyThrows
  private static String getDownloadDir() {
    return Paths.get(
                Objects.requireNonNull(FhirPackageDownloaderTests.class.getResource("/")).toURI())
            .getParent()
            .toString()
        + "/downloaded-packages/";
  }

  @Test
  @SneakyThrows
  void testDownload() {
    String downloadDirPath = getDownloadDir() + UUID.randomUUID() + File.separator;
    File downloadDir = new File(downloadDirPath);
    try {
      var pluginDefinition =
          PluginDefinitionFactory.createFrom("src/test/resources/ConfigData-download-test.yaml");

      var responsePackageAsBytes =
          TestResourceLoader.getResourceFileAsByteArray("example-fhir-package.tgz");
      var request1 = request().withPath("/de.basisprofil.r4/1.3.2");
      var request2 = request().withPath("/de.basisprofil.r4/1.4.0");
      var packageResponse =
          response()
              .withStatusCode(200)
              .withContentType(MediaType.parse("application/tar+gzip"))
              .withHeader(
                  "Content-Disposition",
                  "attachment; filename=de.basisprofil.r4-1.3.2.tgz; filename*=UTF-8''de.basisprofil.r4-1.3.2.tgz")
              .withBody(responsePackageAsBytes);
      mockServer.when(request1).respond(packageResponse);
      mockServer.when(request2).respond(packageResponse);

      List<FhirPackage> filesToDownload =
          new ArrayList<>(
              pluginDefinition.getValidation().getDependencyLists().get(0).getDependencies());
      filesToDownload.add(
          pluginDefinition.getValidation().getDependencyLists().get(0).getFhirPackage());
      fhirPackageDownloader.download(
          filesToDownload, downloadDirPath, new LinkedList<>(), new LinkedList<>());

      assertThat(List.of(Objects.requireNonNull(downloadDir.list())))
          .contains("de.basisprofil.r4-1.4.0.tgz")
          .contains("de.basisprofil.r4-1.3.2.tgz");
      mockServer.verify(request1, VerificationTimes.once());
      mockServer.verify(request2, VerificationTimes.once());
    } finally {
      FileUtils.deleteDirectory(downloadDir);
    }
  }

  @Test
  @SneakyThrows
  void testDownloadWildcard() {
    String downloadDirPath = getDownloadDir() + UUID.randomUUID() + File.separator;
    File downloadDir = new File(downloadDirPath);
    try {
      var pluginDefinition =
          PluginDefinitionFactory.createFrom("src/test/resources/download-wildcard-test.yaml");

      var allWildcardMatchingPackagesResponse =
          FileUtils.readFileToString(
              new File("src/test/resources/wildcard-response.json"), StandardCharsets.UTF_8);
      var request = request().withPath("/de.basisprofil.r4");
      var response =
          response()
              .withStatusCode(200)
              .withContentType(MediaType.parse("application/json"))
              .withBody(allWildcardMatchingPackagesResponse);
      mockServer.when(request).respond(response);

      var responsePackageAsBytes =
          TestResourceLoader.getResourceFileAsByteArray("example-fhir-package.tgz");
      var request1 = request().withPath("/some.package/0.0.1");
      var request2 = request().withPath("/de.basisprofil.r4/1.3.3");
      mockServer
          .when(request1)
          .respond(
              response()
                  .withStatusCode(200)
                  .withContentType(MediaType.parse("application/tar+gzip"))
                  .withHeader(
                      "Content-Disposition",
                      "attachment; filename=some.package-0.0.1.tgz; filename*=UTF-8''some.package-0.0.1.tgz")
                  .withBody(responsePackageAsBytes));
      mockServer
          .when(request2)
          .respond(
              response()
                  .withStatusCode(200)
                  .withContentType(MediaType.parse("application/tar+gzip"))
                  .withHeader(
                      "Content-Disposition",
                      "attachment; filename=de.basisprofil.r4-1.3.3.tgz; filename*=UTF-8''de.basisprofil.r4-1.3.3.tgz")
                  .withBody(responsePackageAsBytes));

      List<FhirPackage> packagesToDownload =
          new ArrayList<>(
              pluginDefinition.getValidation().getDependencyLists().get(0).getDependencies());
      packagesToDownload.add(
          pluginDefinition.getValidation().getDependencyLists().get(0).getFhirPackage());
      fhirPackageDownloader.download(
          packagesToDownload, downloadDirPath, new LinkedList<>(), new LinkedList<>());

      assertThat(List.of(Objects.requireNonNull(downloadDir.list())))
          .contains("some.package-0.0.1.tgz")
          .contains("de.basisprofil.r4-1.3.3.tgz");
      mockServer.verify(request, VerificationTimes.atLeast(1));
      mockServer.verify(request1, VerificationTimes.once());
      mockServer.verify(request2, VerificationTimes.once());
    } finally {
      FileUtils.deleteDirectory(downloadDir);
    }
  }

  @Test
  @SneakyThrows
  void testDownloadOfUnknownPackageThrowsException() {
    String downloadDirPath = getDownloadDir() + UUID.randomUUID() + File.separator;
    File downloadDir = new File(downloadDirPath);
    try {
      var request = request().withPath("/test-package/1.0.0");
      mockServer.when(request).respond(response().withStatusCode(404));

      var pluginDefinition =
          PluginDefinitionFactory.createFrom("src/test/resources/plugin-definition.yaml");
      Assertions.assertThrows(
          DownloadFailedException.class,
          () ->
              fhirPackageDownloader.download(
                  pluginDefinition.getValidation().getDependencyLists().get(0).getDependencies(),
                  downloadDirPath,
                  new LinkedList<>(),
                  new LinkedList<>()));
    } finally {
      FileUtils.deleteDirectory(downloadDir);
    }
  }
}
