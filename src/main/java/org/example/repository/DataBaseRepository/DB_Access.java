package org.example.repository.DataBaseRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class DB_Access {

    private Connection connection;
    private final String url, password, usernameDB;

    public DB_Access(String url, String usernameDB, String password) {
        this.url = url;
        this.usernameDB = usernameDB;
        this.password = password;
    }

    public void createConnection() throws SQLException
    {
        connection=DriverManager.getConnection(url,usernameDB,password);
    }

    public PreparedStatement createStatement(String statement) throws SQLException
    {
        return connection.prepareStatement(statement);
    }

/*    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsernameDB() {
        return usernameDB;
    }

    public void setUsernameDB(String usernameDB) {
        this.usernameDB = usernameDB;
    }*/
}
