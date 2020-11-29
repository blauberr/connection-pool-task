package dbmanagement;

import dbmanagement.exceptions.ConnectionIsInvalidException;
import dbmanagement.exceptions.ConnectionsPrepareUnsuccessfulException;
import dbmanagement.exceptions.OutOfConnectionsException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ConnectionPool {

    private static final Logger log = LogManager.getLogger(ConnectionPool.class);

    private static final int DEFAULT_POOL_SIZE = 5;
    private static final int DEFAULT_CONNECTION_VALIDATION_TIMEOUT = 2;

    private final String url;
    private final String user;
    private final String pass;
    private final int poolSize;
    private final List<Connection> connections;
    private boolean allConnectionsWereTaken = false;

    public ConnectionPool(String url, String user, String pass) {
        this(url, user, pass, DEFAULT_POOL_SIZE);
    }

    public ConnectionPool(String url, String user, String pass, int poolSize) {
        this.url = url;
        this.user = user;
        this.pass = pass;
        this.poolSize = poolSize;
        this.connections = new ArrayList<>(poolSize);
    }

    /**
     * @throws OutOfConnectionsException    when all connections are taken
     * @throws ConnectionIsInvalidException when all connections are taken
     */
    public Connection getConnection() throws OutOfConnectionsException {

        if (connections.isEmpty() && !allConnectionsWereTaken) {
            prepareConnections();
        }

        Connection connection = tryToAcquireConnection();
        if (connection == null) {
            try {
                log.info("No available connection in the pool, waiting " + DEFAULT_CONNECTION_VALIDATION_TIMEOUT + " seconds for connection becoming available");
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                log.error("An error occurred while awaiting for connection becoming available");
            }

            connection = tryToAcquireConnection();

            if (connection == null) throw new OutOfConnectionsException();
        }

        boolean connectionIsValid = validateConnection(connection);

        if (connectionIsValid) {
            log.info("Connection is valid and was taken from the pool, " + connections.size() + " connections remains in the pool");

            if (connections.size() == 0) allConnectionsWereTaken = true;

            return connection;

        } else {
            log.error("Connection is not valid");
            connections.clear();
            throw new ConnectionIsInvalidException();
        }
    }

    private boolean validateConnection(Connection connection) {
        try {
            log.info("Validating acquired connection");
            return connection.isValid(DEFAULT_CONNECTION_VALIDATION_TIMEOUT);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * @return Connection or null
     */
    private Connection tryToAcquireConnection() {
        synchronized (connections) {
            return connections.isEmpty() ? null : connections.remove(0);
        }
    }

    public void returnConnection(Connection connection) {
        synchronized (connections) {
            connections.add(connection);
        }
    }

    /**
     * @throws ConnectionsPrepareUnsuccessfulException when was not possible to prepare connections
     */
    void prepareConnections() {
        connections.clear();
        try {
            log.info("Preparing connections for connection pool");
            DriverManager.registerDriver(DriverManager.getDriver(url));

            for (int i = 0; i < poolSize; i++) {
                Connection connection = DriverManager.getConnection(url, user, pass);
                connections.add(connection);
                log.info("Connection number " + (i + 1) + " prepared");
            }

            log.info("Connections for connection pool were successfully prepared");

        } catch (SQLException e) {
            log.error("Was not possible to prepare connection for connection pool");
            throw new ConnectionsPrepareUnsuccessfulException();
        }
    }
}