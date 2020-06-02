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

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    
    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
        String userId = userService.getCurrentUser().getUserId();
        String firstName = getFirstName(userId);
        String lastName = getLastName(userId);
      
        // need to set a name
        if(firstName.equals("") && lastName.equals("")) {
            out.println("<h1>Enter your name</h1>");
            out.println("<form method=\"POST\" action=\"/name\">");
            out.println("<p>First name:</p>");
            out.println("<input name=\"firstName\" value=\"" + firstName + "\" />");
            out.println("<p>Last name:</p>");
            out.println("<input name=\"lastName\" value=\"" + lastName + "\" />");
            out.println("<br/>");
            out.println("<button>Submit</button>");
            out.println("</form>");
        }
        else {
            response.sendRedirect("/contact.html");
        }
    } 
    else {
        String loginUrl = userService.createLoginURL("/name");
        out.println("<p>Login <a href=\"" + loginUrl + "\">here</a>.</p>");
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/name");
      return;
    }

    String firstName = request.getParameter("firstName");
    String lastName = request.getParameter("lastName");
    String id = userService.getCurrentUser().getUserId();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity entity = new Entity("UserInfo", id);
    entity.setProperty("id", id);
    entity.setProperty("firstName", firstName);
    entity.setProperty("lastName", lastName);
    datastore.put(entity);

    response.sendRedirect("/contact.html");
  }

  // returns the first name of the user with id, or empty String if the user has not set a nickname.
  public static String getFirstName(String id) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("UserInfo").setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      return "";
    }
    String firstName = (String) entity.getProperty("firstName");
    return firstName;
  }

  // returns the last name of the user with id, or empty String if the user has not set a nickname.
  public static String getLastName(String id) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("UserInfo").setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      return "";
    }
    String lastName = (String) entity.getProperty("lastName");
    return lastName;
  }
}