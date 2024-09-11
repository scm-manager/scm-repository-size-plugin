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

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
