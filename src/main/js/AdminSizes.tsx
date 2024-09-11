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

import React, { FC } from "react";
import {
  formatSizes,
  isNoRepositorySizeAvailable,
  mergeRepoSizes,
  RepositorySizes,
  RepositorySize,
  useReposSize,
  isDataLoading
} from "./size";
import { ErrorNotification, Loading, Notification, Title } from "@scm-manager/ui-components";
import { Card, CardList, CardListBox } from "@scm-manager/ui-layout";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import styled, { keyframes } from "styled-components";

const StyledTag = styled(Card.Details.Detail.Tag)`
  width: 6rem;
  justify-content: right !important;
`;

const skeleton = keyframes`
    0% {opacity: 60%;}
    100% {opacity: 100%;}
`;
const LoadingTag = styled(Card.Details.Detail.Tag)`
  width: 6rem;
  justify-content: right !important;
  animation: ${skeleton} 1.5s ease infinite;
  animation-direction: alternate;
`;

const formatter = new Intl.NumberFormat("en-US", {
  minimumFractionDigits: 2
});

type SizeDetailProps = {
  repoSize: RepositorySize;
  emphasize?: boolean;
};

const SizeDetail: FC<SizeDetailProps> = ({ repoSize, emphasize }: SizeDetailProps) => {
  const [t] = useTranslation("plugins");
  return (
    <>
      {formatSizes(repoSize).map(size => {
        if (isNoRepositorySizeAvailable(size)) {
          return null;
        }
        return (
          <Card.Details.Detail key={size.name}>
            {({ labelId }) => (
              <>
                <Card.Details.Detail.Label id={labelId}>
                  {t(`scm-repository-size-plugin.table.${size.name}`)}
                </Card.Details.Detail.Label>
                {repoSize.isLoading ? (
                  <LoadingTag cardVariant={emphasize ? "info" : "light"} />
                ) : (
                  <StyledTag
                    cardVariant={emphasize ? "info" : "light"}
                    aria-labelledby={labelId}
                    className="has-text-right is-family-monospace"
                  >
                    {formatter.format(size.value)} {size.unit}
                  </StyledTag>
                )}
              </>
            )}
          </Card.Details.Detail>
        );
      })}
    </>
  );
};

const DataPanel: FC<{ data: RepositorySizes }> = ({ data }) => {
  const [t] = useTranslation("plugins");
  if (Object.keys(data).length === 0) {
    return <Notification type="info">{t("scm-repository-size-plugin.table.empty")}</Notification>;
  }
  return (
    <CardListBox>
      <CardList.Card rowGap="0.5rem">
        <Card.Row>
          <Card.Title>{t("scm-repository-size-plugin.mergedReposTotal")}</Card.Title>
        </Card.Row>
        <Card.Row>
          <Card.Details>
            <SizeDetail repoSize={mergeRepoSizes(data)} emphasize={true} />
          </Card.Details>
        </Card.Row>
      </CardList.Card>
      {Object.keys(data).map(repo => (
        <CardList.Card key={repo} rowGap="0.5rem">
          <Card.Row>
            <Card.Title>
              <Link to={`/repo/${repo}/info`}>{repo}</Link>
            </Card.Title>
          </Card.Row>
          <Card.Row>
            <Card.Details>
              <SizeDetail repoSize={data[repo]} />
            </Card.Details>
          </Card.Row>
        </CardList.Card>
      ))}
    </CardListBox>
  );
};

const AdminSizes: FC = () => {
  const { data, isLoading, error } = useReposSize();
  const [t] = useTranslation("plugins");

  // @ts-ignore
  return (
    <>
      <Title title={t("scm-repository-size-plugin.title")} />
      {error ? <ErrorNotification error={error} /> : null}
      <Notification type="info">{t("scm-repository-size-plugin.adminInfo")}</Notification>
      {isLoading ? <Loading /> : <DataPanel data={data} />}
    </>
  );
};

export default AdminSizes;
