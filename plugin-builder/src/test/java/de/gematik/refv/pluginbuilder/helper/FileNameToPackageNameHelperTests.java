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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class FileNameToPackageNameHelperTests {

    @ParameterizedTest
    @CsvSource({
            "package.name-1.0.0.tgz, package.name#1.0.0",
            "package.name.with.more.text-1.0.0.tgz, package.name.with.more.text#1.0.0",
            "package.name-1.0.0-abc.tgz, package.name#1.0.0-abc",
            "package.name.with.more.text-1.0.0-abc.tgz, package.name.with.more.text#1.0.0-abc",
            })
    @Execution(ExecutionMode.CONCURRENT)
    void testFileNameToPackageName(String fileName, String expectedPackageName) {
        String packageName = FileNameToPackageNameHelper.fileNameToPackageName(fileName);
        assertThat(packageName).isEqualTo(expectedPackageName);
    }

    @Test
    void testFileNameToPackageNameThrowsException() {
        String fileName = "not.a.package.file";
        assertThatThrownBy(() -> FileNameToPackageNameHelper.fileNameToPackageName(fileName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Could not map the file name to package name: not.a.package.file");
    }
}
