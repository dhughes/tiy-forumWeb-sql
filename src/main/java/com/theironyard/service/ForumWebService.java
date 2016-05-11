package com.theironyard.service;

import com.theironyard.Message;
import com.theironyard.User;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by doug on 5/10/16.
 */
public class ForumWebService {

    private final Connection connection;

    public ForumWebService(Connection connection) {
        this.connection = connection;
    }

    /**
     * This method creates our tables.
     */
    public void initializeDatabase() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS user (id IDENTITY, name VARCHAR, password VARCHAR)");
        statement.execute("CREATE TABLE IF NOT EXISTS message (id IDENTITY, replyId int, authorId INT, text VARCHAR)");
    }

    /**
     * This inserts a new user into the database and updates the instance's id
     * @param user
     */
    public void insertUser(User user) throws SQLException {
        // insert the new user
        PreparedStatement statement = connection.prepareStatement("INSERT INTO user VALUES (NULL, ?, ?)");
        statement.setString(1, user.getName());
        statement.setString(2, user.getPassword());
        statement.executeUpdate();

        // get the generated id
        ResultSet resultSet = statement.getGeneratedKeys();
        resultSet.next(); // read the first line of results

        // set the generated id into my user
        user.setId(resultSet.getInt(1));
    }

    public ArrayList<Message> getMessagesByReplyId(Integer replyId) throws SQLException {
        // declare our statement
        PreparedStatement statement;
        // create SQL string
        String sql = "SELECT m.*, u.NAME as authorName, u.PASSWORD as authorPassword " +
                "FROM message m JOIN user as u " +
                "   ON m.authorId = u.id " +
                " WHERE replyId " + (replyId == null ? "is NULL" : " = ?" + " " +
                "");
        // set the string as the statement's query
        statement = connection.prepareStatement(sql);
        // if it's not null then I set the id value
        if(replyId != null) statement.setInt(1, replyId);

        // execute the query
        ResultSet resultSet = statement.executeQuery();

        // parse the results
        ArrayList<Message> messages = new ArrayList<>();

        while(resultSet.next()){
            User author = new User(
                    resultSet.getString("authorName"),
                    resultSet.getString("authorPassword")
            );
            author.setId(resultSet.getInt("authorId"));

            Message message = new Message(
                    resultSet.getInt("id"),
                    resultSet.getInt("replyId"),
                    author,
                    resultSet.getString("text")
            );

            messages.add(message);
        }

        return messages;
    }
}
