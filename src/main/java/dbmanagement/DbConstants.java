package dbmanagement;

public final class DbConstants {
    static final String PRIMARY_URL = "jdbc:oracle:thin:@localhost:1521:XE";
    static final String PRIMARY_USER = "TICKETSYSTEM";
    static final String PRIMARY_PASS = "admin";

    static final String SECONDARY_URL = "jdbc:postgresql://localhost:5432/postgres";
    static final String SECONDARY_USER = "postgres";
    static final String SECONDARY_PASS = "admin";
}