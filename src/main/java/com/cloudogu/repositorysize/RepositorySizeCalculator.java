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

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;

import jakarta.inject.Inject;
import java.nio.file.Path;

public class RepositorySizeCalculator {

  private static final String STORE = "store";
  private static final String BLOB = "blob";

  private final RepositoryLocationResolver locationResolver;

  @Inject
  public RepositorySizeCalculator(RepositoryLocationResolver locationResolver) {
    this.locationResolver = locationResolver;
  }

  public double getRepoSize(Repository repository) {
    String dirPath = locationResolver.forClass(Path.class).getLocation(repository.getId()).resolve("data").toString();

    return new DirSize(dirPath).inBytes();
  }

  public double getStoreSize(Repository repository) {
    String repoPath = locationResolver.forClass(Path.class).getLocation(repository.getId()).resolve(STORE).toString();

    return new DirSize(repoPath).inBytes();
  }

  public double getLfsSize(Repository repository) {
    String repoLFSPath = locationResolver.forClass(Path.class).getLocation(repository.getId()).resolve(STORE).resolve(BLOB).resolve("git-lfs").toString();
    return new DirSize(repoLFSPath).inBytes();
  }

  public double getTempSize(Repository repository) {
    String repoExportPath = locationResolver.forClass(Path.class).getLocation(repository.getId()).resolve(STORE).resolve(BLOB).resolve("repository-export").toString();
    String repoWorkDirPath = locationResolver.forClass(Path.class).getLocation(repository.getId()).resolve("work").toString();

    return new DirSize(repoExportPath).inBytes() + new DirSize(repoWorkDirPath).inBytes();
  }
}
