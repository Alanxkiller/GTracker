package com.example.gasolinerafirebase;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Objects;

public class ConnectionClass {
    protected static String db = "gtracker";
    protected static String ip = "192.168.1.109";
    protected static String port = "3306";
    protected static String username = "Master";
    protected static String password = "Master";

    public Connection CONN(){
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String connectionString = "jdbc:mysql://"+ip + ":" +port + "/" + db;
            conn = DriverManager.getConnection(connectionString,username,password);

        } catch (Exception e){
            Log.e("ERRO", Objects.requireNonNull(e.getMessage()));
        }
        return conn;
    }

}
