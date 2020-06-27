package com.example.final_project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.final_project.location_data.LocationContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    // Constants for logging and referring to a unique loader
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LOCATION_LOADER_ID = 0;

    private RecyclerView mRecyclerView;
    private LocationAdapter mAdapter;

    private EditText mLongitudeEditText;
    private EditText mLatitudeEditText;
    private EditText mNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.location_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new LocationAdapter(this);

        mRecyclerView.setAdapter(mAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Construct the Uri for the item to delete
                int id = (int) viewHolder.itemView.getTag();

                Log.e("NullPointer", "viewHolder.itemView error");

                // Build appropriate uri with String row id appended
                String stringId = Integer.toString(id);
                Uri uri = LocationContract.LocationEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(stringId).build();
                Log.d("URI", String.valueOf(uri));

                // Delete a single row of data using a ContentResolver
                getContentResolver().delete(uri, null, null);

                // Restart the loader to re-query for all tasks after a deletion
                getSupportLoaderManager().restartLoader(LOCATION_LOADER_ID, null, MainActivity.this);
                Log.d("Swipe delete data","Delete success");
            }
        }).attachToRecyclerView(mRecyclerView);

        /*
         Ensure a loader is initialized and active. If the loader doesn't already exist, one is
         created, otherwise the last created loader is re-used.
         */
//        getLoaderManager().initLoader()
        getSupportLoaderManager().initLoader(LOCATION_LOADER_ID, null, this);
    }

    // Don't need this as we only use one activity
    @Override
    protected void onResume() {
        super.onResume();
        // re-queries for all locations
        getSupportLoaderManager().restartLoader(LOCATION_LOADER_ID, null, this);
    }

    public void onClickAddLocation(View view){
        mLongitudeEditText = (EditText) findViewById(R.id.longitude_edit_text);
        mLatitudeEditText = (EditText) findViewById(R.id.latitude_edit_text);
        mNameEditText = (EditText) findViewById(R.id.name_edit_text);

        float longitude = Float.parseFloat(mLongitudeEditText.getText().toString());
        float latitude = Float.parseFloat(mLatitudeEditText.getText().toString());
        String name = mNameEditText.getText().toString();

        if(mLongitudeEditText.getText().length() == 0 || mLatitudeEditText.getText().length() == 0
                || mNameEditText.getText().length() == 0) return;

        // ContentValues is used to put the data correspond to the table's column
        ContentValues contentValues = new ContentValues();

        contentValues.put(LocationContract.LocationEntry.COLUMN_LONGITUDE, longitude);
        contentValues.put(LocationContract.LocationEntry.COLUMN_LATITUDE, latitude);
        contentValues.put(LocationContract.LocationEntry.COLUMN_NAME, name);

        Uri uri = getContentResolver().insert(LocationContract.LocationEntry.CONTENT_URI, contentValues);

        // Show the data in the uri in the toast
        if(uri != null){
            Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
            Log.d("Add Location",String.valueOf(uri));
        }
        getSupportLoaderManager().restartLoader(LOCATION_LOADER_ID, null, this);
        mLongitudeEditText.clearFocus();
        mLongitudeEditText.getText().clear();
        mLatitudeEditText.getText().clear();
        mNameEditText.getText().clear();
        Log.d("Add Clicked", "Add success");
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {
            // Initialize a Cursor, this will hold all the location data
            Cursor mLocationData = null;

            // onStartLoading() is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                if(mLocationData != null){
                    // Delivers any previously loaded data immediately
                    deliverResult(mLocationData);
                } else {
                    // Force a new load
                    forceLoad();
                }
            }

            // Performs asynchronous loading of data
            @Nullable
            @Override
            public Cursor loadInBackground() {
                // Will implement to load data
                try {
                    return getContentResolver().query(LocationContract.LocationEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);
                } catch (Exception e) {
                    Log.e("Load Failed", "Failed to load data asynchronously");
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(@Nullable Cursor data) {
                mLocationData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        Log.d("onLoadFinished", "onLoadFinished called");
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
        Log.d("onLoaderReset", "onLoaderReset called");
    }
}