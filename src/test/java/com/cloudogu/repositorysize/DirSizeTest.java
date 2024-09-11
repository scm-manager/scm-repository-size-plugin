/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.repositorysize;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DirSizeTest {


  @Test
  void shouldCalculateDirSizeForBigFile(@TempDir Path dir) throws IOException {
    try (RandomAccessFile f = new RandomAccessFile(dir + "/test", "rw")) {

      f.setLength(1024 * 1024 * 1024);

      DirSize dirSize = new DirSize(dir.toString());

      assertThat(dirSize.inBytes()).isEqualTo(1.073741824E9);
    }
  }

  @Test
  void shouldCalculateDirSizeForSmallFile(@TempDir Path dir) throws IOException {
    try (RandomAccessFile f = new RandomAccessFile(dir + "/test", "rw")) {
      f.setLength(1024);

      DirSize dirSize = new DirSize(dir.toString());

      assertThat(dirSize.inBytes()).isEqualTo(1024);
    }
  }

  @Test
  void shouldCalculateDirSizeForEmptyFile(@TempDir Path dir) throws IOException {
    try (RandomAccessFile f = new RandomAccessFile(dir + "/test", "rw")) {
      f.setLength(0);

      DirSize dirSize = new DirSize(dir.toString());

      assertThat(dirSize.inBytes()).isZero();
    }
  }

  @Test
  void shouldCalculateDirSizeWithMultipleFiles(@TempDir Path dir) throws IOException {
    try (RandomAccessFile f1 = new RandomAccessFile(dir + "/test", "rw"); RandomAccessFile f2 = new RandomAccessFile(dir + "/test2", "rw")) {
      f1.setLength(42 * 1024 * 1024);
      f2.setLength(21 * 1024);

      DirSize dirSize = new DirSize(dir.toString());

      assertThat(dirSize.inBytes()).isEqualTo(4.4061696E7);
    }
  }

  @Test
  void shouldCalculateDirSizeWithNestedMultipleFiles(@TempDir Path dir) throws IOException {
    Files.createDirectory(Path.of(dir.toString(), "2"));
    try (RandomAccessFile f1 = new RandomAccessFile(dir + "/test", "rw"); RandomAccessFile f2 = new RandomAccessFile(dir + "/2/test", "rw")) {
      f1.setLength(1);
      f2.setLength(2);

      DirSize dirSize = new DirSize(dir.toString());

      assertThat(dirSize.inBytes()).isEqualTo(3);
    }
  }
}
