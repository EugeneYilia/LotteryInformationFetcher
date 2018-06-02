package spider.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static spider.constant.Constant.*;

public class DatabaseConnection {
    private static Connection connection = null;

    private DatabaseConnection(){}
    
    static {
        try {
            Class.forName(DRIVER);
            connection = DriverManager.getConnection(DATABASE_URL,USERNAME,PASSWORD);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static Connection getConnection(){
        return connection;
    }
}
