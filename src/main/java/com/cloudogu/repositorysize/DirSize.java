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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    if (!Files.exists(Path.of(this.resolveDirPath()))) {
      this.folderSize = -1;
      return;
    }
    try (Stream<Path> walk = Files.walk(Paths.get(this.resolveDirPath()))) {
      this.folderSize = walk
        .filter(Files::isRegularFile)
        .mapToLong(path -> {
          try {
            return Files.size(path);
          } catch (IOException e) {
            LOG.error("Error calculating folder size: " + path, e);
            return 0;
          }
        })
        .sum();
    } catch (IOException e) {
      LOG.error("Error calculating folder size", e);
    }
  }
}
