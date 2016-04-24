package com.valevich.sunshine.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.valevich.sunshine.data.WeatherContract.*;

/**
 * Created by NotePad.by on 24.04.2016.
 */
public class WeatherDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "weather.db";

    private static final int DATABASE_VERSION = 1;

    public WeatherDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String CREATE_LOCATION_TABLE =
                "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                        LocationEntry._ID  + " INTEGER PRIMARY KEY," +
                        LocationEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, " +
                        LocationEntry.COLUMN_LOCATION_SETTING + " TEXT UNIQUE NOT NULL, " + //we don't want 2 locations with the same postal code.
                        LocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL, " +
                        LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                        " UNIQUE (" + LocationEntry.COLUMN_LOCATION_SETTING + //combination of lat/long also MUST BE UNIQUE--------------------->
                        ") ON CONFLICT IGNORE);";

        final String CREATE_WEATHER_TABLE =
                "CREATE TABLE " + WeatherEntry.TABLE_NAME + " (" +
                        WeatherEntry._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT," + //natural way of sorting as we get data from the server. MAYBE sorting will solve the problem if not use autoincrement.
                        WeatherEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, " +
                        WeatherEntry.COLUMN_DATETEXT + " TEXT NOT NULL, " +
                        WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL, " +
                        WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, " +
                        WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL, " +
                        WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL, " +
                        WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL, " +
                        WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, " +
                        WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, " +
                        WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, " +

                        " FOREIGN KEY(" + WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES " +
                        LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "), " +
                        " UNIQUE (" + WeatherEntry.COLUMN_DATETEXT + ", " +
                        WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";

        //we don't want to have 2 rows with for example Minsk Tuesday. So update values if weather changes.
        //only the combination is unique, not date or loc key, because for example we can have the same dates but corresponding to different locations
        // or the same location loc_keys corresponding to different weather


        //--------------------FOREIGN_KEY------------------------------------------------------
        //The applications using this database are entitled to assume that for each row in the weather table there exists a corresponding row in the location table.
        // Unfortunately, if a user edits the database using an external tool or if there is a bug in an application,
        // rows might be inserted into the weather table that do not correspond to any row in the location table.
        // Or rows might be deleted from the location table, leaving orphaned rows in the weather table that do not correspond to any of the remaining rows in locations.
        // This might cause the application or applications to malfunction later on, or at least make coding the application more difficult.

        db.execSQL(CREATE_LOCATION_TABLE);
        db.execSQL(CREATE_WEATHER_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + WeatherEntry.TABLE_NAME);
        onCreate(db);
    }

}
