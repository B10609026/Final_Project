package com.example.final_project.location_data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LocationContentProvider extends ContentProvider {

    // Define final integer constants for the directory of tasks and a single item.
    // It's convention to use 100, 200, 300, etc for directories,
    // and related ints (101, 102, ..) for items in that directory.
    public static final int LOCATIONS = 100;
    public static final int NEARBY_WITH_ID = 101;
    public static final int LOCATION_WITH_ID = 102;
    public static final int OTHER_THAN_SELECTED_ID = 103;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher(){
        // Initialize a uriMatcher
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // Add some URIs to be matched
        uriMatcher.addURI(LocationContract.AUTHORITY, LocationContract.PATH_LOCATION, LOCATIONS);
        uriMatcher.addURI(LocationContract.AUTHORITY, LocationContract.PATH_NEARBY + "/#", NEARBY_WITH_ID);
        uriMatcher.addURI(LocationContract.AUTHORITY, LocationContract.PATH_LOCATION + "/#", LOCATION_WITH_ID);
        uriMatcher.addURI(LocationContract.AUTHORITY, LocationContract.PATH_OTHER_THAN_SELECTED + "/#", OTHER_THAN_SELECTED_ID);

        return uriMatcher;
    }

    private LocationDBHelper dbHelper;
    @Override
    public boolean onCreate() {
        Context context = getContext();
        dbHelper = new LocationDBHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // First get the read-only database
        final SQLiteDatabase database = dbHelper.getReadableDatabase();

        // Write URI match code and set a variable to return a Cursor
        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        // Query for the locations directory and write a default case
        switch (match) {
            // Query for the locations directory
            case LOCATIONS:
                retCursor = database.query(LocationContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case NEARBY_WITH_ID:
                // Get id from Uri path
                String id = uri.getPathSegments().get(1);
                // Get the item based on the current id
                retCursor = database.query(LocationContract.LocationEntry.TABLE_NAME,
                        null,
                        "_id=?",
                        new String[]{id},
                        null,
                        null,
                        null);
                break;
            case OTHER_THAN_SELECTED_ID:
                // Get id from Uri path
                String id2 = uri.getPathSegments().get(1);
                // Get the item based on the current id
                retCursor = database.query(LocationContract.LocationEntry.TABLE_NAME,
                        null,
                        "_id!=?",
                        new String[]{id2},
                        null,
                        null,
                        null);
                break;
            // Default exception
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Set a notification URI on the Cursor and return that Cursor
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Write URI matching code to identify the match for the tasks directory
        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case LOCATIONS:
                // Insert new values to the database
                long id = database.insert(LocationContract.LocationEntry.TABLE_NAME, null, values);
                if(id > 0){
                    returnUri = ContentUris.withAppendedId(LocationContract.LocationEntry.CONTENT_URI, id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
                // Default case throws an UnsupportedOperationException
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Notify the resolver if the uri has been changed, and return the newly inserted URI
        getContext().getContentResolver().notifyChange(uri, null);
        // Return constructed uri (this points to the newly inserted row of data)
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get access to the database and write URI matching code to recognize a single item
        final SQLiteDatabase database = dbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        // Keep track of the number of deleted locations
        int locationsDeleted; // starts as 0

        // The code to delete single row of data
        switch (match) {
            // Handle the single item case, recognized by the ID included in the URI path
            case LOCATION_WITH_ID:
                // Get the id from the Uri path
                String id = uri.getPathSegments().get(1);
                // Use selections/selectionArgs to filter for this ID
                locationsDeleted = database.delete(LocationContract.LocationEntry.TABLE_NAME,
                        "_id=?",
                        new String[]{id});
                Log.d("URI", String.valueOf(uri));
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver of a change and return the number of items deleted
        if (locationsDeleted!=0) {
            // A task was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return locationsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
