

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class mysql_interface {
	private Statement stmt;
	String url;
	java.sql.Connection con;
    private Logger log;
    String user, pass;
    boolean silent;
	
	
	public mysql_interface(String location, String port, String db, String user, String pass, int silent) {
		url = "jdbc:mysql://" + location + ":" + port + "/" + db;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("You appear to be missing MySQL JDBC");
			e.printStackTrace();
			return;
		}
		this.user = user;
		this.pass = pass;
		reconnect();
		if (silent > 0) {
			this.silent = true;
			return;
		}
		this.silent = false;
        log = Logger.getLogger("Minecraft");
	}
	
	public void reconnect() {
		try {
			con = (Connection) DriverManager.getConnection(url, user, pass);// + "?autoReconnect=true&user=" + user + "&password=" + pass);
		} catch (SQLException e) {
			log.info("[MineQuest] Unable to Connect to MySQL Databse");
			e.printStackTrace();
			return;
		}
		
		 try {
			stmt = (Statement) con.createStatement();
		} catch (SQLException e) {
			log.info("[MineQuest] Failed to setup MySQL Statement");
			e.printStackTrace();
		}
	}
	
	public ResultSet query(String the_query) {
		if (!silent) {
			log.info("[MineQuest] (MySQL) " + the_query);
		}
		try {
			return stmt.executeQuery(the_query);
		} catch (SQLException e) {
			log.info("[MineQuest] Failed to query database");
			e.printStackTrace();
			reconnect();
			try {
				return stmt.executeQuery(the_query);
			} catch (SQLException e1) {
				return null;
			}
		}
	}
	
	public int update(String sql) {
		if (!silent) {
			log.info("[MineQuest] (MySQL) " + sql);
		}
		try {
			return stmt.executeUpdate(sql);
		} catch (SQLException e) {
			log.info("[MineQuest] Failed to update database");
			e.printStackTrace();
			reconnect();
			try {
				return stmt.executeUpdate(sql);
			} catch (SQLException e1) {
				return 1;
			}
		}
	}
}