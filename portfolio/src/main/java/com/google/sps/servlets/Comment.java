// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.Key;

public class Comment {

    public int id;
    public String comment;
    public String firstName;
    public String lastName;
    public boolean canEdit;

    public Comment(int id, String comment, String firstName, String lastName, boolean canEdit) {
        this.id = id;
        this.comment = comment;
        this.firstName = firstName;
        this.lastName = lastName;
        this.canEdit = canEdit;
    }
}