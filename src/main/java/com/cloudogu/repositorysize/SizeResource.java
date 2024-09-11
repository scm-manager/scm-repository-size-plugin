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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.web.VndMediaType;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;

@Path("v2/repository-size")
public class SizeResource {

  private final RepositorySizeCalculator sizeCalculator;
  private final RepositoryManager repositoryManager;
  private final ObjectMapper objectMapper;

  @Inject
  public SizeResource(RepositorySizeCalculator sizeCalculator, RepositoryManager repositoryManager, ObjectMapper objectMapper) {
    this.sizeCalculator = sizeCalculator;
    this.repositoryManager = repositoryManager;
    this.objectMapper = objectMapper;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "Repositories sizes",
    description = "Returns a summary of the repo sizes for all repositories which the user may pull.",
    tags = "Repository Sizes",
    operationId = "repo_sizes_get_all_sizes"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  @Path("")
  public StreamingOutput getSizes() {
    return output -> {
      JsonGenerator jsonGenerator = objectMapper.getFactory().createGenerator(output);
      jsonGenerator.writeStartArray();
      for (Repository repository : repositoryManager.getAll()) {
        if (RepositoryPermissions.pull(repository.getId()).isPermitted()) {
          jsonGenerator.writeObject(createDto(repository));
          jsonGenerator.flush();
        }
      }
      jsonGenerator.writeEndArray();
      jsonGenerator.close();
    };
  }

  private RepositorySizeDto createDto(Repository repo) {
    double repoSize = sizeCalculator.getRepoSize(repo);
    double lfsSize = sizeCalculator.getLfsSize(repo);
    double exportsSize = sizeCalculator.getTempSize(repo);
    double storeSize = sizeCalculator.getStoreSize(repo) - lfsSize - exportsSize;

    return new RepositorySizeDto(
      repo.getNamespace(),
      repo.getName(),
      repoSize + storeSize + lfsSize + exportsSize,
      repoSize,
      storeSize,
      lfsSize,
      exportsSize
    );
  }
}
