package mamt.project.newapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class ErpNextDbConfig {

    @Value("${erpnext.db.url}")
    private String dbUrl;

    @Value("${erpnext.db.username}")
    private String dbUsername;

    @Value("${erpnext.db.password}")
    private String dbPassword;

    @Bean
    public Connection erpNextDbConnection() throws SQLException {
        try {
            // Charge explicitement le driver MariaDB
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MariaDB JDBC Driver not found", e);
        }
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }
}