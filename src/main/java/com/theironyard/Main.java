package com.theironyard;

import com.theironyard.service.ForumWebService;
import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import static spark.Spark.halt;

public class Main {
    static HashMap<String, User> users = new HashMap<>();
    static ArrayList<Message> messages = new ArrayList<>();

    public static void main(String[] args) throws SQLException {
        // create a server
        Server server = Server.createTcpServer("-baseDir", "./data").start();

        // created our connection
        String jdbcUrl = "jdbc:h2:" + server.getURL() + "/main";
        System.out.println(jdbcUrl);
        Connection connection = DriverManager.getConnection(jdbcUrl, "", null);

        // configure service
        ForumWebService forumWebService = new ForumWebService(connection);

        // insure the DB tables exist
        forumWebService.initializeDatabase();

        Spark.get(
                "/",
                (request, response) -> {
                    HashMap m = new HashMap();

                    Integer replyId = null;
                    if(request.queryParams("replyId") != null){
                        replyId = Integer.parseInt(request.queryParams("replyId"));
                    }

                    ArrayList<Message> threads = forumWebService.getMessagesByReplyId(replyId);

                    Session session = request.session();
                    String userName = session.attribute("userName");

                    m.put("messages", threads);
                    m.put("userName", userName);
                    m.put("replyIdNum", request.queryParams("replyId"));
                    m.put("loginFailed", request.queryParams("loginFailed"));
                    return new ModelAndView(m, "home.mustache");
                },
                new MustacheTemplateEngine()
        );

        Spark.post(
                "/login",
                (request, response) -> {
                    String userName = request.queryParams("loginName");
                    if(userName == null){
                        //throw new Exception("Login name not found.");
                        response.redirect("/?loginFailed=true");
                    }
                    String password = request.queryParams("password");
                    if(password == null){
                        //throw new Exception("password not provided.");
                        response.redirect("/?loginFailed=true");
                    }

                    User user = users.get(userName);
                    if(user == null){
                        //throw new Exception("user not found");
                        response.redirect("/?loginFailed=true");
                    }

                    if(!user.getPassword().equals(password)){
                        //throw new Exception("password incorrect");
                        response.redirect("/?loginFailed=true");
                    }

                    /*if(user == null){
                        user = new User(userName);
                        users.put(userName, user);
                    }*/

                    request.session().attribute("userName", userName);

                    response.redirect("/");
                    halt();

                    return "";
                }
        );

        Spark.get(
                "/logout",
                (request, response) -> {
                    request.session().invalidate();
                    response.redirect("/");
                    halt();
                    return "";
                }
        );

        Spark.post(
                "/post-message",
                (request, response) -> {
                    /*String message = request.queryParams("message");
                    Integer replyId = Integer.valueOf(request.queryParams("replyId"));

                    if(message.length() != 0 && replyId != null){
                        // get the last message
                        Message lastMessage = messages.get(messages.size()-1);
                        int userName = request.session().attribute("userName");
                        // create a message
                        Message newMessage = new Message(lastMessage.getId()+1, replyId, userName, message);
                        // add to messages array
                        messages.add(newMessage);

                        response.redirect(request.headers("Referer"));
                    } else {
                        throw new Exception("bad message");
                    }
                    */
                    return "";

                }
        );

    }



}
