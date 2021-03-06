package com.google.codeu.servlets;

import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

import java.io.IOException;
import java.util.List;
import java.net.URISyntaxException; 
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.codeu.data.Datastore;
import com.google.codeu.data.Message;
import com.google.gson.Gson;
import com.google.codeu.api.YelpQuery;


/**
 * Handles fetching all messages for the public feed.
 */
@WebServlet("/feed")
public class MessageFeedServlet extends HttpServlet{
  
  private Datastore datastore;

  @Override
  public void init() {
    datastore = new Datastore();
  }

  /*
   * Responds with a JSON representation of Message data for all users.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException {

    response.setContentType("application/json; charset=UTF-8");
    response.setCharacterEncoding("UTF-8"); 
    List<Message> messages = datastore.getAllMessages();
    Gson gson = new Gson();
    String term = request.getParameter("term");
    String location = request.getParameter("location");
    HashMap<String, String> paramMap = new HashMap<String, String>();
    Enumeration<String> paramNames = request.getParameterNames();
    while(paramNames.hasMoreElements()) {
        String currentName = paramNames.nextElement();
        paramMap.put(currentName,request.getParameter(currentName));
    }
    YelpQuery query = new YelpQuery(paramMap);
    String json = "";
    try {
      json = query.createQuery();//gson.toJson(messages);
    } catch (URISyntaxException e) { response.getOutputStream().println("aaa");}
    //response.getOutputStream().println(json);
    PrintWriter out = response.getWriter();
    out.println(json);
   }
}
