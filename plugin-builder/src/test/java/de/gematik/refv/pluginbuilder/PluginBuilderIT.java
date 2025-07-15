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

import de.gematik.refv.Plugin;
import de.gematik.refv.pluginbuilder.configuration.PluginDefinitionFactory;
import de.gematik.refv.pluginbuilder.exceptions.DownloadFailedException;
import de.gematik.refv.pluginbuilder.exceptions.PluginIdentifierException;
import de.gematik.refv.pluginbuilder.helper.FhirPackageDownloader;
import de.gematik.refv.pluginbuilder.helper.PluginZipper;
import de.gematik.refv.plugins.configuration.FhirPackage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.utilities.npm.PackageServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Slf4j
class PluginBuilderIT {

    private static PluginBuilder pluginBuilder;

    private final ClientAndServer mockServer = new ClientAndServer("localhost", 0);

    @SneakyThrows
    private static String getTargetDir() {
        return Paths.get(Objects.requireNonNull(PluginBuilderIT.class.getResource("/")).toURI()).getParent().toString() + "/built-plugins";
    }

    @BeforeEach
    @SneakyThrows
    void setUp() {
        String serverUrl = "http://localhost:" + mockServer.getLocalPort();
        FhirPackageDownloader fhirPackageDownloader = new FhirPackageDownloader(List.of(new PackageServer(serverUrl)));
        PluginZipper pluginZipper = new PluginZipper();

        pluginBuilder = new PluginBuilder(
                fhirPackageDownloader,
                pluginZipper
        );

        FileUtils.deleteDirectory(new File(getTargetDir()));
    }

    @SneakyThrows
    @Test
    void testBuildPluginV2() {
        var request1 = request()
                .withPath("/de.gematik.isik-basismodul-stufe1/1.0.9");
        var request2 = request()
                .withPath("/de.basisprofil.r4/1.4.0");
        var request3 = request()
                .withPath("/kbv.basis/1.2.0");
        var request4 = request()
                .withPath("/de.basisprofil.r4/0.9.13");
        mockServer.when(request1).respond(response()
                .withStatusCode(200)
                .withContentType(MediaType.parse("application/tar+gzip"))
                .withBody(TestResourceLoader.getIsik1Package("de.gematik.isik-basismodul-stufe1-1.0.9.tgz")));
        mockServer.when(request2).respond(response()
                .withStatusCode(200)
                .withContentType(MediaType.parse("application/tar+gzip"))
                .withBody(TestResourceLoader.getIsik1Package("de.basisprofil.r4-1.4.0.tgz")));
        mockServer.when(request3).respond(response()
                .withStatusCode(200)
                .withContentType(MediaType.parse("application/tar+gzip"))
                .withBody(TestResourceLoader.getIsik1Package("kbv.basis-1.2.0.tgz")));
        mockServer.when(request4).respond(response()
                .withStatusCode(200)
                .withContentType(MediaType.parse("application/tar+gzip"))
                .withBody(TestResourceLoader.getIsik1Package("de.basisprofil.r4-0.9.13.tgz")));

        String pluginDir = "src/test/resources/plugins/correct-plugin-v2";
        String resultFilepath = pluginBuilder.buildPlugin(pluginDir, getTargetDir()).getCreatedPluginPath();
        File pluginZip = new File(resultFilepath);

        assertTrue(pluginZip.exists(), "Plugin zip has not been created");
        assertEquals("isik1-plugin-v2-1.0.0.zip", pluginZip.getName(), "Wrong plugin file name");

        //check for downloaded packages and config.yaml
        assertZipEntryExists(pluginZip, "package/de.basisprofil.r4-1.4.0.tgz");
        assertZipEntryExists(pluginZip, "package/de.gematik.isik-basismodul-stufe1-1.0.9.tgz");
        assertZipEntryExists(pluginZip, "package/kbv.basis-1.2.0.tgz");
        assertZipEntryExists(pluginZip, "config.yaml");

        // This entry is marked as 'snapshot' dependency and should not be included in the plugin zip
        assertZipEntryDoesntExists(pluginZip, "package/de.basisprofil.r4-0.9.13.tgz");

        Plugin p = Plugin.createFromZipFile(new ZipFile(pluginZip));
        var pluginDefinition = PluginDefinitionFactory.createFrom(p.getResource("config.yaml"));
        assertFalse(pluginDefinition.getValidation().getDependencyLists().get(0).getDependencies().isEmpty(), "Dependency list has not been filled");

        // "de.basisprofil.r4#0.9.13" should not be in the dependency list, because it is a snapshot dependency
        assertThat(pluginDefinition.getValidation().getDependencyLists().get(0).getDependencies()).containsExactlyInAnyOrderElementsOf(List.of(
                new FhirPackage("simplevalidationmodule.test#1.0.0"),
                new FhirPackage("kbv.basis#1.2.0"),
                new FhirPackage("de.basisprofil.r4#1.4.0")
        ));
        assertFalse(pluginDefinition.getValidation().getDependencyLists().get(1).getDependencies().isEmpty(), "Dependency list has not been filled");
        assertThat(pluginDefinition.getValidation().getDependencyLists().get(1).getDependencies()).containsExactlyInAnyOrderElementsOf(List.of(
                new FhirPackage("de.basisprofil.r4#1.4.0")
        ));


        //check if package that was passed in src-packages exists
        assertZipEntryExists(pluginZip, "package/simplevalidationmodule.test-1.0.0.tgz");
        mockServer.verify(request1, VerificationTimes.exactly(1));
        mockServer.verify(request2, VerificationTimes.exactly(1));
        mockServer.verify(request3, VerificationTimes.exactly(1));
        mockServer.verify(request4, VerificationTimes.exactly(1));
    }

    @SneakyThrows
    private void assertZipEntryDoesntExists(File zipFile, String entryPath) {
        try (ZipFile zf = new ZipFile(zipFile)) {
            assertNull(zf.getEntry(entryPath), "'" + entryPath + "' found in the plugin zip file");
        }
    }


    @SneakyThrows
    private static void assertZipEntryExists(File zipFile, String entryPath) {
        try (ZipFile zf = new ZipFile(zipFile)) {
            assertNotNull(zf.getEntry(entryPath), "'" + entryPath + "' not found in the plugin zip file");
        }
    }

    @Test
    @SneakyThrows
    void testBuildPluginThrowsIllegalArgumentExceptionNonExistentDir() {
        String pluginDir = "src/test/resources/plugins/non-existent";
        String targetDir = getTargetDir();
        Assertions.assertThrows(IllegalArgumentException.class, () -> pluginBuilder.buildPlugin(pluginDir, targetDir));
    }

    @Test
    @SneakyThrows
    void testBuildPluginThrowsIllegalArgumentExceptionIsNotADirectory() {
        String pluginDir = "src/test/resources/plugins/not-a-directory.txt";
        String targetDir = getTargetDir();
        Assertions.assertThrows(IllegalArgumentException.class, () -> pluginBuilder.buildPlugin(pluginDir, targetDir));
    }

    @Test
    @SneakyThrows
    void testBuildPluginThrowsDownloadFailedException() {
        String pluginDir = "src/test/resources/plugins/download-failed";
        String targetDir = getTargetDir();
        Assertions.assertThrows(DownloadFailedException.class, () -> pluginBuilder.buildPlugin(pluginDir, targetDir));
    }

    @Test
    @SneakyThrows
    void testBuildPluginThrowsMalformedPackageDeclarationException() {
        String pluginDir = "src/test/resources/plugins/malformed-package-declaration";
        String targetDir = getTargetDir();
        Assertions.assertThrows(IllegalArgumentException.class, () -> pluginBuilder.buildPlugin(pluginDir, targetDir));
    }

    @Test
    @SneakyThrows
    void testBuildPluginThrowsPluginIdentifierException() {
        String pluginDir = "src/test/resources/plugins/duplicate-plugin-identifier";
        String targetDir = getTargetDir();
        Assertions.assertThrows(PluginIdentifierException.class, () -> pluginBuilder.buildPlugin(pluginDir, targetDir));
    }


}
