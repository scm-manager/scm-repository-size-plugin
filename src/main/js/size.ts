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

import { Repository, Link } from "@scm-manager/ui-types";
import { apiClient, useJsonResource, useRepositories } from "@scm-manager/ui-api";
import { useEffect, useState } from "react";

type ConvertedSize = {
  name: string;
  value: number;
  unit: string;
};

const sizeTypeNames = [
  "totalSizeInBytes",
  "repoSizeInBytes",
  "storeSizeInBytes",
  "lfsSizeInBytes",
  "tempSizeInBytes"
] as const;

type SizeTypes = {
  [K in typeof sizeTypeNames[number]]: number;
};

export type RepositorySize = SizeTypes & { isLoading: boolean };

export type RepositorySizes = {
  [repository: string]: RepositorySize;
};

export const isNoRepositorySizeAvailable = (repositorySize: ConvertedSize) => repositorySize.value < 0;

export const useRepoSize = (repository: Repository) =>
  useJsonResource<RepositorySizes>(repository, "size", ["repository", repository.namespace, repository.name, "size"]);

export const useReposSize = () => {
  // fetch streaming json data from url
  const [data, setData] = useState<RepositorySizes>({});
  const { data: repositories, isLoading, error } = useRepositories({pageSize: 100000});

  useEffect(() => {
    if (isLoading) {
      return;
    }
    let blankData: RepositorySizes = {};
    repositories!._embedded!.repositories.forEach(repository => {
      blankData[`${repository.namespace}/${repository.name}`] = {
        totalSizeInBytes: 0,
        repoSizeInBytes: 0,
        storeSizeInBytes: 0,
        lfsSizeInBytes: -1,
        tempSizeInBytes: -1,
        isLoading: true
      };
    });
    setData(blankData);
    const loadForRepository = async () => {
      for (const repository of repositories?._embedded?.repositories ?? []) {
        await apiClient
          .get((repository._links.size as Link).href)
          .then(response => response.json())
          .then(response =>
            setData(oldData => {
              oldData[`${repository.namespace}/${repository.name}`] = { ...response, isLoading: false };
              return { ...oldData };
            })
          );
      }
    };
    loadForRepository();
  }, [repositories]);

  return {
    data,
    error: error,
    isLoading: isLoading
  };
};

export const formatSizes = (size: RepositorySize) =>
  Object.entries(size).reduce<ConvertedSize[]>((convertedSizes, [name, value]) => {
    if (typeof value === "number") {
      if (value <= 1024) {
        convertedSizes.push({ name, value, unit: " B" });
      } else if (value <= 1024 * 1024) {
        convertedSizes.push({ name, value: Math.round((value / 1024) * 100) / 100, unit: "KB" });
      } else if (value <= 1024 * 1024 * 1024) {
        convertedSizes.push({ name, value: Math.round((value / (1024 * 1024)) * 100) / 100, unit: "MB" });
      } else {
        convertedSizes.push({ name, value: Math.round((value / (1024 * 1024 * 1024)) * 100) / 100, unit: "GB" });
      }
    }
    return convertedSizes;
  }, []);

export const isDataLoading = (sizes: RepositorySizes) =>
  Object.values(sizes).find(size => size.isLoading) !== undefined;

export const mergeRepoSizes = (sizes: RepositorySizes) => {
  const mergedSizes: RepositorySize = {
    totalSizeInBytes: -1,
    repoSizeInBytes: -1,
    storeSizeInBytes: -1,
    lfsSizeInBytes: -1,
    tempSizeInBytes: -1,
    isLoading: true
  };

  Object.values(sizes).forEach(size => {
    sizeTypeNames.forEach(sizeType => {
      if (size[sizeType] < 0) {
        return;
      }

      if (mergedSizes[sizeType] < 0) {
        mergedSizes[sizeType] = size[sizeType];
      } else {
        mergedSizes[sizeType] += size[sizeType];
      }
    });
  });
  mergedSizes["isLoading"] = isDataLoading(sizes);

  return mergedSizes;
};
