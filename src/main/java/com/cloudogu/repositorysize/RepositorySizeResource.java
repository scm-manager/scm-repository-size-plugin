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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@OpenAPIDefinition(tags = {
  @Tag(name = "Repository Sizes", description = "Repository sizes endpoints")
})
@Path("v2/repository-size")
public class RepositorySizeResource {

  private final RepositorySizeCalculator sizeCalculator;
  private final RepositoryManager repositoryManager;

  @Inject
  RepositorySizeResource(RepositorySizeCalculator sizeCalculator, RepositoryManager repositoryManager) {
    this.sizeCalculator = sizeCalculator;
    this.repositoryManager = repositoryManager;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "Repository size by categories",
    description = "Returns a summary of the repo sizes by categories.",
    tags = "Repository Sizes",
    operationId = "repo_sizes_get_repo_sizes"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = @Schema(implementation = RepositorySizeDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"pullRepository\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  @Path("{namespace}/{name}")
  public RepositorySizeDto getRepositorySize(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    RepositoryPermissions.pull().check(repository);

    double repoSize = sizeCalculator.getRepoSize(repository);
    double lfsSize = sizeCalculator.getLfsSize(repository);
    double tempSize = sizeCalculator.getTempSize(repository);
    double storeSize = sizeCalculator.getStoreSize(repository) - lfsSize - tempSize;

    return new RepositorySizeDto(
      namespace,
      name,
      repoSize + storeSize + lfsSize + tempSize,
      repoSize,
      storeSize,
      lfsSize,
      tempSize
    );
  }
}
