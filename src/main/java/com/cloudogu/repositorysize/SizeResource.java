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

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

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
