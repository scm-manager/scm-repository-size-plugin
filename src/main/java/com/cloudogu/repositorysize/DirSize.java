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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DirSize {
  private static final Logger LOG = LoggerFactory.getLogger(DirSize.class);

  private final String dirPath;
  private double folderSize;

  public DirSize(String dirPath) {
    this.dirPath = dirPath;
    calculate();
  }

  public double inBytes() {
    return this.folderSize;
  }

  private String resolveDirPath() {
    return this.dirPath;
  }

  private void calculate() {
    this.folderSize = 0;
    if (!Files.exists(Path.of(this.resolveDirPath()))) {
      this.folderSize = -1;
      return;
    }
    try (Stream<Path> walk = Files.walk(Paths.get(this.resolveDirPath()))) {
      List<Path> result = walk
        .filter(Files::isRegularFile)
        .collect(Collectors.toList());

      result.forEach(path -> {
        try {
          this.folderSize += Files.size(path);
        } catch (IOException e) {
          LOG.error("Error calculating folder size: " + path, e);
        }
      });
    } catch (IOException e) {
      LOG.error("Error calculating folder size", e);
    }
  }
}
