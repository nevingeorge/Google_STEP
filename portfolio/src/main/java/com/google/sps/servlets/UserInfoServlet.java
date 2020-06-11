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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/user-info")
public class UserInfoServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserService userService = UserServiceFactory.getUserService();
        if(userService.isUserLoggedIn()) {
            Entity userEntity = getUserEntity(userService.getCurrentUser().getUserId());
            UserInfo userInfo = new UserInfo((String) userEntity.getProperty("firstName"), (String) userEntity.getProperty("lastName"), (boolean) userEntity.getProperty("canVote"));
            Gson gson = new Gson();
            String json = gson.toJson(userInfo);
            response.setContentType("application/json;");
            response.getWriter().println(json);
        }
        else {
            response.sendRedirect("/forum.html");
        }
    }

    public static Entity getUserEntity(String userId) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("UserAccount").setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, userId));
        PreparedQuery results = datastore.prepare(query);
        Entity userEntity =  results.asSingleEntity();
       
        if(userEntity == null) {
            userEntity = new Entity("UserAccount", userId);
            userEntity.setProperty("id", userId);
            userEntity.setProperty("firstName", "");
            userEntity.setProperty("lastName", "");
            userEntity.setProperty("canVote", false);
            datastore.put(userEntity);
        }
        return userEntity;
    }
}