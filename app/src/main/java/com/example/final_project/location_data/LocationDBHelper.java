package com.example.final_project.location_data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class LocationDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "location.db";

    private static final int DATABASE_VERSION = 1;

    public LocationDBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationContract.LocationEntry.TABLE_NAME + " (" +
                LocationContract.LocationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LocationContract.LocationEntry.COLUMN_LONGITUDE + " REAL NOT NULL, " +
                LocationContract.LocationEntry.COLUMN_LATITUDE + " REAL NOT NULL, " +
                LocationContract.LocationEntry.COLUMN_NAME + " TEXT " + "); ";
        db.execSQL(CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + LocationContract.LocationEntry.TABLE_NAME);
        onCreate(db);
    }
}
