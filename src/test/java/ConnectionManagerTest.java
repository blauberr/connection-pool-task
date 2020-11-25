import dbmanagement.ConnectionManager;
import dbmanagement.ConnectionPool;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManagerTest {


    private static final String TEST_USER = "user";
    private static final String TEST_PASSWORD = "pass";



    @Test
    public void returnConnectionsToThePool () throws SQLException {
        ConnectionPool connectionPool1 = new ConnectionPool("jdbc:h2:mem:test2", TEST_USER, TEST_PASSWORD);
        ConnectionPool connectionPool2 = new ConnectionPool("jdbc:postgresql://localhost:5432/postgres", "postgres", "admin");

        ConnectionManager connectionManager = ConnectionManager.getInstance();

        Connection connection = connectionPool1.getConnection();

        Mockito.when(connection.getMetaData().getURL()).thenReturn("jdbc:postgresql://localhost:5432/postgres");


        connectionManager.returnConnectionToConnectionPool(connection);
    }
}
