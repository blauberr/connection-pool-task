import dbmanagement.ConnectionPool;
import dbmanagement.exceptions.ConnectionIsInvalidException;
import dbmanagement.exceptions.OutOfConnectionsException;
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
    public void getConnectionsThrowsExceptionWhenOutOfConnections() {

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
    public void test() {
        ConnectionPool connectionPool = new ConnectionPool("jdbc:postgresql://localhost:5432/postgres", "postgres", "admin");
        ConnectionPool connectionPool2 = new ConnectionPool("jdbc:h2:mem:test2", TEST_USER, TEST_PASSWORD);

        Connection connection = connectionPool.getConnection();
        Connection connection2 = connectionPool2.getConnection();

        assertNotNull(connection);
        assertNotNull(connection2);
    }

    @Test
    public void getConnectionThrowsConnectionIsInvalidException() throws SQLException {
//        dbmanagement.ConnectionPool connectionPool = new dbmanagement.ConnectionPool("jdbc:postgresql://localhost:5432/postgres", "postgres", "admin");
//
//        Connection c = connectionPool.getConnection();
        Connection connection = Mockito.mock(Connection.class);
        ConnectionPool pool = Mockito.mock(ConnectionPool.class);

        Mockito.when(connection.isValid(Mockito.anyInt())).thenReturn(false);

        assertThrows(ConnectionIsInvalidException.class, pool::getConnection);


    }

//    @Test
//    public void getConnectionsThrowsExceptionWhenConnectionIsInvalid() throws SQLException {
//
//        // create pool
//        dbmanagement.ConnectionPool connectionPool = new dbmanagement.ConnectionPool(TEST_URL, TEST_USER, TEST_PASSWORD);
//
//        Connection connectionMock = Mockito.mock(Connection.class);
//        when(connectionMock.isValid(anyInt())).thenThrow(SQLException.class);
//        doThrow(SQLException.class).when(connectionMock).isValid(anyInt());
//
//        // ask for one more connection
//        assertThrows(ConnectionIsInvalidException.class);
//    }
}
