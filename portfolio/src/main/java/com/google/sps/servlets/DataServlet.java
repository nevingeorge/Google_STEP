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
import com.google.appengine.api.datastore.Query.SortDirection;
import java.io.IOException;
import java.util.ArrayList;
import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Servlet that handles comments data
@WebServlet("/data")
public class DataServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long timestamp = System.currentTimeMillis();
        String comment = request.getParameter("comment");

        Entity commentEntity = new Entity("comment");
        commentEntity.setProperty("comment", comment);
        commentEntity.setProperty("timestamp", timestamp);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(commentEntity);

        response.sendRedirect("/contact.html");
    }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("comment").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    int limit = getLimit(request);
    int count = 0;

    ArrayList<String> commentsHistory = new ArrayList<String>();
    for(Entity entity : results.asIterable()) {
        if(count==limit)
            break;
        String comment = (String) entity.getProperty("comment");
        commentsHistory.add(comment);
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

      if(limit<0) {
        System.err.println("User input for limit is out of range: " + strLimit);
        return -1;
      }

      return limit;
  }
}
