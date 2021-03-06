package com.rosteringester.delegatedetect;

import com.rosteringester.db.DbSqlServer;
import com.rosteringester.db.dbModels.DBRosterMDCRRequired;
import com.rosteringester.discovery.DiscoverMedicare;
import com.rosteringester.encryption.MD5Hasher;
import com.rosteringester.filecategorization.FileMover;
import com.rosteringester.fileread.ReadEntireTextFiles;
import com.rosteringester.main.RosterIngester;
import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
* Created by jeshernandez on 09/02/2017.
*/
public class DetectDelegate {
Logger LOGGER = Logger.getLogger(DetectDelegate.class.getName());
DiscoverMedicare medicare;
private String directoryPath;
private boolean localDebug = false;
private Connection conn;
private DbSqlServer db;
private String fileName;
private int productID;
private int id;
private int delegateIDTIN;
private int delegateIDPOIN;
private int delegateFinal;
private int preDelegateID;
private String delegateErrorMsg;
private int scanRecords = 8000;

String updateQuery = null;

public DetectDelegate() {
Map<String, String> config = setConfig("env.yaml");
this.directoryPath = config.get("queryDirectory");
}

// ----------------------------------------------
public Map<String, String> setConfig(String configFile) {
Yaml yaml = new Yaml();
return (Map<String, String>) yaml.load(getClass().getClassLoader().getResourceAsStream(configFile));
}



// ----------------------------------------------
public void getRosterForDetection(String queryFile) {

db =  new DbSqlServer();
db.setConnectionUrl();
conn = db.getDBConn();

String[] tinList;

String query = new ReadEntireTextFiles()
    .getTextData(this.directoryPath + "\\" + queryFile);
db.query(conn, query);

fileName = db.getValueAt(0,0).toString();
productID = Integer.parseInt(db.getValueAt(0,1).toString());
id = Integer.parseInt(db.getValueAt(0,2).toString());

preDelegateID = Integer.parseInt(db.getValueAt(0,4).toString());

if(localDebug) System.out.println("File Name: " + db.getValueAt(0,0).toString());

medicare = new DiscoverMedicare();
medicare.findField(fileName);

int listSize = medicare.getRowCount();

tinList = new String[listSize];

if(localDebug) System.out.println("Tin List Size: " + tinList.length);

for (int i = 1; i < listSize; i++) {
tinList[i] = medicare.normalRoster[1][i];
}

if(preDelegateID > 1) {
    delegateFinal = preDelegateID;
} else {
    // Default final value
    delegateFinal = -1;
    // Detect delegate ID Based on TIN
    delegateIDTIN = -1;
    delegateIDTIN = getDelegateIDTIN(tinList);

    // Detect delegate ID Based on POIN TIN list.
    delegateIDPOIN = -1;
    delegateIDPOIN = getDelegateIDPOIN(tinList);
}


System.out.println("Delegate TIN: " + delegateIDTIN);
System.out.println("Delegate POIN: " + delegateIDPOIN);



if(delegateIDPOIN == -2 || delegateIDTIN == -2) {
 delegateFinal = -2;
 LOGGER.info("More than one delegate found.");
 delegateFinal = delegateIDPOIN;
}
else {
 if(delegateIDTIN != -1 && delegateIDPOIN != -1) {
     LOGGER.info("Delegate found on both TIN and POIN lists.");
     delegateErrorMsg = "DELEGATE OVERLAP: (" + delegateIDTIN + "),(" +delegateIDPOIN+")" ;
 } else {
     if (delegateIDTIN == -1 && delegateIDPOIN == -1) {
         LOGGER.info("No delegate information was found.");
         delegateErrorMsg = "NO DELEGATE FOUND";
     } else if (delegateIDTIN != -1) {
         LOGGER.info("Delegate found on TIN list.");
         delegateFinal = delegateIDTIN;
     } else if (delegateIDPOIN != -1) {
         LOGGER.info("Delegate found on POIN list.");
         delegateFinal = delegateIDPOIN;
     }
 }
}



// Ingest the roster if ingest flag is true and delegate ID was captured
// for either POIN or TIN list.
if(RosterIngester.ingestData && delegateFinal != -1) {

LOGGER.info("Delegate [" + delegateFinal + "] Ingest roster into database...");
ingestRoster(delegateFinal);

} else {
LOGGER.info("Ingest turned off, or issue detecting delegate ID.");
}




// ------------------------
// Assigned delegate
// --------------------------


if (!RosterIngester.accentureSupport) {
    // Assigned found delegate
    if (!RosterIngester.networkSupport) {

        if(delegateFinal != -2) {
                if (delegateFinal != -1) {
                    LOGGER.info("Logging delegate in database...");

                    updateQuery = "update logs.dbo.grips_log_received\n" +
                            " set delegate_id =" + delegateFinal +
                            " , valid = 'Y'" +
                            " , status = 'INGESTED' " +
                            " , standardized = 'Y'" +
                            " , date_last_modified = current_timestamp " +
                            " where id = " + id;
                    if (localDebug) System.out.println("Update: \n" + updateQuery);
                    FileMover move = new FileMover();
                    move.moveFile(RosterIngester.ROSTERS + fileName, RosterIngester.COMPLETED_ROSTER
                            + fileName);
                } else {
                    if (localDebug) LOGGER.info("Logging delegate error.");

                    updateQuery = "update logs.dbo.grips_log_received\n" +
                            " set status = 'PDIU SUPPORT: " + delegateErrorMsg + "'" +
                            " , valid = 'N'" +
                            " , standardized = 'Y'" +
                            " , delegate_id = -1 " +
                            " , date_last_modified = current_timestamp " +
                            " where id = " + id;
                    if (localDebug) System.out.println("Update: \n" + updateQuery);
                    FileMover move = new FileMover();
                    move.moveFile(RosterIngester.ROSTERS + fileName, RosterIngester.NETWORK_FOLDER
                            + fileName);
                }
            } else {
                updateQuery = "update logs.dbo.grips_log_received\n" +
                        " set delegate_id =" + delegateFinal +
                        " , valid = 'N'" +
                        " , status = 'PDIU SUPPORT: MULTIPLE DELEGATES FOUND.'" +
                        " , standardized = 'N'" +
                        " , date_last_modified = current_timestamp " +
                        " where id = " + id;
                if (localDebug) System.out.println("Update: \n" + updateQuery);
                FileMover move = new FileMover();
                move.moveFile(RosterIngester.ROSTERS + fileName, RosterIngester.NETWORK_FOLDER
                        + fileName);
            } // end multiple delegates if-statement

    } else {
        LOGGER.info("Logging network manual support...");

        updateQuery = "update logs.dbo.grips_log_received\n" +
                " set delegate_id = -1"+
                " , valid = 'N'" +
                " , status = 'PDIU SUPPORT: " + RosterIngester.networkErrorMsg + "'" +
                " , standardized = 'N'" +
                " , date_last_modified = current_timestamp " +
                " where id = " + id;
        if (localDebug) System.out.println("Update: \n" + updateQuery);
        FileMover move = new FileMover();
        move.moveFile(RosterIngester.ROSTERS + fileName, RosterIngester.NETWORK_FOLDER
                + fileName);
    }

} else {
    LOGGER.info("Logging accenture support...");

    updateQuery = "update logs.dbo.grips_log_received\n" +
            " set delegate_id =" + delegateFinal +
            " , valid = 'N'" +
            " , status = 'ACCENTURE SUPPORT: " + RosterIngester.accentureErrorMsg + "'" +
            " , standardized = 'N'" +
            " , date_last_modified = current_timestamp " +
            " where id = " + id;
    if (localDebug) System.out.println("Update: \n" + updateQuery);

    // Make a copy before sending to Accenture.
    // ------------------------------------------
    String source = RosterIngester.ROSTERS + fileName;
    String target = RosterIngester.BACKUP_FOLDER + fileName;
    String accentureFolder = RosterIngester.ACCENTURE_FOLDER + fileName;
    FileMover move = new FileMover();

    File fSource = new File(source.toString());
    File fTarget = new File(target.toString());
    File fAccenture = new File(accentureFolder.toString());

    try {
        //FileUtils.copyFile(fSource, fTarget);
        FileUtils.copyFile(fSource, fAccenture);
    } catch (IOException e) {
        e.printStackTrace();
    }

    move.moveFile(RosterIngester.ROSTERS + fileName, target);

} // end accenture if-statement




// ------------------------
// Update assigned delegate
// --------------------------
db.update(conn, updateQuery);



// Close the connection if its open.
try {
if(!conn.isClosed()) {
    LOGGER.info("Detect Delegate Engine connection closing...");
    conn.close();
}


}catch (SQLException e) {
e.printStackTrace();
}
} // end of getRosterForDetection method



// ---------------------------------
public int getDelegateIDTIN(String[] tinList) {
int delegateID = -1;

for (int i = 1; i < tinList.length-1; i++) {

if(localDebug) System.out.println("TIN [" + i + "]: " + tinList[i]);

}

StringBuilder inTinList = new StringBuilder();

Random rand = new Random();
int totalCount = tinList.length;
int sampleSize = 0;
int minRandom = 1;
int maxRandom = totalCount-2;

if(totalCount > scanRecords) {
sampleSize = scanRecords;
} else {
sampleSize = totalCount;
}

// ---------------------------------------------
//     SKIP PROCESSING WHEN VENDOR SUPPORT
// ---------------------------------------------

if(!RosterIngester.accentureSupport) {
for (int i = 1; i < sampleSize; i++) {
    int random = (int) (Math.random() * maxRandom + minRandom);

    // Detect the last number to avoid adding comma.
    //System.out.println("Random: " + random);
    if (i == sampleSize - 1) {
        inTinList.append(tinList[random].toString());
    } else {
        inTinList.append(tinList[random].toString() + ",");
    }

} // End for-loop


// ------------------------
// get appropriate query for product
// --------------------------
String query = null;
if (productID == 1) {
    query = "SELECT DISTINCT delegate_id \n" +
            " FROM grips.dbo.grips_tin\n" +
            " WHERE tin in (" + inTinList.toString() + ")\n";
} else if (productID == 0) {
    query = "SELECT DISTINCT delegate_id \n" +
            " FROM grips.dbo.grips_cpd_tin\n" +
            " WHERE tin in (" + inTinList.toString() + ")\n";
} else if (productID == 2) {
    query = "SELECT DISTINCT delegate_id \n" +
            " FROM grips.dbo.grips_tin\n" +
            " WHERE tin in (" + inTinList.toString() + ")\n";
}

if (localDebug) System.out.println("Query\n: " + query);

db.query(conn, query);

String backupQuery = null;
if(productID == 2 && db.getValueAt(0,0) == null) {
    LOGGER.info("DETECTED BOTH PRODUCTS, EMPTY on EPDB. Delegate detection CPD side.");
    backupQuery = "SELECT DISTINCT delegate_id \n" +
            " FROM grips.dbo.grips_cpd_tin\n" +
            " WHERE tin in (" + inTinList.toString() + ")\n";
    db.query(conn, backupQuery);
}


//

// -----------------------------------------------
// Detect if more than one delegate was found.
// -----------------------------------------------
if(db.getRowCount() == 1) {
    if (db.getValueAt(0, 0) != null) {
        delegateID = Integer.parseInt(db.getValueAt(0, 0).toString());
    }
} else if(db.getRowCount() > 1) {
    LOGGER.info("DELEGATE ERROR [TIN]: More than one delegate was found.");
    delegateID = -2;
}


if (localDebug) System.out.println("Delegate Found: " + db.getValueAt(0, 0));
} // end if-statement

return delegateID;
}





// ---------------------------------
public int getDelegateIDPOIN(String[] tinList) {
int delegateID = -1;

for (int i = 1; i < tinList.length-1; i++) {

if(localDebug) System.out.println("POIN TIN [" + i + "]: " + tinList[i]);

}

StringBuilder inTinList = new StringBuilder();

Random rand = new Random();
int totalCount = tinList.length;
int sampleSize = 0;
int minRandom = 1;
int maxRandom = totalCount-2;

if(totalCount > scanRecords) {
sampleSize = scanRecords;
} else {
sampleSize = totalCount;
}

// ---------------------------------------------
//     SKIP PROCESSING WHEN VENDOR SUPPORT
// ---------------------------------------------

if(!RosterIngester.accentureSupport) {
for (int i = 1; i < sampleSize; i++) {
    int random = (int) (Math.random() * maxRandom + minRandom);

    // Detect the last number to avoid adding comma.
    //System.out.println("Random: " + random);
    if (i == sampleSize - 1) {
        inTinList.append(tinList[random].toString());
    } else {
        inTinList.append(tinList[random].toString() + ",");
    }

} // End for-loop


// ------------------------
// get appropriate query for product
// --------------------------
String query = null;
if (productID == 1) {
    query = "SELECT DISTINCT delegate_id \n" +
            " FROM grips.dbo.grips_grpaffil\n" +
            " WHERE tin in (" + inTinList.toString() + ")\n";
} else if (productID == 0) {
    query = "SELECT DISTINCT delegate_id \n" +
            " FROM grips.dbo.grips_grpaffil\n" +
            " WHERE tin in (" + inTinList.toString() + ")\n";
} else if (productID == 2) {
    query = "SELECT DISTINCT delegate_id \n" +
            " FROM grips.dbo.grips_grpaffil\n" +
            " WHERE tin in (" + inTinList.toString() + ")\n";
}

if (localDebug) System.out.println("Query\n: " + query);


db.query(conn, query);

// -----------------------------------------------
// Detect if more than one delegate was found.
// -----------------------------------------------
if(db.getRowCount() == 1) {
    if (db.getValueAt(0, 0) != null) {
        delegateID = Integer.parseInt(db.getValueAt(0, 0).toString());
    }
} else if(db.getRowCount() > 1) {
    LOGGER.info("DELEGATE ERROR [POIN]: More than one delegate was found.");
    delegateID = -2;
}


if (localDebug) System.out.println("Delegate Found: " + db.getValueAt(0, 0));
} // end if-statement

return delegateID;
}







// ---------------------------------
public void ingestRoster(int delegateID) {
DBRosterMDCRRequired dbRoster;

MD5Hasher md5 = new MD5Hasher();
String rosterFileName;
int recordSize = medicare.normalRoster[0].length-1;
rosterFileName = fileName.toString();
// Generate roster key
String rosterKey = md5.generateRosterKey(rosterFileName, delegateID);


// Insert records into database
for (int i = 1; i < recordSize; i++) {

    System.out.println(getPercentage(i, recordSize));


//    // Check for empty or blank NPI's
//    if(medicare.normalRoster[0][i] == null || medicare.normalRoster[0][i].length() < 1) {
//        medicare.normalRoster[0][i] = "-1";
//    }
//
//    // Check for empty or blank TIN's
//    if(medicare.normalRoster[1][i] == null || medicare.normalRoster[1][i].length() < 1) {
//        medicare.normalRoster[1][i] = "-1";
//    }
//
//    // Check for empty or blank PHONE's
//    if(medicare.normalRoster[14][i] == null || medicare.normalRoster[14][i].length() < 1) {
//        medicare.normalRoster[14][i] = "-1";
//    }

    dbRoster = new DBRosterMDCRRequired.Builder()
            .delegateID(delegateID)
            .rosterName(rosterFileName)
            .rosterKey(rosterKey)
            .rowKey(md5.generateRowKey(medicare.normalRoster[0][i],
                    medicare.normalRoster[1][i],medicare.normalRoster[2][i],
                    medicare.normalRoster[4][i], medicare.normalRoster[6][i],
                    medicare.normalRoster[9][i],medicare.normalRoster[14][i]))
            .npi(Integer.parseInt(medicare.normalRoster[0][i]))
            .tin(Integer.parseInt(medicare.normalRoster[1][i]))
            .firstName(medicare.normalRoster[2][i])
            .middleName(medicare.normalRoster[3][i])
            .lastName(medicare.normalRoster[4][i])
            .role(medicare.normalRoster[5][i])
            .specialty(medicare.normalRoster[6][i])
            .degree(medicare.normalRoster[7][i])
            .groupName(medicare.normalRoster[8][i])
            .address(medicare.normalRoster[9][i])
            .suite(medicare.normalRoster[10][i])
            .city(medicare.normalRoster[11][i])
            .state(medicare.normalRoster[12][i].toUpperCase())
            .zipCode(Integer.parseInt(medicare.normalRoster[13][i]))
            .servicePhone(Long.parseLong(medicare.normalRoster[14][i]))
            .officeHours(medicare.normalRoster[15][i])
            .directoryPrint(medicare.normalRoster[16][i])
            .acceptingNew(medicare.normalRoster[17][i])
            .product(productID)
            .build()
            .create(RosterIngester.logConn);
}
}



private String getPercentage(int start, int end) {

String progressBar = null;

DecimalFormat df = new DecimalFormat("#.##");
double progress;

progress = start*100 / end;
String fp = df.format(progress);

if(progress > 0 && progress < 10) {
progressBar = "Progress(" + fp + "%) [=                   ] of " + end +" rows.";
} else if(progress >= 10 && progress < 20) {
progressBar = "Progress(" + fp + "%) [==                  ] of " + end +" rows.";
} else if(progress >=20 && progress < 30) {
progressBar = "Progress(" + fp + "%) [====                ] of " + end +" rows.";
} else if(progress >=30 && progress < 40) {
progressBar = "Progress(" + fp + "%) [=======             ] of " + end +" rows.";
} else if(progress >=40 && progress < 50) {
progressBar = "Progress(" + fp + "%) [==========          ] of " + end +" rows.";
} else if(progress >=50 && progress < 60) {
progressBar = "Progress(" + fp + "%) [===========         ] of " + end +" rows.";
} else if(progress >=60 && progress < 70) {
progressBar = "Progress(" + fp + "%) [============        ] of " + end +" rows.";
} else if(progress >=70 && progress < 80) {
progressBar = "Progress(" + fp + "%) [===============     ] of " + end +" rows.";
} else if(progress >=80 && progress < 90) {
progressBar = "Progress(" + fp + "%) [==================  ] of " + end +" rows.";
} else if(progress >=90 && progress <= 100) {
progressBar = "Progress(" + fp + "%) [====================] of " + end +" rows.";
} else {
progressBar = "Progress(" + fp + "%) [                    ] of " + end +" rows.";
}

return progressBar;
}



} // End of DetectDelegate
