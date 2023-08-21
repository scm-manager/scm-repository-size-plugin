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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Provider;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.web.JsonMockHttpResponse;
import sonia.scm.web.RestDispatcher;

import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
@SubjectAware(value = "trillian")
class SizeResourceTest {

  private final static Repository REPOSITORY_1 = RepositoryTestData.create42Puzzle();
  private final static Repository REPOSITORY_2 = RepositoryTestData.createHeartOfGold();

  @Mock
  private RepositorySizeCalculator sizeCalculator;
  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private Provider<ScmPathInfoStore> pathInfoStoreProvider;
  @Mock
  private ScmPathInfoStore scmPathInfoStore;

  @InjectMocks
  private SizeResource resource;

  private RestDispatcher dispatcher;

  @BeforeEach
  void initResource() {
    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);
    lenient().when(pathInfoStoreProvider.get()).thenReturn(scmPathInfoStore);
    lenient().when(scmPathInfoStore.get()).thenReturn(() -> URI.create("hitchhiker.org/scm/"));
    REPOSITORY_1.setId("1");
    REPOSITORY_2.setId("2");

    when(repositoryManager.getAll()).thenReturn(List.of(REPOSITORY_1, REPOSITORY_2));
  }

  @Test
  void shouldNotGetAnySizesWithoutPullPermissions() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/v2/repository-size/");
    JsonMockHttpResponse response = new JsonMockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsJson()).isEmpty();
  }

  @Test
  @SubjectAware(permissions = "repository:pull:1")
  void shouldGetRepoSizeForSingleRepo() throws URISyntaxException {
    mockRepoSizes(REPOSITORY_1, 1.0, 9.0, 3.0, 2.0);

    MockHttpRequest request = MockHttpRequest.get("/v2/repository-size/");
    JsonMockHttpResponse response = new JsonMockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    JsonNode repoSize = response.getContentAsJson().get(0);
    assertThat(repoSize.get("namespace").textValue()).isEqualTo(REPOSITORY_1.getNamespace());
    assertThat(repoSize.get("name").textValue()).isEqualTo(REPOSITORY_1.getName());
    assertThat(repoSize.get("totalSizeInBytes").asDouble()).isEqualTo(10.0);
    assertThat(repoSize.get("repoSizeInBytes").asDouble()).isEqualTo(1.0);
    assertThat(repoSize.get("storeSizeInBytes").asDouble()).isEqualTo(4.0);
    assertThat(repoSize.get("tempSizeInBytes").asDouble()).isEqualTo(3.0);
    assertThat(repoSize.get("lfsSizeInBytes").asDouble()).isEqualTo(2.0);
  }

  @Test
  @SubjectAware(permissions = "repository:pull:*")
  void shouldGetRepoSizeForAllRepos() throws URISyntaxException {
    mockRepoSizes(REPOSITORY_1,1.0, 9.0, 3.0, 2.0);
    mockRepoSizes(REPOSITORY_2, 50.0, 42.0, 0.0, 0.0);

    MockHttpRequest request = MockHttpRequest.get("/v2/repository-size/");
    JsonMockHttpResponse response = new JsonMockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    JsonNode json = response.getContentAsJson();

    JsonNode repoSize_1 = json.get(0);
    assertThat(repoSize_1.get("namespace").textValue()).isEqualTo(REPOSITORY_1.getNamespace());
    assertThat(repoSize_1.get("name").textValue()).isEqualTo(REPOSITORY_1.getName());
    assertThat(repoSize_1.get("totalSizeInBytes").asDouble()).isEqualTo(10.0);
    assertThat(repoSize_1.get("repoSizeInBytes").asDouble()).isEqualTo(1.0);
    assertThat(repoSize_1.get("storeSizeInBytes").asDouble()).isEqualTo(4.0);
    assertThat(repoSize_1.get("tempSizeInBytes").asDouble()).isEqualTo(3.0);
    assertThat(repoSize_1.get("lfsSizeInBytes").asDouble()).isEqualTo(2.0);

    JsonNode repoSize_2 = json.get(1);
    assertThat(repoSize_2.get("namespace").textValue()).isEqualTo(REPOSITORY_2.getNamespace());
    assertThat(repoSize_2.get("name").textValue()).isEqualTo(REPOSITORY_2.getName());
    assertThat(repoSize_2.get("totalSizeInBytes").asDouble()).isEqualTo(92.0);
    assertThat(repoSize_2.get("repoSizeInBytes").asDouble()).isEqualTo(50.0);
    assertThat(repoSize_2.get("storeSizeInBytes").asDouble()).isEqualTo(42.0);
    assertThat(repoSize_2.get("lfsSizeInBytes").asDouble()).isEqualTo(0.0);
    assertThat(repoSize_2.get("tempSizeInBytes").asDouble()).isEqualTo(0.0);
  }

  private void mockRepoSizes(Repository repository, double repoSize, double storeSize, double tempSize, double lfsSize) {
    when(sizeCalculator.getRepoSize(repository)).thenReturn(repoSize);
    when(sizeCalculator.getStoreSize(repository)).thenReturn(storeSize);
    when(sizeCalculator.getTempSize(repository)).thenReturn(tempSize);
    when(sizeCalculator.getLfsSize(repository)).thenReturn(lfsSize);
  }

}
