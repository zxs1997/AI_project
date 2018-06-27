package com.example.dante.ai_demo.DatabasePack;

import android.provider.BaseColumns;

public final class Contract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private Contract() {}

    /* Inner class that defines the table contents (table name and column names)*/
    public static class Entry implements BaseColumns {
        //用了BaseColumn后就会自动生成id
        public static final String TABLE_NAME = "rubbish";
        public static final String COLUMN_NAME_RUBBISH_NAME = "rubbish_name";
        public static final String COLUMN_NAME_RUBBISH_CATALOGUE = "rubbish_catalogue";
    }
}
