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
