/*
 * *****************************************************************************
 *   Copyright 2014-2016 Spectra Logic Corporation. All Rights Reserved.
 *   Licensed under the Apache License, Version 2.0 (the "License"). You may not use
 *   this file except in compliance with the License. A copy of the License is located at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file.
 *   This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *   CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations under the License.
 * ***************************************************************************
 */

package com.spectralogic.ds3cli.models;

import com.spectralogic.ds3client.models.DetailedS3Object;

import java.util.Iterator;

public class GetDetailedObjectsResult implements Result {
    final private Iterable<DetailedS3Object> objects;

    public GetDetailedObjectsResult(final Iterable<DetailedS3Object> objects) {
        this.objects = objects;
    }

    public Iterable<DetailedS3Object> getObjIterator() {
        return this.objects;
    }
}