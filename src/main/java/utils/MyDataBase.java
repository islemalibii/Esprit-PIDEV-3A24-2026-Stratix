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

    private MyDataBase(){
        try {
            cnx = DriverManager.getConnection(URL,USER,PASSWORD);
            System.out.println("Connected to database");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.err.println("Erreur de connexion à la base de données: " + e.getMessage());
            System.err.println("Vérifiez que MySQL est démarré et que la base 'stratix' existe.");
            // Ne pas lancer d'exception, juste logger l'erreur
            this.cnx = null;
        }

    }
    public static MyDataBase getInstance(){
        if(instance == null)
            instance = new MyDataBase();
        return instance;
    }

    public Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("Database Reconnection Error: " + e.getMessage());
        }
        return cnx;
    }
}
