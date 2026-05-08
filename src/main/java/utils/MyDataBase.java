package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    private static MyDataBase instance;
    private static final String URL = "jdbc:mysql://localhost:3306/stratix1";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private Connection cnx;

    private MyDataBase() {
        connect();
    }

    private void connect() {
        try {
            this.cnx= DriverManager.getConnection(URL,USER,PASSWORD);
            System.out.println("connected...");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());;
        }

    }

public static MyDataBase getInstance(){
    try {
        if (instance == null || instance.getCnx() == null || instance.getCnx().isClosed()) {
            instance = new MyDataBase();
        }
    } catch (SQLException e) {
        System.err.println("Failed to reconnect to database: " + e.getMessage());
    }
    return instance;
}

    public Connection getCnx() {
        return cnx;
    }
}
