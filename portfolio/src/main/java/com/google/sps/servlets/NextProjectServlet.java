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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// handles the next project chart in contact.html
@WebServlet("/next-project")
public class NextProjectServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    ArrayList<Integer> projectVotes = new ArrayList<Integer>();

    projectVotes.add(getProjectVotes("Website"));
    projectVotes.add(getProjectVotes("iOS App"));
    projectVotes.add(getProjectVotes("Machine Learning"));

    response.setContentType("application/json");
    Gson gson = new Gson();
    String json = gson.toJson(projectVotes);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if(userService.isUserLoggedIn()) {
        String project = request.getParameter("project");
        int currentVotes = getProjectVotes(project);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity newEntity = new Entity("ProjectVotes", project);
        newEntity.setProperty("votes", currentVotes+1);
        datastore.put(newEntity);

        // update the user info to indicate that the user has voted and cannot vote anymore
        String userId = userService.getCurrentUser().getUserId();
        Entity userEntity = UserInfoServlet.getUserEntity(userId);

        Entity newUserEntity = new Entity("UserAccount", userId);
        newUserEntity.setProperty("id", userId);
        newUserEntity.setProperty("firstName", (String) userEntity.getProperty("firstName"));
        newUserEntity.setProperty("lastName", (String) userEntity.getProperty("lastName"));
        newUserEntity.setProperty("canVote", false);
        datastore.put(newUserEntity);
    }
    response.sendRedirect("/contact.html");
  }

  private static int getProjectVotes(String project) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    try {
        Entity entity = datastore.get(KeyFactory.createKey("ProjectVotes", project));
        return ((Long) entity.getProperty("votes")).intValue();
    }
    catch (Exception e) {
        return 0;
    }
  }
}