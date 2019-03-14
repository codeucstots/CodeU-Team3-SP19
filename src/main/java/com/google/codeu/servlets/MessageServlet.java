/*
 * Copyright 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.codeu.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.codeu.data.Datastore;
import com.google.codeu.data.Message;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/** Handles fetching and saving {@link Message} instances. */
@WebServlet("/messages")
public class MessageServlet extends HttpServlet {

  private Datastore datastore;

  @Override
  public void init() {
    datastore = new Datastore();
  }

  /**
   * Responds with a JSON representation of {@link Message} data for a specific user. Responds with
   * an empty array if the user is not provided.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    response.setContentType("application/json");

    String user = request.getParameter("user");
    String recipient = request.getParameter("recipient");

    if (user == null || user.equals("")) {
      // Request is invalid, return empty array
      response.getWriter().println("[]");
      return;
    }

    List<Message> messages = datastore.getMessages(user);
    Gson gson = new Gson();
    String json = gson.toJson(messages);

    response.getWriter().println(json);
  }

  /** Stores a new {@link Message}. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    String user, user_text, regex_string, replacement_string, images_replaced_text;

    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/index.html");
      return;
    }

    user = userService.getCurrentUser().getEmail();
    user_text = Jsoup.clean(request.getParameter("text"), Whitelist.none());

    regex_string= "(https?://\\S+\\.(png | jpg))";
    //FIXME: Compiler refuses to accept this next line
    //TODO: ADD THE FOLLOWING COMMENT TO APL NOTEBOOK for furture reference
    //TODO: Add maven instructions and bash call from inside fish to APL notebook
    /** 
     * $1 provides characters inside the capturing group,
     * which is passed into the replaceAll() function.
     */

    replacement_string = "<img src = \"$1\/>";
    images_replaced_text = user_text.replaceAll(regex_string, replacement_string);

    String recipient = request.getParameter("recipient");

    /**
     * TODO
     *    Replaced  Message message = new Message(user, user_text, recipient);
     *    With      Message message = new Message(user, images_replaced_text, recipient);
     *    
     *    Make sure we don't need the old message
     */
    Message message = new Message(user, images_replaced_text, recipient);
    datastore.storeMessage(message);

    response.sendRedirect("/user-page.html?user=" + recipient);
  }
}
