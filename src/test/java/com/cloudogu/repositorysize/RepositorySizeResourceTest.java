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
import org.jboss.resteasy.mock.MockHttpResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
@SubjectAware(value = "trillian")
class RepositorySizeResourceTest {

  private final static Repository REPOSITORY = RepositoryTestData.create42Puzzle();

  @Mock
  private RepositorySizeCalculator sizeCalculator;
  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private Provider<ScmPathInfoStore> pathInfoStoreProvider;
  @Mock
  private ScmPathInfoStore scmPathInfoStore;

  @InjectMocks
  private RepositorySizeResource resource;

  private RestDispatcher dispatcher;

  @BeforeEach
  void initResource() {
    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);
    lenient().when(pathInfoStoreProvider.get()).thenReturn(scmPathInfoStore);
    lenient().when(scmPathInfoStore.get()).thenReturn(() -> URI.create("hitchhiker.org/scm/"));
    REPOSITORY.setId("1");

    when(repositoryManager.get(REPOSITORY.getNamespaceAndName())).thenReturn(REPOSITORY);
  }

  @Test
  void shouldNotGetRepoSizeWithoutPermission() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/v2/repository-size/" + REPOSITORY.getNamespaceAndName().toString());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
  }

  @Test
  @SubjectAware(permissions = "repository:pull:1")
  void shouldGetRepoSize() throws URISyntaxException {
    when(sizeCalculator.getRepoSize(REPOSITORY)).thenReturn(1.0);
    when(sizeCalculator.getStoreSize(REPOSITORY)).thenReturn(9.0);
    when(sizeCalculator.getTempSize(REPOSITORY)).thenReturn(2.0);
    when(sizeCalculator.getLfsSize(REPOSITORY)).thenReturn(3.0);

    MockHttpRequest request = MockHttpRequest.get("/v2/repository-size/" + REPOSITORY.getNamespaceAndName().toString());
    JsonMockHttpResponse response = new JsonMockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    JsonNode json = response.getContentAsJson();
    assertThat(json.get("namespace").textValue()).isEqualTo(REPOSITORY.getNamespace());
    assertThat(json.get("name").textValue()).isEqualTo(REPOSITORY.getName());
    assertThat(json.get("totalSizeInBytes").asDouble()).isEqualTo(10.0);
    assertThat(json.get("repoSizeInBytes").asDouble()).isEqualTo(1.0);
    assertThat(json.get("storeSizeInBytes").asDouble()).isEqualTo(4.0);
    assertThat(json.get("lfsSizeInBytes").asDouble()).isEqualTo(3.0);
    assertThat(json.get("tempSizeInBytes").asDouble()).isEqualTo(2.0);
  }

}
