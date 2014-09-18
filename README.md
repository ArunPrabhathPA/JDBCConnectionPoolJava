JDBCConnectionPoolJava
======================

The Java code for JDBC Connection Pool.

Create a connection Pool
========================

There are two overloaded constructors for creating the connections pool

ConnectionPool pool = new ConnectionPool("jdbc:mysql://host:port/dbname","username","password", "divername"); 

ConnectionPool pool = new ConnectionPool("jdbc:mysql://host:port/dbname","username","password", "divername", poolSize, cleanupInterval); 
 


Getting a connection from the Pool
================================

Connection connection = pool.checkout();

Leave the connection back to the pool
=====================================

pool.checkin(connection);


We don't need to close the connection, it will automatically close the unused connection and maintain constant number of connections in the pool.
