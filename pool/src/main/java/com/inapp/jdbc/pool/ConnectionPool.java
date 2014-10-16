package com.inapp.jdbc.pool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Vector;

/**
 * This class creates a JDBC connection pool. It will automatically  
 * clean up the connection and maintain the pool with a predefined 
 * number of connections.
 * 
 * @author arun
 * @date 18-Sep-2014
 * @project pool
 */
public class ConnectionPool implements Runnable {

	private static boolean driverLoaded = false;
	
	private static int interval = 60;

	private int initialConnectionCount = 10;

	private Vector<Connection> availableConns = new Vector<Connection>();

	private Vector<Connection> usedConn = new Vector<Connection>();

	private String urlString = null;

	private String userName = null;

	private String password = null;

	private Thread cleanupThread = null;
    /**
     * This is the constructor for initializing  the Pool.
     * 
     * @param url              - Connection string Eg: jdbc:mysql://host:port/dbname
     * @param user             - User  name of the database
     * @param passwd           - Password of the user
     * @param driver           - Driver class name Eg: com.mysql.jdbc.Driver
     * @param poolSize         - This is the pool size
     * @param cleanupInterval  - This is the interval for cleanup in seconds
     * @throws SQLException
     */
	public ConnectionPool(String url, String user, String passwd, String driver, int poolSize, int cleanupInterval)
			throws SQLException {
		if (poolSize > 0  ) {
			initialConnectionCount = poolSize;
		}
		if (cleanupInterval > 0 ) {
			interval = cleanupInterval;
		}
		if (!driverLoaded) {
			loadDriver(driver);
		}

		urlString = url;
		userName = user;
		password = passwd;
		System.out.println("Creating connections");
		for (int cnt = 0; cnt < initialConnectionCount; cnt++) {
			availableConns.addElement(getConnection());
		}
		cleanupThread = new Thread(this);
		cleanupThread.start();
	}

	/**
	 * This is the constructor for initializing  the Pool.
	 * 
	 * The default pool size is 10
	 * The default cleanupInterval is 60 seconds.
	 * 
	 * @param url              - Connection string Eg: jdbc:mysql://host:port/dbname
     * @param user             - User  name of the database
     * @param passwd           - Password of the user
     * @param driver           - Driver class name Eg: com.mysql.jdbc.Driver
	 * @throws SQLException
	 */
	public ConnectionPool(String url, String user, String passwd, String driver) 
			throws SQLException {
		if (!driverLoaded) {
			loadDriver(driver);
		}
		urlString = url;
		userName = user;
		password = passwd;
		System.out.println("Creating connections");
		for (int cnt = 0; cnt < initialConnectionCount; cnt++) {
			availableConns.addElement(getConnection());
		}
		cleanupThread = new Thread(this);
		cleanupThread.start();
	}
	private void loadDriver(String driver) {
		try {
			System.out.println("Loading Driver");
			Class.forName(driver).newInstance();
			driverLoaded = true;
		} catch (Exception e) {
			System.err.println("Exception while loading driver");
			e.printStackTrace();
		}

	}

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(urlString, userName, password);
	}

	/**
	 * This function retrieve a connection from the pool
	 * 
	 * @author arun
	 * @date 18-Sep-2014
	 * @return Connection
	 * @throws SQLException *
	 */
	public synchronized Connection checkout() throws SQLException {
		Connection newConnxn = null;

		if (availableConns.size() == 0) {
			newConnxn = getConnection();
			usedConn.addElement(newConnxn);
		} else {
			newConnxn = (Connection) availableConns.lastElement();
			availableConns.removeElement(newConnxn);
			usedConn.addElement(newConnxn);
		}
		return newConnxn;
	}

	/**
	 * This function leaves the connection to the pool
	 * 
	 * @author arun
	 * @date 18-Sep-2014
	 * @param c *
	 */
	public synchronized void checkin(Connection c) {
		if (c != null) {
			usedConn.removeElement(c);
			availableConns.addElement(c);
		}
	}

	public int availableCount() {
		return availableConns.size();
	}

	public void run() {
		try {
			while (true) {
				synchronized (this) {
					while (availableConns.size() > initialConnectionCount) {
						Connection c = (Connection) availableConns
								.lastElement();
						availableConns.removeElement(c);
						c.close();
					}
				}
				System.out.println("CLEANUP : Available Connections : "
						+ availableCount());
				Thread.sleep(1000 * interval);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

