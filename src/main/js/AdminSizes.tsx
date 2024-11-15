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

import React, { FC, useEffect, useState } from "react";
import {
  formatSizes,
  isNoRepositorySizeAvailable,
  mergeRepoSizes,
  RepositorySizes,
  RepositorySize,
  useReposSize,
  sortRepoByField,
  SortingState,
  SizeType
} from "./size";
import { ErrorNotification, Loading, Notification, Title } from "@scm-manager/ui-components";
import { Card, CardList, CardListBox } from "@scm-manager/ui-layout";
import { Button, Icon } from "@scm-manager/ui-buttons";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import styled, { keyframes } from "styled-components";

const skeleton = keyframes`
  0% {
    opacity: 60%;
  }
  100% {
    opacity: 100%;
  }
`;

const FieldItem = styled.div`
  min-width: 165px;
  display: flex;
`;

const StyledTag = styled(Card.Details.Detail.Tag)`
  width: 6rem;
  justify-content: right !important;
`;

const LoadingTag = styled(Card.Details.Detail.Tag)`
  width: 6rem;
  justify-content: right !important;
  animation: ${skeleton} 1.5s ease infinite;
  animation-direction: alternate;
`;

const StyledButton = styled(Button)`
  background-color: transparent;
  border: 0;
  padding: 0;
`;

const LoadingButton = styled(Button)`
  background-color: transparent;
  border: 0;
  padding: 0;
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
          <FieldItem key={size.name}>
            <Card.Details.Detail>
              {({ labelId }: { labelId: string }) => (
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
          </FieldItem>
        );
      })}
    </>
  );
};

type SizeDetailHeaderProps = {
  repoSize: RepositorySize;
  sortField: SizeType | undefined;
  setSortField: (value: SizeType | undefined) => void;
  sortDirection: SortingState;
  setSortDirection: (value: SortingState) => void;
};

const SizeDetailHeader: FC<SizeDetailHeaderProps> = ({
  repoSize,
  setSortField,
  sortField,
  sortDirection,
  setSortDirection
}: SizeDetailHeaderProps) => {
  const [t] = useTranslation("plugins");

  const onSort = (field: SizeType) => {
    if (sortField === field) {
      if (sortDirection === "asc") {
        setSortDirection("desc");
      } else if (sortDirection === "desc") {
        setSortDirection("asc");
      }
    } else {
      setSortField(field);
      setSortDirection("desc");
    }
  };

  const onReset = () => {
    setSortDirection("unsorted");
    setSortField(undefined);
  };

  const getIconName = (field: SizeType) => {
    if (sortField === field) {
      if (sortDirection === "asc") {
        return "sort-up";
      } else if (sortDirection === "desc") {
        return "sort-down";
      }
    }
    return "sort";
  };

  return (
    <>
      {formatSizes(repoSize).map(size => {
        if (isNoRepositorySizeAvailable(size)) {
          return null;
        }
        return (
          <FieldItem key={size.name}>
            <Card.Details.Detail>
              {repoSize.isLoading ? (
                <LoadingButton />
              ) : (
                <StyledButton
                  aria-label={t(`scm-repository-size-plugin.header.${size.name}`, {
                    value: t(`scm-repository-size-plugin.header.${sortDirection as string}`)
                  })}
                  onClick={() => onSort(size.name as SizeType)}
                >
                  <p>{t(`scm-repository-size-plugin.table.${size.name}`)}</p>
                  <Icon>{getIconName(size.name as SizeType)}</Icon>
                </StyledButton>
              )}
            </Card.Details.Detail>
          </FieldItem>
        );
      })}
      <StyledButton aria-label={t("scm-repository-size-plugin.header.reset")} onClick={() => onReset()}>
        {t("scm-repository-size-plugin.header.resetButton")}
      </StyledButton>
    </>
  );
};

const DataPanel: FC<{
  data: RepositorySizes;
  sortField: SizeType | undefined;
  setSortField: (value: SizeType | undefined) => void;
  sortDirection: SortingState;
  setSortDirection: (value: SortingState) => void;
}> = ({ data, sortField, setSortField, sortDirection, setSortDirection }) => {
  const [t] = useTranslation("plugins");
  if (Object.keys(data).length === 0) {
    return <Notification type="info">{t("scm-repository-size-plugin.table.empty")}</Notification>;
  }
  return (
    <CardListBox>
      <CardList.Card rowGap="0.5rem">
        <Card.Row>
          <Card.Details>
            <SizeDetailHeader
              repoSize={mergeRepoSizes(data)}
              sortField={sortField}
              setSortField={setSortField}
              sortDirection={sortDirection}
              setSortDirection={setSortDirection}
            />
          </Card.Details>
        </Card.Row>
      </CardList.Card>
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
              {
                //@ts-expect-error
                <Link to={`/repo/${repo}/info`}>{repo}</Link>
              }
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
  const [sortField, setSortField] = useState<SizeType | undefined>(undefined);
  const [sortDirection, setSortDirection] = useState<SortingState>("unsorted");
  const [repos, setRepos] = useState(data);

  useEffect(() => {
    if (sortField) {
      const r = sortRepoByField(data, sortField, sortDirection);
      setRepos(r);
    } else {
      setRepos(data);
    }
  }, [sortField, sortDirection, data]);

  return (
    <>
      <Title title={t("scm-repository-size-plugin.title")} />
      {error ? <ErrorNotification error={error} /> : null}
      <Notification type="info">{t("scm-repository-size-plugin.adminInfo")}</Notification>
      {isLoading && repos ? (
        //@ts-expect-error
        <Loading />
      ) : (
        <DataPanel
          data={repos}
          sortField={sortField}
          setSortField={setSortField}
          sortDirection={sortDirection}
          setSortDirection={setSortDirection}
        />
      )}
    </>
  );
};

export default AdminSizes;
