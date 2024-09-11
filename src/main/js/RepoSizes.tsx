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

import { ErrorNotification, LabelWithHelpIcon, SmallLoadingSpinner } from "@scm-manager/ui-components";
import { Repository } from "@scm-manager/ui-types";
import React, { FC } from "react";
import { formatSizes, isNoRepositorySizeAvailable, useRepoSize } from "./size";
import { useTranslation } from "react-i18next";

type Props = {
  repository: Repository;
};

const RepoSizes: FC<Props> = ({ repository }) => {
  const { data, isLoading, error } = useRepoSize(repository);
  const [t] = useTranslation("plugins");

  if (error) {
    return <ErrorNotification error={error} />;
  }

  return (
    <tr>
      <th>
        <LabelWithHelpIcon
          label={t("scm-repository-size-plugin.table.key")}
          helpText={t("scm-repository-size-plugin.repoInfo")}
        />
      </th>
      <td>
        {!data || isLoading ? (
          <SmallLoadingSpinner />
        ) : (
          formatSizes(data).map(s => {
            if (isNoRepositorySizeAvailable(s)) {
              return null;
            }
            return (
              <div key={s.name}>
                <span className="has-text-weight-bold">{t(`scm-repository-size-plugin.table.${s.name}`)}:</span>{" "}
                {s.value} {s.unit}
              </div>
            );
          })
        )}
      </td>
    </tr>
  );
};

export default RepoSizes;
