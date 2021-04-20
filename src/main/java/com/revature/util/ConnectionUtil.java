package com.revature.util;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;

import org.mariadb.jdbc.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revature.controller.ClientController;

public class ConnectionUtil {

	private static Logger logger = LoggerFactory.getLogger(ConnectionUtil.class);
	
	public static Connection getConnection() throws SQLException {
		
		Driver mariaDBDriver = new Driver();
		DriverManager.registerDriver(mariaDBDriver);
		
		String username = System.getenv("db_username");
		String password = System.getenv("db_password");
		String connectionString = System.getenv("db_url");
		
		return DriverManager.getConnection(connectionString, username, password);
	}
	

	
}
