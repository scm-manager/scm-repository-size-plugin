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

import { mergeRepoSizes, RepositorySize } from "./size";

describe("size", () => {
  describe(mergeRepoSizes, () => {
    const defaultSizes: RepositorySize = {
      name: "",
      namespace: "",
      totalSizeInBytes: -1,
      repoSizeInBytes: -1,
      storeSizeInBytes: -1,
      lfsSizeInBytes: -1,
      tempSizeInBytes: -1
    };

    it.each([
      [[], { ...defaultSizes }],
      [
        [
          {
            ...defaultSizes,
            totalSizeInBytes: 10,
            repoSizeInBytes: 20,
            storeSizeInBytes: 30,
            lfsSizeInBytes: 40,
            tempSizeInBytes: 50
          }
        ],
        {
          ...defaultSizes,
          totalSizeInBytes: 10,
          repoSizeInBytes: 20,
          storeSizeInBytes: 30,
          lfsSizeInBytes: 40,
          tempSizeInBytes: 50
        }
      ],
      [
        [
          {
            ...defaultSizes,
            totalSizeInBytes: -1,
            repoSizeInBytes: -1,
            storeSizeInBytes: -1,
            lfsSizeInBytes: -1,
            tempSizeInBytes: -1
          }
        ],
        {
          ...defaultSizes,
          totalSizeInBytes: -1,
          repoSizeInBytes: -1,
          storeSizeInBytes: -1,
          lfsSizeInBytes: -1,
          tempSizeInBytes: -1
        }
      ],
      [
        [
          {
            ...defaultSizes,
            totalSizeInBytes: -1,
            repoSizeInBytes: -1,
            storeSizeInBytes: -1,
            lfsSizeInBytes: -1,
            tempSizeInBytes: -1
          },
          {
            ...defaultSizes,
            totalSizeInBytes: 10,
            repoSizeInBytes: 20,
            storeSizeInBytes: 30,
            lfsSizeInBytes: 40,
            tempSizeInBytes: 50
          }
        ],
        {
          ...defaultSizes,
          totalSizeInBytes: 10,
          repoSizeInBytes: 20,
          storeSizeInBytes: 30,
          lfsSizeInBytes: 40,
          tempSizeInBytes: 50
        }
      ],
      [
        [
          {
            ...defaultSizes,
            totalSizeInBytes: -1,
            repoSizeInBytes: -1,
            storeSizeInBytes: -1,
            lfsSizeInBytes: -1,
            tempSizeInBytes: -1
          },
          {
            ...defaultSizes,
            totalSizeInBytes: 10,
            repoSizeInBytes: 20,
            storeSizeInBytes: 30,
            lfsSizeInBytes: 40,
            tempSizeInBytes: 50
          },
          {
            ...defaultSizes,
            totalSizeInBytes: 5,
            repoSizeInBytes: 15,
            storeSizeInBytes: 25,
            lfsSizeInBytes: 35,
            tempSizeInBytes: 45
          }
        ],
        {
          ...defaultSizes,
          totalSizeInBytes: 15,
          repoSizeInBytes: 35,
          storeSizeInBytes: 55,
          lfsSizeInBytes: 75,
          tempSizeInBytes: 95
        }
      ]
    ])("expected sizes", (input: RepositorySize[], expected: RepositorySize) => {
      const result = mergeRepoSizes(input);
      expect(result).toEqual(expected);
    });
  });
});
