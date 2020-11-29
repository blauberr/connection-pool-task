import dbmanagement.ConnectionManager;
import dbmanagement.ConnectionPool;
import dbmanagement.exceptions.ConnectionIsInvalidException;
import dbmanagement.exceptions.OutOfConnectionsException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

public class ConnectionPoolTest {

    private static final int POOL_SIZE = 5;
    private static final String TEST_URL = "jdbc:h2:mem:test";
    private static final String TEST_USER = "user";
    private static final String TEST_PASSWORD = "pass";

    @Test
    public void getConnectionsThrowsExceptionWhenOutOfConnections() throws OutOfConnectionsException {

        // create pool
        ConnectionPool connectionPool = new ConnectionPool(TEST_URL, TEST_USER, TEST_PASSWORD);

        // consume all connections
        for (int i = 0; i <= POOL_SIZE; i++) {
            connectionPool.getConnection();
        }

        // ask for one more connection
        assertThrows(OutOfConnectionsException.class, connectionPool::getConnection);
    }

    @Test
    public void testConnection() {
        ConnectionPool connectionPool = new ConnectionPool("jdbc:postgresql://localhost:5432/postgres", "postgres", "admin");
        ConnectionPool connectionPool2 = new ConnectionPool("jdbc:oracle:thin:@localhost:1521:XE", "TICKETSYSTEM", "admin");

        Connection connection = connectionPool.getConnection();
        Connection connection2 = connectionPool2.getConnection();

        assertNotNull(connection);
        assertNotNull(connection2);
    }
}
