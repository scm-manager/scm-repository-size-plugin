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
