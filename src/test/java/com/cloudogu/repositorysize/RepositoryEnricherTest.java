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

import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import jakarta.inject.Provider;

import java.net.URI;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
class RepositoryEnricherTest {

  private final Repository repository = RepositoryTestData.create42Puzzle();

  @Mock
  private Provider<ScmPathInfoStore> storeProvider;

  @Mock
  private HalAppender appender;

  @Mock
  private HalEnricherContext context;

  @InjectMocks
  private RepositoryEnricher enricher;

  @BeforeEach
  void initMocks() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create(""));
    lenient().when(storeProvider.get()).thenReturn(scmPathInfoStore);

    repository.setId("1");
    when(context.oneRequireByType(Repository.class)).thenReturn(repository);
  }

  @Test
  void shouldNotAppendLink() {
    enricher.enrich(context, appender);

    verifyNoInteractions(appender);
  }

  @Test
  @SubjectAware(value = "trillian", permissions = "repository:pull:1")
  void shouldAppendLink() {
    enricher.enrich(context, appender);

    verify(appender).appendLink("size", "v2/repository-size/hitchhiker/42Puzzle");
  }
}
