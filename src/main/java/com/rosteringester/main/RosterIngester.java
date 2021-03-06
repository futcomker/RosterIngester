package com.rosteringester.main;

import com.rosteringester.db.DbSqlServer;
import com.rosteringester.delegatedetect.DetectDelegate;
import com.rosteringester.emailsystem.SendEmail;
import com.rosteringester.filecategorization.FileMover;
import com.rosteringester.usps.AddressEngine;
import com.rosteringester.usps.AddressInText;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;


/**
 * Created by jeshernandez on 6/14/17.
 */

// ------------------------------------------------------------------------------------------------
// TODO - ISSUE#1: me certain templates hang, example: IL_Adventist_Aetna and Coventry...
// TODO - issue is solved by copying and pasting in text form.
// TODO - ISSUE#2: parallel ingestion of rosters, indicate in MS SQL while a roster is in progress.
// TODO - this will allow additional threads to run and allow multiple rosters to ingest at same time.
// TODO - ISSUE#3: need a way to stop and validate fields captured by distance algorithm are valid. Then
// TODO - give user opportunity to accept, or select new fields. This is for GUI version.
// TODO - ISSUE #3: come up with a solution to automatically track rosters throughout the process
// TODO - difficulty comes in when roster name (title) changes.
// TODO - rosters with no product, fail with simple NullPointerException. Need to throw exception.
// TODO - rosters with no SUITE are very common. We need a workaround to allow these rosters.
// TODO - ISSUE #4 multiple tab detection xls xlsx
// TODO ISSUE #5 reject rosters that do not have a product
// TODO - ISSUE #5 rosters are being ingested with blanks, have to figure out how to convert entire sheet
// TODO - to text base only to eliminate ingesting rows that may detect a space and ingested blank.
    // TODO - "smarty streets" sends 100 batch address, under 100 causes not to send.
// -------------------------------------------------------------------------------------------------

public class RosterIngester {
    public static boolean debug = true;
    private static boolean activateMove = false;
    private static boolean activateDelegateDetection = false;

    private static boolean activeAddressNormalization = true;
    private static String typeOfNormalization = "grips";

    public static boolean accentureSupport = false;
    public static String accentureErrorMsg = "TABS, EXCLUDE RED";
    // STANDARDIZATION ISSUES: ADDRESS
    // STANDARDIZATION ISSUES - TABS
    // FIELDS, PARSE REQUIRED
    // HORIZONTAL ADDRESS
    // SPLIT TIN TO PROVDR ROWS



    public static boolean networkSupport = false;
    public static String networkErrorMsg = "STILL MISSING TIN";
    // ADDRESS MISSING
    // ROLE VALUES CANNOT BE MAPPED
    // ROSTER MISSING PHONE, TIN, OTHER FIELDS
    // MISSING DIR PRINT, MUL DELEGATES
    // MISSING DIRECTORY PRINT"
    // MISSING ACCEPTING NEW PAT
    // BAD ROSTERS, MISSING FIELDS
    // MISSING DIR PRINT, ACCPT PT

    public static boolean ingestData = false;

    static Logger LOGGER = Logger.getLogger(RosterIngester.class.getName());
    public static Connection logConn = null;

//    public static String NORMALIZE_PATH = "C:\\DATA\\rosters\\standardized\\";
    //public static String ARRIVING_ROSTERS = "C:\\DATA\\rosters\\arrived";
   // public static String ROSTERS = "C:\\DATA\\rosters\\";
//    public static String NETWORK_FOLDER = "C:\\DATA\\rosters\\network_review\\";
//    public static String COMPLETED_ROSTER = "C:\\DATA\\rosters\\archive_completed\\";


    public static String NORMALIZE_PATH = "\\\\frsp-oa-001\\DirectoryAccuracyITStrg\\standardized\\";
    public static String ARRIVING_ROSTERS = "\\\\midp-sfs-009\\Prov_addresses_CleanUp\\Round 2\\Rosters";
    public static String ROSTERS = "\\\\frsp-oa-001\\DirectoryAccuracyITStrg\\rosters\\";
    public static String NETWORK_FOLDER = "\\\\frsp-oa-001\\DirectoryAccuracyITStrg\\network_review\\";
    public static String COMPLETED_ROSTER = "\\\\frsp-oa-001\\DirectoryAccuracyITStrg\\archive_completed\\";
    public static String ACCENTURE_FOLDER = "\\\\frsp-oa-001\\DirectoryAccuracyITStrg\\accenture_support\\";
    public static String BACKUP_FOLDER = "\\\\frsp-oa-001\\DirectoryAccuracyITStrg\\rosters\\BACKUP\\";

    public static void main(String [] args) {

        // IDEAL SOLUTION
        // ------------------------------
        // step 1 -> file category
        // step 2 -> file reading *
        // step 3 -> algorithm *
        // step 4 -> database (ingest) ??
        // step 5 -> filewrite (output normalized roster)
        // step 6 -> usps (standardize address)
        // step 7 -> business rules for RPDB compare
        // step 8 -> autoreport (output to network drive).

        // PLAN B
        // -------------
        // step 1 -> Donna to label every file
        // step 2 -> Accenture standardizes rosters
        // step 3 -> Ingest with Alteryx
        // step 4 -> usps (standardize address)
        // step 5 -> business rules for RPDB compare
        // step 6 -> autoreport (output to network drive).


       // Discover the roster

//        new SendEmail().init("GRIPS: URGENT - Medicare / Roster Updates", "Our very first GRIPS notificaiton email.");

        // ----------------------------------
        //    1.   INSTANTIATE CONN
        // ----------------------------------
        DbSqlServer dbSql =  new DbSqlServer();
        dbSql.setConnectionUrl();
        logConn = dbSql.getDBConn();

        // ----------------------------------
        //    2.   MOVE AND LOG FILES (package: filewrite)
        // ----------------------------------
        if(activateMove) new FileMover().detectFilesMoveThem();


        // ----------------------------------
        //    3.   START DELEGATE DETECTION
        // ----------------------------------

        if(activateDelegateDetection) {
            DetectDelegate dd = new DetectDelegate();
            dd.getRosterForDetection("delegateDetection.sql");
        }


        if(activeAddressNormalization) {

            if(typeOfNormalization.toLowerCase().equals("grips")) {
                LOGGER.info("Normalizing grips...");
                AddressEngine ae = new AddressEngine();
                ae.startStandard("gripsQuery.sql",
                        "gripsUpdate.sql");
            } else if(typeOfNormalization.toLowerCase().equals("epdb")) {
                LOGGER.info("Normalizing epdb...");
                AddressEngine ae = new AddressEngine();
                ae.startStandard("epdbAddressQuery.sql",
                        "epdbAddressUpdate.sql");
            } else if(typeOfNormalization.toLowerCase().equals("cpd")) {
                LOGGER.info("Normalizing cpd...");
                AddressEngine ae = new AddressEngine();
                ae.startStandard("cpdAddressQuery.sql",
                        "cpdAddressUpdate.sql");
            } else if(typeOfNormalization.toLowerCase().equals("gripsusps")) {
                LOGGER.info("Normalizing usps cpd...");
                AddressEngine ae = new AddressEngine();
                ae.startUSPS("uspsGRIPSQuery.sql",
                        "uspsGRIPSUpdate.sql");
            } else if(typeOfNormalization.toLowerCase().equals("epdbusps")) {
                LOGGER.info("Normalizing usps epdb...");
                AddressEngine ae = new AddressEngine();
                ae.startUSPS("uspsEPDBQuery.sql",
                        "uspsEPDBUpdate.sql");
            } else if(typeOfNormalization.toLowerCase().equals("cpdusps")) {
                LOGGER.info("Normalizing usps cpd...");
                AddressEngine ae = new AddressEngine();
                ae.startUSPS("uspsCPDQuery.sql",
                        "uspsCPDUpdate.sql");
            } else if(typeOfNormalization.toLowerCase().equals("epdbtext")) {
                LOGGER.info("Normalizing usps text EPDB...");
                AddressEngine ae = new AddressEngine();
                ae.startAddressInText("textEPDBQuery.sql",
                        "textEPDBUpdate.sql");
            } else if(typeOfNormalization.toLowerCase().equals("gripstext")) {
                LOGGER.info("Normalizing usps text GRIPS...");
                AddressEngine ae = new AddressEngine();
                ae.startAddressInText("textGRIPSQuery.sql",
                        "textGRIPSUpdate.sql");
            } else if(typeOfNormalization.toLowerCase().equals("vendorusps")) {
                LOGGER.info("Normalizing usps text VENDOR USPS...");
                AddressEngine ae = new AddressEngine();
                ae.startSmartyWithSuite("vendorUSPS_DATA_Query.sql",
                        "vendorUSPS_DATA_Update.sql");
            } else if(typeOfNormalization.toLowerCase().equals("grips2")) {
                LOGGER.info("Normalizing usps text GRIPS NO SUITE...");
                AddressEngine ae = new AddressEngine();
                ae.startNoSuite("gripsQueryNoSuite.sql",
                        "gripsUpdateNoSuite.sql");
            }





        } // End-if



        try {
            if(logConn != null) {
                if(!logConn.isClosed() ) {
                    LOGGER.info("Connection open, closing...");
                    logConn.close();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }








    } // End of Main

} // End of RosterIngester