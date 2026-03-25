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

import { Repository } from "@scm-manager/ui-types";
import { useIndex, useJsonResource } from "@scm-manager/ui-api";
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
  "tempSizeInBytes",
] as const;

export type SizeType = (typeof sizeTypeNames)[number];

const SORTING_VALUES = ["asc", "desc", "unsorted"] as const;

export type SortingState = (typeof SORTING_VALUES)[number];

type SizeTypes = {
  [K in (typeof sizeTypeNames)[number]]: number;
};

type BackendSizes = SizeTypes & { name: string; namespace: string };

export type RepositorySize = SizeTypes;

export type RepositorySizes = {
  [repository: string]: RepositorySize;
};

export const isNoRepositorySizeAvailable = (repositorySize: ConvertedSize) => repositorySize.value < 0;

export const useRepoSize = (repository: Repository) =>
  useJsonResource<RepositorySizes>(repository, "size", ["repository", repository.namespace, repository.name, "size"]);

export const useReposSize = () => {
  // fetch streaming json data from url
  const [data, setData] = useState<RepositorySizes>({});
  const { data: index } = useIndex();
  const {
    data: sizes,
    isLoading,
    error,
  } = useJsonResource<BackendSizes[]>(index!, "repository-size", ["repository-size"]);

  useEffect(() => {
    if (isLoading) {
      return;
    }
    const data: RepositorySizes = {};
    sizes!.forEach((repository) => {
      data[`${repository.namespace}/${repository.name}`] = repository;
    });
    setData(data);
  }, [sizes]);

  return {
    data,
    error: error,
    isLoading: isLoading,
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

export const mergeRepoSizes = (sizes: RepositorySizes) => {
  const mergedSizes: RepositorySize = {
    totalSizeInBytes: -1,
    repoSizeInBytes: -1,
    storeSizeInBytes: -1,
    lfsSizeInBytes: -1,
    tempSizeInBytes: -1,
  };

  Object.values(sizes).forEach((size) => {
    sizeTypeNames.forEach((sizeType) => {
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

  return mergedSizes;
};

export const sortRepoByField = (repos: RepositorySizes, field: SizeType, sorting: SortingState) => {
  if (sorting === "unsorted") {
    return repos;
  }
  return Object.fromEntries(
    Object.entries(repos).sort((a, b) => {
      if (sorting === "asc") {
        return a[1][field] - b[1][field];
      } else {
        return b[1][field] - a[1][field];
      }
    }),
  );
};
