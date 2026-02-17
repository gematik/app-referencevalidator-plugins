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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.refv.pluginbuilder.exceptions.PluginTestFailedException;
import de.gematik.refv.plugins.configuration.FhirPackage;
import java.io.File;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PluginTesterTests {

  private PluginTester pluginTester;

  @BeforeEach
  @SneakyThrows
  void setUp() {
    String packageFolderPath = "src/test/resources/package/";
    FhirPackage packageReference = new FhirPackage("minimalvalidationmodule.test", "1.0.0");
    pluginTester = new PluginTester(packageFolderPath, List.of(packageReference));
  }

  @Test
  @SneakyThrows
  void testTestPlugin() {
    String pluginFilePath = "src/test/resources/plugins/valmodule-isik1.zip";
    boolean testedSuccessful = pluginTester.testPlugin(new File(pluginFilePath));
    assertTrue(testedSuccessful);
  }

  @Test
  @SneakyThrows
  void testTestPluginWithExternalTestFiles() {
    String pluginFilePath = "src/test/resources/plugins/valmodule-isik1.zip";
    String externalTestFilesPath = "src/test/resources/plugins/test-files";
    boolean testedSuccessful =
        pluginTester.testPlugin(new File(pluginFilePath), new File(externalTestFilesPath));
    assertTrue(testedSuccessful);
  }

  @Test
  @SneakyThrows
  void testTestPluginWithFailingTestInstances() {
    String pluginFilePath = "src/test/resources/plugins/plugin-with-invalid-test-instances.zip";
    boolean testedSuccessful = pluginTester.testPlugin(new File(pluginFilePath));
    assertFalse(testedSuccessful);
  }

  @Test
  @SneakyThrows
  void testTestPluginWithMissingTestFilesDirThrowsPluginTestFailedException() {
    String pluginFilePath = "src/test/resources/plugins/test-plugin-missing-test-files-dir.zip";
    Assertions.assertThrows(
        PluginTestFailedException.class, () -> pluginTester.testPlugin(new File(pluginFilePath)));
  }
}
