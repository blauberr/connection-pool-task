package dbmanagement;


import dbmanagement.exceptions.ConnectionIsInvalidException;
import dbmanagement.exceptions.ConnectionsPrepareUnsuccessfulException;
import dbmanagement.exceptions.OutOfConnectionsException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ConnectionPool {

    private static final Logger log = LogManager.getLogger(ConnectionPool.class);

    private static final int DEFAULT_POOL_SIZE = 5;
    private static final int CONNECTION_VALIDATION_TIMEOUT_SECONDS = 3;

    private final String url;
    private final String user;
    private final String pass;
    private final int poolSize;
    private final Set<Connection> connections;

    public ConnectionPool(String url, String user, String pass) {
        this(url, user, pass, DEFAULT_POOL_SIZE);
    }


    //TODO нельзя упростить раз уж оно уже доступно отовслюду?
    public ConnectionPool(String url, String user, String pass, int poolSize) {
        this.url = url;
        this.user = user;
        this.pass = pass;
        this.poolSize = poolSize;
        this.connections = new HashSet<>(poolSize);

        prepareConnections();
    }

    /**
     * @throws OutOfConnectionsException    when all connections are taken
     * @throws ConnectionIsInvalidException when DB is not longer active
     */
    public Connection getConnection() throws OutOfConnectionsException {

        if (connections.isEmpty()) {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                log.error("An error occured while awaiting for connection becoming available");
            }

            log.warn("Connection pool is out of available connections");
            throw new OutOfConnectionsException();
        } else {
            Connection connection = connections.iterator().next();
            connections.remove(connection);

            try {
                connection.isValid(CONNECTION_VALIDATION_TIMEOUT_SECONDS);
            } catch (SQLException e) {
                log.error("Connection is no longer valid");
                throw new ConnectionIsInvalidException();
            }
            return connection;
        }
    }

    /**
     * @throws ConnectionIsInvalidException when DB is not longer active
     */
    public boolean isValid () {
        try {
            return getConnection().isValid(CONNECTION_VALIDATION_TIMEOUT_SECONDS);
        } catch (SQLException ex) {
            throw new ConnectionIsInvalidException();
        }
    }

    public void returnConnection(Connection c) {
        connections.add(c);
        log.trace("Connection was returned to pool");
    }

    /**
     * @throws ConnectionsPrepareUnsuccessfulException when was not possible to prepare connections
     */
    private void prepareConnections() {
        try {
            log.info("Preparing connections for connection pool");
            DriverManager.registerDriver(DriverManager.getDriver(url));

            for (int i = 0; i < poolSize; i++) {
                Connection connection = DriverManager.getConnection(url, user, pass);
                connections.add(connection);
            }

            log.info("Connections for connection pool were successfully prepared");

        } catch (SQLException e) {
            log.error("Was not possible to prepare connection for connection poo;");
            throw new ConnectionsPrepareUnsuccessfulException();
        }
    }
}