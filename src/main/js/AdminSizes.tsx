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

import React, { FC } from "react";
import { formatSizes, isNoRepositorySizeAvailable, mergeRepoSizes, RepositorySize, useReposSize } from "./size";
import { ErrorNotification, Loading, Notification, Title } from "@scm-manager/ui-components";
import { Card, CardList, CardListBox } from "@scm-manager/ui-layout";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";

type SizeDetailProps = {
  repoSize: RepositorySize;
};

const SizeDetail: FC<SizeDetailProps> = ({ repoSize }: SizeDetailProps) => {
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
                <Card.Details.Detail.Tag aria-labelledby={labelId}>
                  {size.value} {size.unit}
                </Card.Details.Detail.Tag>
              </>
            )}
          </Card.Details.Detail>
        );
      })}
    </>
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
      {data && !isLoading ? (
        <CardListBox>
          {data.length > 0 && (
            <CardList.Card rowGap="0.5rem">
              <Card.Row>
                <Card.Title>{t("scm-repository-size-plugin.mergedReposTotal")}</Card.Title>
              </Card.Row>
              <Card.Row>
                <Card.Details>
                  <SizeDetail repoSize={mergeRepoSizes(data)} />
                </Card.Details>
              </Card.Row>
            </CardList.Card>
          )}
          {data.length > 0 ? (
            data.map(repoSizes => (
              <CardList.Card key={repoSizes.name} rowGap="0.5rem">
                <Card.Row>
                  <Card.Title>
                    <Link to={`/repo/${repoSizes.namespace}/${repoSizes.name}/info`}>
                      {`${repoSizes.namespace}/${repoSizes.name}`}
                    </Link>
                  </Card.Title>
                </Card.Row>
                <Card.Row>
                  <Card.Details>
                    <SizeDetail repoSize={repoSizes} />
                  </Card.Details>
                </Card.Row>
              </CardList.Card>
            ))
          ) : (
            <Notification type="info">{t("scm-repository-size-plugin.table.empty")}</Notification>
          )}
        </CardListBox>
      ) : (
        <Loading />
      )}
    </>
  );
};

export default AdminSizes;
