import dbmanagement.ConnectionManager;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.sql.*;


public class Main {

    public static void main(String[] args) {

//        RestClientBuilder restClient = RestClient.builder(new HttpHost("localhost", 9200, "http"),
//                new HttpHost("localhost", 9201, "http"));
//        RestClient client = restClient.build();



//        Scanner in = new Scanner(System.in);
//        System.out.println("Do you wish to make search in log?");
//        String inpput = in.next();
//        if (inpput.equalsIgnoreCase("y")) {
//            System.out.println("Enter search request");
//            String toSearch = in.next();
//            ElasticSearch search = new ElasticSearch(toSearch);
//        } else {
//            System.out.println("OK, bye");
//        }


        ConnectionManager manager = ConnectionManager.getInstance();

        for (int i=0; i <=4; i++) {

            Connection connection = manager.getConnection();

            try {
                manager.returnConnectionToConnectionPool(connection);
            } catch (SQLException e) {
            }
        }


//        dbmanagement.ConnectionPool connectionPool = dbmanagement.ConnectionPool.getInstance();

//
//        try {
//            DriverManager.registerDriver(DriverManager.getDriver("jdbc:oracle:thin:@localhost:1521/XE"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        Connection connection = null;
//        Statement statement = null;
//
//        try {
//            String url = "jdbc:oracle:thin:@localhost:1521/XE";
//
//            Properties properties = new Properties();
//            properties.setProperty("user", "TICKETSYSTEM");
//            properties.setProperty("password", "heslo");
//            properties.setProperty("ssl", "false");

//            connection = DriverManager.getConnection(url, properties);
//
//            statement = connection.createStatement();
////
//            String sql;
//


    }
}
