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

public class UserInfo {

    public static String firstName;
    public static String lastName;
    public static boolean canVote;

    public UserInfo(String firstName, String lastName, boolean canVote) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.canVote = canVote;
    }

    public String getJson() 
    { 
        String json = "{";
        json += "\"firstName\": ";
        json += "\"" + firstName + "\"";
        json += ", ";
        json += "\"lastName\": ";
        json += "\"" + lastName + "\"";
        json += ", ";
        json += "\"canVote\": ";
        json += canVote;
        json += "}";
        return json; 
    } 
}