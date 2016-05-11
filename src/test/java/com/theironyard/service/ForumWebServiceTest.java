package com.theironyard.service;

import com.theironyard.Message;
import com.theironyard.User;
import org.h2.tools.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by doug on 5/10/16.
 */
public class ForumWebServiceTest {

    Connection connection;
    ForumWebService service;

    @Before
    public void before() throws SQLException {
        // create a server
        Server server = Server.createTcpServer("-baseDir", "./data").start();

        // created our connection
        connection = DriverManager.getConnection("jdbc:h2:" + server.getURL() + "/test", "", null);

        service = new ForumWebService(connection);
    }

    /**
     * Given a new database
     * When the database is initialized
     * Then we get two tables, User and Message
     */
    @Test
    public void whenInitializedThenUserAndMessageTablesCreated() throws SQLException {
        // arrange

        // act
        service.initializeDatabase();

        // assert
        ResultSet tables = connection.createStatement().executeQuery(
                "SELECT * \n" +
                "FROM INFORMATION_SCHEMA.TABLES \n" +
                "WHERE TABLE_SCHEMA = 'PUBLIC'");

        ArrayList<String> tableNames = new ArrayList<>();

        while(tables.next()){
            tableNames.add(tables.getString("TABLE_NAME"));
        }

        assertThat(tableNames, hasItems("USER", "MESSAGE"));
    }

    /**
     * Given an initialized database and a User
     * When a user is inserted
     * Then user's id is set
     */
    @Test
    public void whenUserInsertedThenIdSet() throws SQLException {
        // arrange
        service.initializeDatabase();
        User user = new User("Doug", "password");

        // act
        service.insertUser(user);

        // assert
        assertThat(user.getId(), not(0));
    }

    /**
     * Given an initialized database with some messages in it
     * When when I get messages by replyId
     * Then I get the appropriate set of messages.
     */
    @Test
    public void whenGetMessagesByReplyIdThenSetOfMessages() throws SQLException {
        // arrange
        service.initializeDatabase();

        // add users
        connection.createStatement().execute("INSERT INTO user VALUES (1, 'Alice', 'cats')");
        connection.createStatement().execute("INSERT INTO user VALUES (2, 'Bob', 'bob')");
        connection.createStatement().execute("INSERT INTO user VALUES (3, 'Charlie', 'password')");
        // add messages
        connection.createStatement().execute("INSERT INTO message VALUES (1, NULL, 1, 'Hello world!')");
        connection.createStatement().execute("INSERT INTO message VALUES (2, NULL, 2, 'This is another thread!')");
        connection.createStatement().execute("INSERT INTO message VALUES (3, 1, 3, 'Cool thread, Alice')");
        connection.createStatement().execute("INSERT INTO message VALUES (4, 2, 1, 'Thanks')");

        // act
        ArrayList<Message> messagesByNull = service.getMessagesByReplyId(null);
        ArrayList<Message> messagesById = service.getMessagesByReplyId(1);

        // assert
        assertThat(2, is(messagesByNull.size()));
        assertThat(1, is(messagesById.size()));

    }

    /**
     * Clean up after our tests.
     */
    @After
    public void after() throws SQLException {

        connection.close();

        File dataFolder = new File("data");
        if(dataFolder.exists()) {
            for(File file : dataFolder.listFiles()){
                if(file.getName().startsWith("test.h2.")) {
                    file.delete();
                }
            }
        }
    }


}
