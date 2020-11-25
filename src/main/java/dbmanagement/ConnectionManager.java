package dbmanagement;

import dbmanagement.exceptions.ConnectionIsInvalidException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.Connection;
import java.sql.SQLException;
import static dbmanagement.DbConstants.*;

public class ConnectionManager {
    private static ConnectionManager connectionManagerSingleton = null;

    private String primaryUrl = PRIMARY_URL;
    private String secondaryUrl = SECONDARY_URL;
    private String primaryUser = PRIMARY_USER;
    private String primaryPass = PRIMARY_PASS;
    private String secondaryUser = SECONDARY_USER;
    private String secondaryPass = SECONDARY_PASS;

    private ConnectionPool activeConnectionPool;
    private final ConnectionPool primaryConnectionPool;
    private final ConnectionPool secondaryConnectionPool;

    private boolean isFailoverMode = false;

    private static final Logger logger = LogManager.getLogger(ConnectionManager.class);

    public static ConnectionManager getInstance() {
        if (connectionManagerSingleton == null) {
            synchronized (ConnectionManager.class) {
                connectionManagerSingleton = new ConnectionManager();
            }
        }
        return connectionManagerSingleton;
    }

    private ConnectionManager() {
        primaryConnectionPool = new ConnectionPool(primaryUrl, primaryUser, primaryPass);
        secondaryConnectionPool = new ConnectionPool(secondaryUrl, secondaryUser, secondaryPass);
        activeConnectionPool = primaryConnectionPool;
    }

    public Connection getConnection() {

        if (isFailoverMode) {
            checkPrimaryPoolIsValid();
        }

        try {
            return activeConnectionPool.getConnection();
        }
        catch (ConnectionIsInvalidException e) {
            isFailoverMode = true;
            logger.warn("Primary connection pool not available, failover mode activated");
            activeConnectionPool = secondaryConnectionPool;
            return secondaryConnectionPool.getConnection();
        }
    }

    public void returnConnectionToConnectionPool(Connection connection) throws SQLException {

        if (connection.getMetaData().getURL().equals(primaryUrl)) {
            primaryConnectionPool.returnConnection(connection);
            logger.error("Connection successfully returned to primary connection pool");
        }

        if (connection.getMetaData().getURL().equals(secondaryUrl)) {
            secondaryConnectionPool.returnConnection(connection);
            logger.info("Connection successfully returned to primary connection pool");
        }
    }

    private void checkPrimaryPoolIsValid() {
        if (primaryConnectionPool.isValid()) {
            isFailoverMode = false;
            logger.info("Primary connection pool is available, failover mode deactivated");
            activeConnectionPool = primaryConnectionPool;
        }
    }

}
