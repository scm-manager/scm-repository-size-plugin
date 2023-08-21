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

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;

import javax.inject.Inject;
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
