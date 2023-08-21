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
