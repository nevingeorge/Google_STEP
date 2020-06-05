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
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/name")
public class NameServlet extends HttpServlet {

  // directs users who need to set a name to name.html, which contains a name input form
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");
    
    UserService userService = UserServiceFactory.getUserService();
    if(userService.isUserLoggedIn()) {
        Entity userEntity = UserInfoServlet.getUserEntity(userService.getCurrentUser().getUserId());
      
        // need to set a name
        if(((String) userEntity.getProperty("firstName")).equals("") && ((String) userEntity.getProperty("lastName")).equals("")) {
            response.sendRedirect("/name.html");
        }
        else {
            response.sendRedirect("/contact.html");
        }
    } 
    else {
        response.sendRedirect("/contact.html");
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if(userService.isUserLoggedIn()) {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String userId = userService.getCurrentUser().getUserId();

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity userEntity = new Entity("UserAccount", userId);
        userEntity.setProperty("id", userId);
        userEntity.setProperty("firstName", firstName);
        userEntity.setProperty("lastName", lastName);
        userEntity.setProperty("canVote", true);
        datastore.put(userEntity);

        response.sendRedirect("/contact.html");
    }
    else {
        response.sendRedirect("/name");
    }
  }
}