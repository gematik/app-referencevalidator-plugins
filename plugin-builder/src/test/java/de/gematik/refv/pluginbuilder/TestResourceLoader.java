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

import de.gematik.refv.Plugin;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

public class TestResourceLoader {
    public static byte[] getResourceFileAsByteArray(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null) return null;
            return is.readAllBytes();
        }
    }

    @SneakyThrows
    public static byte[] getIsik1Package(String name) {
        String pluginFilePath = "src/test/resources/plugins/valmodule-isik1.zip";
        Plugin plugin = Plugin.createFromZipFile(new ZipFile(pluginFilePath));
        return plugin.getResource("package/" + name).readAllBytes();
    }
}
