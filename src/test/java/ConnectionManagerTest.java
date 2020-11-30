import dbmanagement.ConnectionManager;
import dbmanagement.ConnectionPool;
import dbmanagement.exceptions.ConnectionIsInvalidException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Connection;

public class ConnectionManagerTest {

    @Test
    public void verifySwitchesToSecondaryPool() {
        ConnectionManager connectionManager = ConnectionManager.getInstance();

        ConnectionPool primaryPoolMock = Mockito.mock(ConnectionPool.class);
        Mockito.when(primaryPoolMock.getConnection()).thenThrow(new ConnectionIsInvalidException());

        Connection validConnectionMock = Mockito.mock(Connection.class);

        ConnectionPool secondaryPoolMock = Mockito.mock(ConnectionPool.class);
        Mockito.when(secondaryPoolMock.getConnection()).thenReturn(validConnectionMock);

        connectionManager.setPrimaryPool(primaryPoolMock);
        connectionManager.setSecondaryPool(secondaryPoolMock);

        Connection returnedConnection = connectionManager.getConnection();
        Assert.assertEquals(returnedConnection, validConnectionMock);
    }

    @Test
    public void verifySwitchesBackToPrimaryPool() {
        ConnectionManager connectionManager = ConnectionManager.getInstance();

        ConnectionPool primaryPoolMock = Mockito.mock(ConnectionPool.class);
        Mockito.when(primaryPoolMock.getConnection()).thenThrow(new ConnectionIsInvalidException());

        Connection validPrimaryConnectionMock = Mockito.mock(Connection.class);
        Connection validSecondaryConnectionMock = Mockito.mock(Connection.class);

        ConnectionPool secondaryPoolMock = Mockito.mock(ConnectionPool.class);
        Mockito.when(secondaryPoolMock.getConnection()).thenReturn(validSecondaryConnectionMock);

        connectionManager.setPrimaryPool(primaryPoolMock);
        connectionManager.setSecondaryPool(secondaryPoolMock);

        Connection returnedConnection = connectionManager.getConnection();
        Assert.assertEquals(returnedConnection, validSecondaryConnectionMock);

        Mockito.when(secondaryPoolMock.getConnection()).thenReturn(validPrimaryConnectionMock);
        Connection primCon = connectionManager.getConnection();
        Assert.assertEquals(primCon, validPrimaryConnectionMock);
    }
}
