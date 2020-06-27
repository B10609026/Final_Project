package com.example.final_project.location_data;

import android.net.Uri;
import android.provider.BaseColumns;

public class LocationContract {

    // The authority, which is how your code knows which Content Provider to access
    public static final String AUTHORITY = "com.example.final_project";

    // The base content URI = "content://" + <authority>
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // Define the possible paths for accessing data in this contract
    // This is the path for the "location" directory
    public static final String PATH_LOCATION = "locations";
    public static final String PATH_NEARBY = "nearby";
    public static final String PATH_OTHER_THAN_SELECTED = "other-than-selected";

    public static final class LocationEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();
        public static final Uri CONTENT_URI_NEARBY = BASE_CONTENT_URI.buildUpon().appendPath(PATH_NEARBY).build();
        public static final Uri CONTENT_URI_OTHER_THAN_SELECTED = BASE_CONTENT_URI.buildUpon().appendPath(PATH_OTHER_THAN_SELECTED).build();

        // This is for creating the table of the database
        public static final String TABLE_NAME = "location";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_NAME = "name";
    }
}
