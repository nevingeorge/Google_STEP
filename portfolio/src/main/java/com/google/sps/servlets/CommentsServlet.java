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
            // get the comment id number
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Query query = new Query("numComments");
            Entity numCommentsEntity = datastore.prepare(query).asSingleEntity();
            int numComments = 1;
            if(numCommentsEntity != null) {
                numComments = (int) ((long) numCommentsEntity.getProperty("number"));
                numCommentsEntity.setProperty("number", ++numComments);
                datastore.put(numCommentsEntity);
            }
            else {
                Entity newNumCommentsEntity = new Entity("numComments");
                newNumCommentsEntity.setProperty("number", numComments);
                datastore.put(newNumCommentsEntity);
            }

            long timestamp = System.currentTimeMillis();
            String comment = request.getParameter("comment");
            String userId = userService.getCurrentUser().getUserId();

            Entity commentEntity = new Entity("comment", numComments);
            commentEntity.setProperty("commentId", numComments);
            commentEntity.setProperty("comment", comment);
            commentEntity.setProperty("timestamp", timestamp);
            commentEntity.setProperty("userId", userId);

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

    String currentUserId = "";
    UserService userService = UserServiceFactory.getUserService();
    if(userService.isUserLoggedIn()) {
        currentUserId = userService.getCurrentUser().getUserId();
    }

    int limit = getLimit(request);
    int count = 0;

    // retrieve at most limit number of comments from the server
    ArrayList<Comment> commentsHistory = new ArrayList<Comment>();
    for(Entity entity : results.asIterable()) {
        if(count==limit)
            break;
        
        int commentId = (int) ((long) entity.getProperty("commentId"));
        String comment = (String) entity.getProperty("comment");
        String userId = (String) entity.getProperty("userId");
        Entity userEntity = UserInfoServlet.getUserEntity(userId);
        String firstName = (String) userEntity.getProperty("firstName");
        String lastName = (String) userEntity.getProperty("lastName");

        boolean canEdit = false;
        if(userId.equals(currentUserId)) {
            canEdit = true;
        }
        
        Comment commentObject = new Comment(commentId, comment, firstName, lastName, canEdit);
        commentsHistory.add(commentObject);
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