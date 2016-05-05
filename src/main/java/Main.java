import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.ArrayList;
import java.util.HashMap;

import static spark.Spark.halt;

public class Main {
    static HashMap<String, User> users = new HashMap<>();
    static ArrayList<Message> messages = new ArrayList<>();

    public static void main(String[] args) {
        addTestUsers();
        addTestMessages();

        Spark.get(
                "/",
                (request, response) -> {
                    String replyId = request.queryParams("replyId");
                    int replyIdNum = -1;
                    if (replyId != null) {
                        replyIdNum = Integer.valueOf(replyId);
                    }

                    HashMap m = new HashMap();
                    ArrayList<Message> threads = new ArrayList<>();

                    for (Message message : messages) {
                        if (message.replyId == replyIdNum) {
                            threads.add(message);
                        }
                    }

                    Session session = request.session();
                    String userName = session.attribute("userName");

                    m.put("messages", threads);
                    m.put("userName", userName);
                    m.put("replyIdNum", replyIdNum);
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
                    String message = request.queryParams("message");
                    Integer replyId = Integer.valueOf(request.queryParams("replyId"));

                    if(message.length() != 0 && replyId != null){
                        // get the last message
                        Message lastMessage = messages.get(messages.size()-1);
                        String userName = request.session().attribute("userName");
                        // create a message
                        Message newMessage = new Message(lastMessage.getId()+1, replyId, userName, message);
                        // add to messages array
                        messages.add(newMessage);

                        response.redirect(request.headers("Referer"));
                    } else {
                        throw new Exception("bad message");
                    }

                    return "";

                }
        );

    }

    static void addTestUsers() {
        users.put("Alice", new User("Alice", "cats"));
        users.put("Bob", new User("Bob", "bob"));
        users.put("Charlie", new User("Charlie", "password"));
    }

    static void addTestMessages() {
        messages.add(new Message(0, -1, "Alice", "Hello world!"));
        messages.add(new Message(1, -1, "Bob", "This is another thread!"));
        messages.add(new Message(2, 0, "Charlie", "Cool thread, Alice."));
        messages.add(new Message(3, 2, "Alice", "Thanks"));
        messages.add(new Message(4, -1, "Doug", "Bob is an idiot."));
    }


}
