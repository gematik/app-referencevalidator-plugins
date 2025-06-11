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

import de.gematik.refv.pluginbuilder.subcommands.PluginBuildCommand;
import de.gematik.refv.pluginbuilder.subcommands.PluginTestCommand;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

@CommandLine.Command(
        name = "", subcommands = { PluginBuildCommand.class,
        PluginTestCommand.class }
        )
@NoArgsConstructor
@Slf4j
public class PluginBuilderCli {

    @SneakyThrows
    public static void main(String[] args) {
        var cli = new CommandLine(new PluginBuilderCli()).setCaseInsensitiveEnumValuesAllowed(true);
        if(args.length == 0) {
            try(ByteArrayOutputStream out1 = new ByteArrayOutputStream()) {
                PrintWriter out = new PrintWriter(out1);
                cli.usage(out);
                logWithLineBreak(out1.toString());
            }
        }
        else
            cli.execute(args);
    }

    private static void logWithLineBreak(String output) {
        log.info("\r\n{}", output);
    }
}
