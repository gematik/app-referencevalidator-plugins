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

package de.gematik.refv.pluginbuilder.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.gematik.refv.SupportedValidationModule;
import de.gematik.refv.pluginbuilder.exceptions.PluginIdentifierException;
import de.gematik.refv.plugins.configuration.v2.PluginDefinition;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PluginDefinitionFactory {

  public static PluginDefinition createFrom(InputStream config)
      throws IOException, PluginIdentifierException {
    ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
    PluginDefinition configData;
    try (config) {
      try {
        String yamlContent = new String(config.readAllBytes());

        var jsonMap = objectMapper.readValue(yamlContent, Map.class);
        var specVersion = jsonMap.get("configSpecVersion");
        if (specVersion == null)
          throw new IllegalArgumentException("Could not determine config file spec version");

        if (specVersion.toString().startsWith("1.")) {
          throw new IllegalArgumentException(
              "Config file spec version 1.x is no longer supported. Please update your config.yaml to version 2.x");
        } else if (specVersion.toString().startsWith("2.")) {
          configData =
              objectMapper.readValue(
                  yamlContent, de.gematik.refv.plugins.configuration.v2.PluginDefinition.class);
        } else
          throw new IllegalArgumentException(
              "Unsupported config file spec version: " + specVersion);
      } catch (Exception e) {
        throw new IllegalArgumentException(
            "Could not parse config.yaml. Please ensure it is a valid YAML file and follows the PluginDefinition schema.",
            e);
      }

      var isSupportedModuleConflict =
          SupportedValidationModule.fromString(configData.getId()).isPresent();
      if (isSupportedModuleConflict)
        throw new PluginIdentifierException(
            "Plugin ID "
                + configData.getId()
                + " is already in use. Please choose another one for the plugin");

      return configData;
    }
  }

  public static PluginDefinition createFrom(String configFilePath)
      throws IOException, PluginIdentifierException {
    File configFile = new File(configFilePath);

    if (!configFile.exists()) {
      throw new FileNotFoundException("config.yaml is missing");
    }

    return PluginDefinitionFactory.createFrom(new FileInputStream(configFile));
  }
}
