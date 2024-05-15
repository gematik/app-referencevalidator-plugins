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
package de.gematik.refv.pluginbuilder.helper;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginZipperTests {

    private PluginZipper pluginZipper;
    private File zipFile;
    private final String sourceFolderPath = "src/test/resources/zip-test-plugin";
    private final String targetFolderPath = "target/";

    @BeforeEach
    void setUp() {
        pluginZipper = new PluginZipper();
    }

    @AfterEach
    @SneakyThrows
    void cleanUp() {
        if (zipFile != null && zipFile.exists() &&!zipFile.delete())
            throw new IllegalStateException("Could not delete zipFile " + zipFile.getName());
    }

    @Test
    @SneakyThrows
    void testZipPlugin() {
        zipFile = pluginZipper.zipPlugin("zip-test-plugin", "1.0.0", sourceFolderPath, targetFolderPath, new ArrayList<>());

        assertTrue(zipFile.exists());
        assertEquals("zip-test-plugin-1.0.0.zip", zipFile.getName(), "Wrong final zipfile name");
        assertZipEntryExists(zipFile, "config.yaml");
        assertZipEntryExists(zipFile, "package/test-package.txt");
        assertZipEntryExists(zipFile, "test-files/test.txt");
    }

    @Test
    @SneakyThrows
    void testZipPluginExclusionString() {
        zipFile = pluginZipper.zipPlugin("zip-test-plugin", "1.0.0", sourceFolderPath, targetFolderPath, List.of("test-files"));

        assertZipEntryNotExists(zipFile);
    }

    private static void assertZipEntryExists(File zipFile, String entryPath) throws Exception {
        try (ZipFile zf = new ZipFile(zipFile)) {
            assertNotNull(zf.getEntry(entryPath), MessageFormat.format("Entry {0} not found in the zipFile {1}", entryPath, zipFile.getName()));
        }
    }

    private static void assertZipEntryNotExists(File zipFile) throws Exception {
        try (ZipFile zf = new ZipFile(zipFile)) {
            assertNull(zf.getEntry("test-files/test.txt"));
        }
    }
}
