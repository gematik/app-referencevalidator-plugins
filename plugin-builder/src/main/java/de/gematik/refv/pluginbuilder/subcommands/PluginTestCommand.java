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

package de.gematik.refv.pluginbuilder.subcommands;

import de.gematik.refv.pluginbuilder.helper.PluginTester;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static de.gematik.refv.pluginbuilder.subcommands.PluginBuildCommand.ERROR_CODE_EXCEPTION;

@CommandLine.Command(
        name="test",
        description = "Test a given plugin (.zip) against a folder of test-files"
)
@Slf4j
public class PluginTestCommand implements Runnable{

    @CommandLine.Parameters(index = "0", paramLabel = "PLUGIN_FILE", description = "Path to the plugin file (.zip)")
    private File pluginToBeTested;

    @CommandLine.Parameters(index = "1", paramLabel = "TEST_FILES_DIRECTORY", description = "Path to the directory containing test files")
    private File testFilesDirectory;

    @Override
    public void run() {
        if(!testFilesDirectory.exists()) {
            log.error("No such directory: {}", testFilesDirectory);
            return;
        }
        if (!testFilesDirectory.isDirectory()) {
            logTestFilesBlueprint();
            return;
        }
        if (pluginToBeTested.isDirectory() || !pluginToBeTested.getName().toLowerCase().endsWith(".zip")) {
            log.error("The plugin {} you passed is not in the correct file format. Accepted formats: [zip]", pluginToBeTested.getAbsolutePath());
            return;
        }

        try(InputStream pluginToBeTestedAsInputStream = new FileInputStream(pluginToBeTested)) {
            PluginTester pluginTester = new PluginTester(pluginToBeTestedAsInputStream);
            boolean isTestedSuccessful = pluginTester.testPlugin(pluginToBeTested, testFilesDirectory);

            if (!isTestedSuccessful) {
                log.error("Testing of plugin '{}' was unsuccessful! See more info above.", pluginToBeTested.getName());
            }
        } catch (Exception e) {
            log.error("Something went wrong!", e);
            System.exit(ERROR_CODE_EXCEPTION);
        }
    }

    private void logTestFilesBlueprint() {
        StringBuilder sb = new StringBuilder();
        sb.append("\r\nTest files are only accepted as a directory and must be in the following format:");
        sb.append("\n.\n" +
                "└── test-files/\n" +
                "    ├── invalid/\n" +
                "    └── valid/");
        log.error("{}",sb);
    }
}
