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
import java.io.IOException;
import java.util.ArrayList;
import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/login-status")
public class LoginStatusServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ArrayList<Object> loginStatus = new ArrayList<Object>();

        UserService userService = UserServiceFactory.getUserService();
        if(userService.isUserLoggedIn()) {
            loginStatus.add(true);
            loginStatus.add(userService.createLogoutURL("/forum.html"));
        }
        else {
            loginStatus.add(false);
            // need to set a name
            loginStatus.add(userService.createLoginURL("/name"));
        }

        Gson gson = new Gson();
        String json = gson.toJson(loginStatus);
        response.setContentType("application/json;");
        response.getWriter().println(json);
    }
}