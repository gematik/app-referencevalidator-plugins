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

import de.gematik.refv.pluginbuilder.support.TestAppender;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;

import static de.gematik.refv.pluginbuilder.TestResourceLoader.getIsik1Package;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class PluginBuilderCliIT {

    private final ClientAndServer mockServer = new ClientAndServer("localhost", 0);

    @BeforeEach
    @SneakyThrows
    void setUp() {
        FileUtils.deleteDirectory(new File(getTempDir()));
        if(!new File(getTempDir()).mkdirs())
            throw new IOException("Could not create temp dir in target folder");
        FileUtils.copyDirectory(new File("src/test/resources/plugins/correct-plugin"), new File(getTempDir() + "correct-plugin"));
    }

    @SneakyThrows
    private static String getTempDir() {
        return Paths.get(Objects.requireNonNull(PluginBuilderCliIT.class.getResource("/")).toURI()).getParent().toString() + File.separator + "pluginbuilder-cli-it" + File.separator;
    }

    private String getFhirPackageServerUrl() {
        return "http://localhost:" + mockServer.getLocalPort();
    }

    @Test
    void testPluginBuilderCliWithoutTargetFolder() {
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
                .withBody(getIsik1Package("de.gematik.isik-basismodul-stufe1-1.0.9.tgz")));
        mockServer.when(request2).respond(response()
                .withStatusCode(200)
                .withContentType(MediaType.parse("application/tar+gzip"))
                .withBody(getIsik1Package("de.basisprofil.r4-1.4.0.tgz")));
        mockServer.when(request3).respond(response()
                .withStatusCode(200)
                .withContentType(MediaType.parse("application/tar+gzip"))
                .withBody(getIsik1Package("kbv.basis-1.2.0.tgz")));
        mockServer.when(request4).respond(response()
                .withStatusCode(200)
                .withContentType(MediaType.parse("application/tar+gzip"))
                .withBody(getIsik1Package("de.basisprofil.r4-0.9.13.tgz")));

        String inputFolder = getTempDir() + "correct-plugin";
        PluginBuilderCli.main(new String[]{"build", inputFolder, "-url", getFhirPackageServerUrl(), "--disableSuccessAndErrorCodes"});

        File plugin = new File(getTempDir() + "isik1-plugin-1.0.0.zip");
        assertThat(plugin).exists();
        mockServer.verify(request1, VerificationTimes.once());
        mockServer.verify(request2, VerificationTimes.once());
        mockServer.verify(request3, VerificationTimes.once());
        mockServer.verify(request4, VerificationTimes.once());
    }

    @Test
    void testPluginBuilderCliWithTargetFolder() {
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
                .withBody(getIsik1Package("de.gematik.isik-basismodul-stufe1-1.0.9.tgz")));
        mockServer.when(request2).respond(response()
                .withStatusCode(200)
                .withContentType(MediaType.parse("application/tar+gzip"))
                .withBody(getIsik1Package("de.basisprofil.r4-1.4.0.tgz")));
        mockServer.when(request3).respond(response()
                .withStatusCode(200)
                .withContentType(MediaType.parse("application/tar+gzip"))
                .withBody(getIsik1Package("kbv.basis-1.2.0.tgz")));
        mockServer.when(request4).respond(response()
                .withStatusCode(200)
                .withContentType(MediaType.parse("application/tar+gzip"))
                .withBody(getIsik1Package("de.basisprofil.r4-0.9.13.tgz")));

        String inputFolder = getTempDir() + "correct-plugin";
        String outputFolder = getTempDir() + "correct-plugin-result" + File.separator;
        PluginBuilderCli.main(new String[]{"build", inputFolder, "-t", outputFolder, "-url", getFhirPackageServerUrl(), "--disableSuccessAndErrorCodes"});

        File plugin = new File(outputFolder + "isik1-plugin-1.0.0.zip");
        assertThat(plugin).exists();
        mockServer.verify(request1, VerificationTimes.once());
        mockServer.verify(request2, VerificationTimes.once());
        mockServer.verify(request3, VerificationTimes.once());
        mockServer.verify(request4, VerificationTimes.once());
    }

    @Test
    void testPluginBuilderCliWithNoArguments(){
        TestAppender appender = new TestAppender();
        Logger.getRootLogger().addAppender(appender);

        PluginBuilderCli.main(new String[]{"build"});

        boolean noExceptionsInLogs = appender.getLogs().stream().noneMatch(e -> e.getMessage().toString().contains("Exception"));
        assertThat(noExceptionsInLogs).isTrue();
    }

    @Test
    void testPluginBuilderCliCatchingException() {
        TestAppender appender = new TestAppender();
        Logger.getRootLogger().addAppender(appender);

        String inputFolder = getTempDir() + "%%%%%%%%" + File.separator;
        assertThatCode(() -> PluginBuilderCli.main(new String[]{"build", inputFolder, "-url", getFhirPackageServerUrl(), "--disableSuccessAndErrorCodes"}))
                .doesNotThrowAnyException();

        boolean exceptionWasLogged = appender.getLogs().stream().anyMatch(e -> e.getMessage().toString().contains("Something went wrong!"));
        assertThat(exceptionWasLogged).isTrue();
    }

    @Test
    void testTestPlugin() {
        TestAppender appender = new TestAppender();
        Logger.getRootLogger().addAppender(appender);

        String plugin = "src/test/resources/plugins/valmodule-isik1.zip";
        String testFiles = "src/test/resources/plugins/test-files";
        assertThatCode(() -> PluginBuilderCli.main(new String[]{"test", plugin, testFiles}))
                .doesNotThrowAnyException();

        boolean testedSuccessful = appender.getLogs().stream().anyMatch(e -> e.getMessage().toString().contains("Finished testing plugin 'valmodule-isik1.zip' successfully."));
        assertThat(testedSuccessful).isTrue();
    }
}
