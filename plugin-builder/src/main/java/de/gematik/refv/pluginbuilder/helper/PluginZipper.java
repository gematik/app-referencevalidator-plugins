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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

/**
 * Handles the compression of built plugins during the <code>PluginBuilder</code> build process.
 * Sets default values for the times of <code>ZipArchiveEntries</code> to prevent misleading changes
 * between final .zip files when rebuilding the same plugin.
 */
@Slf4j
@NoArgsConstructor
@Getter
@Setter
public class PluginZipper {

  /**
   * Creates a plugin as a .zip file.
   *
   * @param sourceFolderPath The source folder where all files that should be in the final zip are.
   * @param targetFolderPath The target folder where the final plugin will be.
   * @param excludedFilesAndFolders Filenames or directory names that should be excluded from the
   *     final plugin.
   * @throws IOException If anything goes wrong during the zip IO actions.
   */
  public File zipPlugin(
      String pluginName,
      String pluginVersion,
      String sourceFolderPath,
      String targetFolderPath,
      List<String> excludedFilesAndFolders)
      throws IOException {
    File zipFile =
        new File(targetFolderPath + File.separator + pluginName + "-" + pluginVersion + ".zip");

    try (FileOutputStream fos = new FileOutputStream(zipFile);
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(fos)) {

      File sourceFolder = new File(sourceFolderPath);
      zipFolderContents(sourceFolder, "", zos, excludedFilesAndFolders);
    }
    return zipFile;
  }

  /**
   * Recursively zips the contents of a given folder.
   *
   * @param folder The folder that should be added to the zip.
   * @param parentFolder The parent folder of the folder that should be zipped.
   * @param zos The ZipOutputStream that is being used to write to the zip.
   * @param exclusionStrings List of Strings containing filenames or directory names that should be
   *     excluded from the final plugin.
   * @throws IOException If anything goes wrong during the zip IO actions.
   */
  private void zipFolderContents(
      File folder, String parentFolder, ZipArchiveOutputStream zos, List<String> exclusionStrings)
      throws IOException {
    for (File file : Objects.requireNonNull(folder.listFiles())) {
      if (checkForExclusion(file.getName(), exclusionStrings)) {
        String entryName =
            parentFolder.isEmpty()
                ? file.getName()
                : parentFolder + File.separator + file.getName();

        if (file.isDirectory()) {
          zipFolderContents(file, entryName, zos, exclusionStrings);
        } else {
          byte[] buffer = new byte[1024];
          try (FileInputStream fis = new FileInputStream(file)) {
            ZipArchiveEntry entry = new ZipArchiveEntry(entryName);

            // setting default values for entry time to prevent misleading changes between final
            // .zip files when rebuilding the same plugin
            entry.setTime(946684800000L);

            zos.putArchiveEntry(entry);

            int length;
            while ((length = fis.read(buffer)) > 0) {
              zos.write(buffer, 0, length);
            }

            zos.closeArchiveEntry();
          }
        }
      }
    }
  }

  /**
   * Checks if a given filename should be excluded or not.
   *
   * @param filename The filename to be checked.
   * @param exclusionStrings List of Strings containing filenames or directory names that should be
   *     excluded from the final plugin.
   * @return True if there are no exclusion Strings defined or the filename shouldn't be excluded.
   *     Else false.
   */
  private boolean checkForExclusion(String filename, List<String> exclusionStrings) {
    if (exclusionStrings == null || exclusionStrings.isEmpty()) return true;
    return !exclusionStrings.contains(filename);
  }
}
