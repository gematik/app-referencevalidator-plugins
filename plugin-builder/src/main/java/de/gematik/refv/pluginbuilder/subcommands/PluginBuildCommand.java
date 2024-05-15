/*
Copyright (c) 2023-2024 gematik GmbH

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
package de.gematik.refv.pluginbuilder.subcommands;

import de.gematik.refv.pluginbuilder.PluginBuilder;
import de.gematik.refv.pluginbuilder.helper.FhirPackageDownloader;
import de.gematik.refv.pluginbuilder.helper.PluginZipper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.utilities.npm.PackageServer;
import picocli.CommandLine;

import java.io.File;
import java.util.List;

@CommandLine.Command(
        name="build",
        description = "The PluginBuilder helps to create custom validation modules (plugins) for the gematik ReferenceValidator (cf. https://github.com/gematik/app-referencevalidator-plugins)"
)
@Slf4j
public class PluginBuildCommand implements Runnable {

    public static final int ERROR_CODE_TESTS_FAILED = 1;
    public static final int ERROR_CODE_EXCEPTION = 2;
    public static final int SUCCESS_CODE = 0;
    @CommandLine.Parameters(paramLabel = "PLUGIN_DEFINITION_DIRECTORY", description = "Directory with plugin definition file and supporting resources")
    private String pluginDefinitionDirectoryPath;

    @CommandLine.Option(names = {"-t", "--targetFolderPath"}, description = "Output directory for created plugins (default: parent directory of PLUGIN_DEFINITION_DIRECTORY)")
    private String targetFolderPath;

    @CommandLine.Option(names = {"-url", "--packageServerUrl"}, description = "The URL of a FHIR package server to use")
    private String packageServerUrl;

    @CommandLine.Option(names = {"-dse", "--disableSuccessAndErrorCodes"}, description = "Disables success/error codes upon finish (0 and 1)")
    private boolean areSuccessAndErrorCodesDisabled = false;

    @Override
    public void run() {
        try {
            targetFolderPath = !StringUtils.isEmpty(targetFolderPath) ? targetFolderPath :
                    new File(pluginDefinitionDirectoryPath).getParentFile().getAbsolutePath();

            PluginBuilder pluginBuilder = setUpPluginBuilder();
            var buildResult = pluginBuilder.buildPlugin(pluginDefinitionDirectoryPath, targetFolderPath);


            if (!buildResult.isTestedSuccessfully()) {
                exit(ERROR_CODE_TESTS_FAILED);
            } else
                exit(SUCCESS_CODE);
        } catch (Exception e) {
            log.error("Something went wrong!", e);
            exit(ERROR_CODE_EXCEPTION);
        }
    }

    private void exit(int errorCode) {
        if(!areSuccessAndErrorCodesDisabled)
            System.exit(errorCode);
    }

    private PluginBuilder setUpPluginBuilder() {
        FhirPackageDownloader fhirPackageDownloader = (packageServerUrl == null) ?
                new FhirPackageDownloader() :
                new FhirPackageDownloader(List.of(new PackageServer(packageServerUrl)));
        PluginZipper pluginZipper = new PluginZipper();

        return new PluginBuilder(
                fhirPackageDownloader,
                pluginZipper
        );
    }
}
