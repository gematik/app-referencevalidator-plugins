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
package de.gematik.refv.pluginbuilder;

import de.gematik.refv.pluginbuilder.helper.FhirPackageDownloader;
import de.gematik.refv.pluginbuilder.helper.PluginZipper;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.utilities.npm.PackageServer;

import java.io.File;
import java.util.List;


/**
 * The main class for the <code>PluginBuilder</code>.
 * mandatory arguments:
 * <code>pluginDefinitionDirectoryPath</code> - The filepath to the plugin definition
 * optional arguments:
 * <code>targetFolderPath</code> - The filepath where the final plugin should be saved (default: parent directory of <code>pluginDefinitionFilepath</code>)
 */
@Slf4j
public class PluginBuilderCli {

    public static void main(String[] args) {
        try {
            if(args.length == 0) {
                log.error("Mandatory arguments missing: pluginDefinitionDirectoryPath");
                return;
            }

            String pluginDefinitionDirectoryPath = args[0];
            String targetFolderPath = getTargetFolderPath(args, pluginDefinitionDirectoryPath);
            PluginBuilder pluginBuilder = setUpPluginBuilder(args.length > 2 ? args[2] : null);
            pluginBuilder.buildPlugin(pluginDefinitionDirectoryPath, targetFolderPath);
        } catch (Exception e) {
            log.error("Something went wrong!", e);
        }

    }

    private static String getTargetFolderPath(String[] args, String pluginDefinitionDirectoryPath) {
        if(args.length >= 2) {
            return args[1];
        } else {
            File file = new File(pluginDefinitionDirectoryPath);
            return file.getParentFile().getAbsolutePath();
        }
    }

    private static PluginBuilder setUpPluginBuilder(String packageServerUrl) {
        FhirPackageDownloader fhirPackageDownloader = packageServerUrl == null ? new FhirPackageDownloader() : new FhirPackageDownloader(List.of(new PackageServer(packageServerUrl)));
        PluginZipper pluginZipper = new PluginZipper();

        return new PluginBuilder(
                fhirPackageDownloader,
                pluginZipper
        );
    }
}
