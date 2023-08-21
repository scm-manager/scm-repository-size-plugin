/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.cloudogu.repositorysize;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.RepositoryTestData;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositorySizeCalculatorTest {

  private final Repository repository = RepositoryTestData.create42Puzzle();

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RepositoryLocationResolver locationResolver;

  @InjectMocks
  private RepositorySizeCalculator calculator;

  @Test
  void shouldReturnZeroIfDirNotFound(@TempDir Path repoPath) {
    when(locationResolver.forClass(Path.class).getLocation(repository.getId())).thenReturn(repoPath);

    assertThat(calculator.getRepoSize(repository)).isEqualTo(-1);
    assertThat(calculator.getStoreSize(repository)).isEqualTo(-1);
    assertThat(calculator.getLfsSize(repository)).isEqualTo(-1);
    assertThat(calculator.getTempSize(repository)).isEqualTo(-2);
  }

  @Test
  void shouldReturnRepoDirSize(@TempDir Path repoPath) throws IOException {
    when(locationResolver.forClass(Path.class).getLocation(repository.getId())).thenReturn(repoPath);
    Files.createDirectory(repoPath.resolve("data"));
    try (RandomAccessFile f = new RandomAccessFile(repoPath + "/data/repo_data", "rw")) {
      f.setLength(1024);
    }

    assertThat(calculator.getRepoSize(repository)).isEqualTo(1024.0);
  }

  @Test
  void shouldReturnStoreDirSize(@TempDir Path repoPath) throws IOException {
    when(locationResolver.forClass(Path.class).getLocation(repository.getId())).thenReturn(repoPath);
    Files.createDirectory(repoPath.resolve("store"));
    try (RandomAccessFile f = new RandomAccessFile(repoPath + "/store/any_store", "rw")) {
      f.setLength(512);
    }

    assertThat(calculator.getStoreSize(repository)).isEqualTo(512.0);
  }

  @Test
  void shouldReturnLfsDirSize(@TempDir Path repoPath) throws IOException {
    when(locationResolver.forClass(Path.class).getLocation(repository.getId())).thenReturn(repoPath);
    Files.createDirectory(repoPath.resolve("store"));
    Files.createDirectory(repoPath.resolve("store").resolve("blob"));
    Files.createDirectory(repoPath.resolve("store").resolve("blob").resolve("git-lfs"));
    try (RandomAccessFile f = new RandomAccessFile(repoPath + "/store/blob/git-lfs/blob.blob", "rw")) {
      f.setLength(42);
    }

    assertThat(calculator.getLfsSize(repository)).isEqualTo(42.0);
  }

  @Test
  void shouldReturnTempDirSize(@TempDir Path repoPath) throws IOException {
    when(locationResolver.forClass(Path.class).getLocation(repository.getId())).thenReturn(repoPath);
    Files.createDirectory(repoPath.resolve("work"));
    Files.createDirectory(repoPath.resolve("store"));
    Files.createDirectory(repoPath.resolve("store").resolve("blob"));
    Files.createDirectory(repoPath.resolve("store").resolve("blob").resolve("repository-export"));
    try (RandomAccessFile f1 = new RandomAccessFile(repoPath + "/store/blob/repository-export/export.zip", "rw");
         RandomAccessFile f2 = new RandomAccessFile(repoPath + "/work/work_it", "rw")) {
      f1.setLength(1);
      f2.setLength(2);
    }

    assertThat(calculator.getTempSize(repository)).isEqualTo(3.0);
  }

}
