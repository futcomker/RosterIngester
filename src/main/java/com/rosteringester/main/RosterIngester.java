package com.rosteringester.main;


import com.rosteringester.discovery.DiscoverFields;


/**
 * Created by jeshernandez on 6/14/17.
 */
public class RosterIngester {
    public static boolean debug = false;
    // TODO me - 07/04/2017 remove word from key and add to requiredFields
    // TODO me - 07/04/2017 find a way to remove highest score for iterator

   //TODO: Michael - Add into its own Service Object. Remove from main method.


    public static void main(String [] args) {

        DiscoverFields df = new DiscoverFields();
        df.findField();




    } // End of Main





} // End of RosterIngester