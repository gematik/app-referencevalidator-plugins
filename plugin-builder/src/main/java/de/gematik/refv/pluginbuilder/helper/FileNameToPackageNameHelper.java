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

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
@Slf4j
public class FileNameToPackageNameHelper {

    public static String fileNameToPackageName(String fileName) {
        Pattern pattern = Pattern.compile("^(.+)-(\\d+\\.\\d+\\.\\d+)(-.+)?");
        Matcher matcher = pattern.matcher(fileName);
        if (!matcher.find())
            throw new IllegalArgumentException("Could not map the file name to package name: " + fileName);

        String version = matcher.group(2) + (matcher.group(3) != null ? matcher.group(3) : "");
        return MessageFormat.format("{0}#{1}", matcher.group(1), version).replace(".tgz", "");
    }
}
