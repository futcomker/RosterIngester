package com.rosteringester.db;

/**
 * Created by jeshernandez on 6/19/17.
 */
public class DbConnection extends DbFactory {

    // Creating Singleton class

    static DbConnection conn = new DbConnection();

    public static DbConnection getInstance(String dbName) {
        DbFactory df = new DbFactory();
        df.getDBConn(dbName);
        return conn;
    }


}
