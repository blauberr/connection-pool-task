package dbmanagement;

import dbmanagement.exceptions.ConnectionIsInvalidException;
import dbmanagement.exceptions.ConnectionsPrepareUnsuccessfulException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

import static dbmanagement.DbConstants.*;

public class ConnectionManager {

    private static ConnectionManager connectionManagerSingleton = null;
    private static final Logger log = LogManager.getLogger(ConnectionManager.class);

    private String primaryUrl = PRIMARY_URL;
    private String primaryUser = PRIMARY_USER;
    private String primaryPass = PRIMARY_PASS;
    private String secondaryUrl = SECONDARY_URL;
    private String secondaryUser = SECONDARY_USER;
    private String secondaryPass = SECONDARY_PASS;

    private ConnectionPool activeConnectionPool;
    private ConnectionPool primaryConnectionPool;
    private ConnectionPool secondaryConnectionPool;

    private boolean isFailoverMode = false;

    public synchronized static ConnectionManager getInstance() {
        if (connectionManagerSingleton == null) {
            connectionManagerSingleton = new ConnectionManager();
        }
        return connectionManagerSingleton;
    }

    public synchronized void setPrimaryPool(ConnectionPool primaryPool) {
        this.primaryConnectionPool = primaryPool;
        if (!isFailoverMode) {
            activeConnectionPool = primaryConnectionPool;
        }
    }

    public synchronized void setSecondaryPool(ConnectionPool secondaryPool) {
        this.secondaryConnectionPool = secondaryPool;
        if (isFailoverMode) {
            activeConnectionPool = secondaryConnectionPool;
        }
    }

    private ConnectionManager() {
        primaryConnectionPool = new ConnectionPool(primaryUrl, primaryUser, primaryPass);
        secondaryConnectionPool = new ConnectionPool(secondaryUrl, secondaryUser, secondaryPass);
        activeConnectionPool = primaryConnectionPool;
    }

    public Connection getConnection() {

        if (isFailoverMode) {
            checkPrimaryPool();
        }

        Connection connection = null;
        try {
            connection = activeConnectionPool.getConnection();
        } catch (ConnectionIsInvalidException e) {
            log.warn("Active connection pool is invalid");
        }

        if (connection != null) {
            return connection;
        } else {
            isFailoverMode = true;
            activeConnectionPool = secondaryConnectionPool;
            log.warn("Failover mode is active, switching to secondary connection pool");
            try {
                connection = activeConnectionPool.getConnection();
            } catch (ConnectionIsInvalidException e) {
                log.error("Both databases are down");
                throw e;
            }
            return connection;
        }
    }

    public void returnConnectionToConnectionPool(Connection connection) throws SQLException {
        try {
            if (connection.getMetaData().getURL().equals(primaryUrl)) {
                primaryConnectionPool.returnConnection(connection);
                log.info("Connection successfully returned to primary connection pool");
            } else if (connection.getMetaData().getURL().equals(secondaryUrl)) {
                secondaryConnectionPool.returnConnection(connection);
                log.info("Connection successfully returned to primary connection pool");
            } else {
                throw new IllegalStateException("Unknown URL");
            }
        } catch (SQLException e) {
            log.error("Failed to return connection", e);
            throw e;
        }
    }

    private void checkPrimaryPool() {
        try {
            primaryConnectionPool.prepareConnections();
            isFailoverMode = false;
            activeConnectionPool = primaryConnectionPool;

            log.info("Primary pool is active again, failover mode deactivated");
        } catch (ConnectionsPrepareUnsuccessfulException e) {
            log.warn("Primary pool is still down");
        }
    }
}
