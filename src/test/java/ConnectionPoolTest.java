import dbmanagement.ConnectionPool;
import dbmanagement.exceptions.OutOfConnectionsException;
import org.junit.Test;

import java.sql.Connection;

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
        for (int i = 0; i < POOL_SIZE; i++) {
            connectionPool.getConnection();
        }

        // ask for one more connection
        assertThrows(OutOfConnectionsException.class, connectionPool::getConnection);
    }

    @Test
    public void testConnection() {
        ConnectionPool connectionPool = new ConnectionPool(TEST_URL, TEST_USER, TEST_PASSWORD, POOL_SIZE);
        ConnectionPool connectionPool2 = new ConnectionPool(TEST_URL, TEST_USER, TEST_PASSWORD, POOL_SIZE);

        Connection connection = connectionPool.getConnection();
        Connection connection2 = connectionPool2.getConnection();

        assertNotNull(connection);
        assertNotNull(connection2);
    }
}
