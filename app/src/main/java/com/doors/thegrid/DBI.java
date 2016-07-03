package com.doors.thegrid;

/**
 * Database Interface - connects to the a database on nightmare
 * Uses JDBC
 * **/



import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.*;

public class DBI {

	private Connection conn;
	private String[] jdbcCredentials = new String[4];

	public DBI() {
		loadCredentials();
	}

	private void loadCredentials() {
		/**
		 * file should be stored as:
		 * line1: jdbc driver
		 * line2: database url
		 * line3: username
		 * line4: password
		 */

		FileReader fr = null;
		try {
			fr = new FileReader("data_store_procedures.txt");
			BufferedReader br = new BufferedReader(fr);
			int lineNumber = 0;
			for(String s = br.readLine(); s != null; s = br.readLine()) {
				jdbcCredentials[lineNumber] = s;
			}
			br.close();
		} catch (Exception e) {
			System.out.println("data_store_procedures.txt file couldn't be read, check that out");
			e.printStackTrace();
		}
	}

	private void startConnection() {
		try {
			Class.forName(jdbcCredentials[0]);
			conn = DriverManager.getConnection(jdbcCredentials[1], jdbcCredentials[2],
					jdbcCredentials[3]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getData(String usr) {
		Statement stat = null;
		String fin = "";
		try {
			startConnection();
			stat = conn.createStatement();
			String sql = "select userID, location, room, deviceID from doors where userID like '" 
						+ usr + "'";
			ResultSet rs = stat.executeQuery(sql);
			while(rs.next()) {
				fin = fin + rs.getString("userID") + "," + rs.getString("location") + "," +
						rs.getString("room") + "," + rs.getString("deviceID") + ";;";
			}
			rs.close();
			stat.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				stat.close();
			} catch (SQLException se) {
			} try {
				if(conn!=null) 
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
		return fin;
	}
	
	public String authenticate(String user, String pwd) {
		Statement stat = null;
		String fin = "";
		try {
			startConnection();
			stat = conn.createStatement();
			String sql = "select password from door_users where userID like '" 
						+ user +"'";
			ResultSet rs = stat.executeQuery(sql);
			while(rs.next()) {
				fin = rs.getString("password");
			}
			if(fin.isEmpty()) return "user does not exist";
			rs.close();
			stat.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				stat.close();
			} catch (SQLException se) {
			} try {
				if(conn!=null) 
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
		if(BCrypt.checkpw(pwd, fin)) {
			return "success";
		} 
		return "failed to login";
	}
	
	
	public void registerUser(String usr, String pwd) {
		PreparedStatement stat = null;
		try {
			startConnection();
			pwd = BCrypt.hashpw(pwd, BCrypt.gensalt(12));
			String sql = "insert into door_users (userID, password) values (?,?)";
			stat = conn.prepareStatement(sql);
			stat.setString(1, usr);
			stat.setString(2, pwd);
			stat.executeUpdate();
			stat.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				stat.close();
			} catch (SQLException se) {
			} try {
				if(conn!=null) 
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}

	public void appendRow(String usr, String loc, String room, String dev) {
		PreparedStatement stat = null;
		try {
			startConnection();
			String sql = "insert into doors (userID, location, room, deviceID) values (?,?,?,?)";
			stat = conn.prepareStatement(sql);
			stat.setString(1, usr);
			stat.setString(2, loc);
			stat.setString(3, room);
			stat.setString(4, dev);
			stat.executeUpdate();
			stat.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				stat.close();
			} catch (SQLException se) {
			} try {
				if(conn!=null) 
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}
}