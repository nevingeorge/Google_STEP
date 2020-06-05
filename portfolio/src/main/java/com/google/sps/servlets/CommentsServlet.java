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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;
import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/comments")
public class CommentsServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserService userService = UserServiceFactory.getUserService();
        if(userService.isUserLoggedIn()) {
            long timestamp = System.currentTimeMillis();
            String comment = request.getParameter("comment");
            String userId = userService.getCurrentUser().getUserId();

            Entity commentEntity = new Entity("comment");
            commentEntity.setProperty("comment", comment);
            commentEntity.setProperty("timestamp", timestamp);
            commentEntity.setProperty("id", userId);

            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            datastore.put(commentEntity);

            response.sendRedirect("/forum.html");
        }
        else {
            response.sendRedirect("/forum.html");
        }
    }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("comment").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    int limit = getLimit(request);
    int count = 0;

    // retrieve at most limit number of comments from the server
    ArrayList<String[]> commentsHistory = new ArrayList<String[]>();
    for(Entity entity : results.asIterable()) {
        if(count==limit)
            break;
            
        String[] commentInfo = new String[3];
        commentInfo[0] = (String) entity.getProperty("comment");

        Entity userEntity = UserInfoServlet.getUserEntity((String) entity.getProperty("id"));
        commentInfo[1] = (String) userEntity.getProperty("firstName");
        commentInfo[2] = (String) userEntity.getProperty("lastName");
        commentsHistory.add(commentInfo);
        count++;
    }

    Gson gson = new Gson();
    String json = gson.toJson(commentsHistory);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  private int getLimit(HttpServletRequest request) {
      String strLimit = request.getParameter("limit");
      int limit;
      try {
        limit = Integer.parseInt(strLimit);
      } catch (NumberFormatException e) {
        System.err.println("Could not convert to int: " + strLimit);
        return -1;
      }
      return limit;
  }
}