package com.rosteringester.db;

import com.sun.org.apache.xpath.internal.operations.Bool;

/**
 * Created by MichaelChrisco on 7/5/17.
 * Model class that acts in the same way as a MVC model.
 */
public class DBRoster {

    public Integer id;
    public String office_phone;
    public String primary_address;
    public String suite;
    public String city;
    public Integer zip_code;
    public String speciality;
    public Boolean accepting_new_patients;
    public Boolean print_in_directory;

    public Boolean isSavedFlag;

    public DBRoster(Object... initArray) {
        this.isSavedFlag = Boolean.FALSE;
        if(initArray.length > 0) {
            this.set(initArray);
        }
    }

    public void set(Object... initArray){
       this.office_phone = (String)initArray[0];
       this.primary_address = (String)initArray[1];
       this.suite = (String)initArray[2];
       this.city = (String)initArray[3];
       this.zip_code = (Integer)initArray[4];
       this.speciality = (String)initArray[5];
       this.accepting_new_patients = (Boolean) initArray[6];
       this.print_in_directory = (Boolean)initArray[7];
    }

    public DBRoster save(){
        this.isSavedFlag = Boolean.TRUE;
        //TODO: DB operation to save DB Roster
        DbSqlServer sqlServer = new DbSqlServer();
        //String query = " insert into rosters (office_phone,
        //                                      primary_address,
        //                                      suite,
        //                                      city,
        //                                      zip_code,
        //                                      speciality,
        //                                      accepting_new_patients,
        //                                      print_in_directory)"
        // + " values (?, ?, ?, ?, ?, ?, ?, ?)";
        //PreparedStatement preparedStmt = conn.prepareStatement(query);
//        preparedStmt.setString (1, this.office_phone);
//        preparedStmt.setString (2, this.primary_address);
//        preparedStmt.setString (3, this.suite);
//        preparedStmt.setString (4, this.city);
//        preparedStmt.setString (5, this.zip_code);
//        preparedStmt.setString (6, this.speciality);
//        preparedStmt.setString (7, this.accepting_new_patients);
//        preparedStmt.setString (8, this.print_in_directory);
//        TODO: Select request to get the models ID from the DB.
        return this;
    }


}