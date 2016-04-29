package com.valevich.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import com.valevich.sunshine.data.WeatherContract.*;

/**
 * Created by NotePad.by on 25.04.2016.
 */
public class WeatherProvider extends ContentProvider {

    private static final int WEATHER = 100;
    private static final int WEATHER_WITH_LOCATION = 101;
    private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;

    private static UriMatcher sURIMatcher = buildUriMatcher();

    private WeatherDBHelper mOpenHelper;
    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

    static {
        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
        sWeatherByLocationSettingQueryBuilder.setTables(WeatherEntry.TABLE_NAME
                + " INNER JOIN "
                + LocationEntry.TABLE_NAME
                + " ON " + WeatherEntry.TABLE_NAME
                + "." + WeatherEntry.COLUMN_LOC_KEY
                + " = " + LocationEntry.TABLE_NAME
                + "." + LocationEntry._ID
        );
    }

    private SQLiteDatabase openDb() {
        return mOpenHelper.getWritableDatabase();
    }

//    The SQLite Joins clause is used to combine records from two or more tables in a database.
//    A JOIN is a means for combining fields from two tables by using values common to each.

    private static final String sLocationSettingSelection =
            LocationEntry.TABLE_NAME + "." + LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";

    private static final String sLocationSettingWithStartDateSelection =
            LocationEntry.TABLE_NAME
                    + "." + LocationEntry.COLUMN_LOCATION_SETTING
                    + " = ? AND "
                    + WeatherEntry.COLUMN_DATETEXT + " >= ? ";

    private static final String sLocationSettingWithDateSelection =
            LocationEntry.TABLE_NAME
                    + "." + LocationEntry.COLUMN_LOCATION_SETTING
                    + " = ? AND "
                    + WeatherEntry.COLUMN_DATETEXT + " = ? ";

    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherEntry.getLocationSettingFromUri(uri);
        String startDate = WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if(startDate == null) {
            selection = sLocationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selectionArgs = new String[]{locationSetting,startDate};
            selection = sLocationSettingWithStartDateSelection;
        }

        return sWeatherByLocationSettingQueryBuilder.query(
                openDb(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

    }

    private Cursor getWeatherByLocationSettingAndDate(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherEntry.getLocationSettingFromUri(uri);
        String date = WeatherEntry.getDateFromUri(uri);

        String[] selectionArgs = new String[]{locationSetting,date};
        String selection = sLocationSettingWithDateSelection;

        return sWeatherByLocationSettingQueryBuilder.query(
                openDb(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }


    private Cursor getWeatherCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder) {//CLOSE DATABASE
        Cursor retCursor;
        retCursor = openDb().query(
                WeatherEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        return retCursor;
    }

    private Cursor getLocationCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder) {//CLOSE DATABASE
        Cursor retCursor;
        retCursor = openDb().query(
                LocationEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
        return retCursor;
    }


    private static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WeatherContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority,WeatherContract.PATH_WEATHER,WEATHER);
        uriMatcher.addURI(authority,WeatherContract.PATH_WEATHER + "/*",WEATHER_WITH_LOCATION);
        uriMatcher.addURI(authority,WeatherContract.PATH_WEATHER + "/*/*",WEATHER_WITH_LOCATION_AND_DATE);

        uriMatcher.addURI(authority,WeatherContract.PATH_LOCATION,LOCATION);
        uriMatcher.addURI(authority,WeatherContract.PATH_LOCATION + "/#",LOCATION_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new WeatherDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;

        switch (sURIMatcher.match(uri)) {
            case WEATHER_WITH_LOCATION_AND_DATE :
                retCursor = getWeatherByLocationSettingAndDate(uri,projection,sortOrder);
                break;
            case WEATHER_WITH_LOCATION :
                retCursor = getWeatherByLocationSetting(uri,projection,sortOrder);
                break;
            case WEATHER :
                retCursor = getWeatherCursor(projection, selection, selectionArgs, sortOrder);
                break;
            case LOCATION :
                retCursor = getLocationCursor(projection, selection, selectionArgs, sortOrder);
                break;
            case LOCATION_ID :
                retCursor = getLocationCursor(projection,
                        LocationEntry._ID + " = " + ContentUris.parseId(uri),
                        selectionArgs,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException(" Unknown uri " + uri);
        }

        if(getContext() != null)
            retCursor.setNotificationUri(getContext().getContentResolver(),uri); //register content observer for changes that can happen to that uri
                                                                                //or any of it's descendants

        return retCursor;
    }


    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sURIMatcher.match(uri);
        switch (match) {
            case WEATHER_WITH_LOCATION_AND_DATE :
                return WeatherEntry.CONTENT_ITEM_TYPE;
            case WEATHER_WITH_LOCATION :
                return WeatherEntry.CONTENT_TYPE;
            case WEATHER :
                return WeatherEntry.CONTENT_TYPE;
            case LOCATION :
                return LocationEntry.CONTENT_TYPE;
            case LOCATION_ID :
                return LocationEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException(" Unknown uri " + uri);
        }
    }

//    When we insert in the database we want to notify every content observer that might have data modified by our insert.
//    Notifying the root uri we also notify it's descendants
    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri returnUri;
        long _id;
        switch (sURIMatcher.match(uri)) {
            case WEATHER:
                _id = openDb().insert(WeatherEntry.TABLE_NAME,null,values);
                if(_id > 0) returnUri = WeatherEntry.buildWeatherUri(_id);
                else throw new SQLException("Error inserting into" + uri);
                break;
            case LOCATION :
                _id = openDb().insert(LocationEntry.TABLE_NAME,null,values);
                if(_id > 0) returnUri = LocationEntry.buildLocationUri(_id);
                else throw new SQLException("Error inserting into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri" + uri);
        }

        if(getContext()!=null) getContext().getContentResolver().notifyChange(uri,null); //this uri changed

        return returnUri;
    }

//    Why we insert only weather and location? if we notify content observer by for example weather with location uri, weather uri observer will not be notified
//    And what is insert weather with location? We would have to write special query for joined table

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted;
        switch (sURIMatcher.match(uri)) {
            case WEATHER:
                rowsDeleted = openDb().delete(WeatherEntry.TABLE_NAME,selection,selectionArgs);
                break;
            case LOCATION :
                rowsDeleted =  openDb().delete(LocationEntry.TABLE_NAME,selection,selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri" + uri);
        }

        if(selection == null || rowsDeleted != 0) {
            if (getContext() != null)
                getContext().getContentResolver().notifyChange(uri, null); //this uri changed
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int rowsUpdated;
        switch (sURIMatcher.match(uri)) {
            case WEATHER:
                rowsUpdated = openDb().update(WeatherEntry.TABLE_NAME,values,selection,selectionArgs);
                break;
            case LOCATION :
                rowsUpdated =  openDb().update(LocationEntry.TABLE_NAME,values,selection,selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri" + uri);
        }

        if(rowsUpdated != 0) {
            if (getContext() != null)
                getContext().getContentResolver().notifyChange(uri, null); //this uri changed
        }

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        switch (sURIMatcher.match(uri)) {
            case WEATHER:
                SQLiteDatabase db = openDb();
                db.beginTransaction();
                int retCount = 0;
                try {
                    for(ContentValues value: values ) {
                        long rowId =  db.insert(WeatherEntry.TABLE_NAME,null,value);
                        if (rowId != -1) retCount++;
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction(); //roll back if transaction is not successful
                }
                if(getContext()!=null) getContext().getContentResolver().notifyChange(uri,null); //this uri changed
                return retCount;
            default:
                return super.bulkInsert(uri,values);
        }
    }
}
