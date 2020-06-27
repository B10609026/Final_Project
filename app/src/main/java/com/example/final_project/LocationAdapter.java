package com.example.final_project;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.Math;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.location_data.LocationContract;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationListViewHolder> {
//    final private ListItemClickListener mOnClickListener;

    private Context mContext;
    private Cursor mCursor;

    public LocationAdapter(Context context){
        this.mContext = context;
    }

    // onCreateViewHolder asks the Adapter to create viewHolders for the data
    // correspond to the number of items needed
    // This also inflate the layout created earlier
    @NonNull
    @Override
    public LocationListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.location_layout, parent, false);
        return new LocationListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationListViewHolder holder, int position) {
        int idIndex = mCursor.getColumnIndex(LocationContract.LocationEntry._ID);
        int longitudeIndex = mCursor.getColumnIndex(LocationContract.LocationEntry.COLUMN_LONGITUDE);
        int latitudeIndex = mCursor.getColumnIndex(LocationContract.LocationEntry.COLUMN_LATITUDE);
        int nameIndex = mCursor.getColumnIndex(LocationContract.LocationEntry.COLUMN_NAME);

        mCursor.moveToPosition(position);

        final int id = mCursor.getInt(idIndex);
        float longitude = mCursor.getFloat(longitudeIndex);
        float latitude = mCursor.getFloat(latitudeIndex);
        String name = mCursor.getString(nameIndex);

        // Set the tag so that the itemView could be obtained when needed
        holder.itemView.setTag(id);

        holder.location_id_text_view.setText(String.valueOf(id));
        holder.location_longitude_text_view.setText(String.valueOf(longitude));
        holder.location_latitude_text_view.setText(String.valueOf(latitude));
        holder.location_name_text_view.setText(name);
    }

    @Override
    public int getItemCount() {
        if(mCursor == null){
            return 0;
        }
        return mCursor.getCount();
    }

    public class LocationListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        TextView location_id_text_view;
        TextView location_longitude_text_view;
        TextView location_latitude_text_view;
        TextView location_name_text_view;

        public LocationListViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            location_id_text_view = (TextView) itemView.findViewById(R.id.location_ID_text_view);
            location_longitude_text_view = (TextView) itemView.findViewById(R.id.location_longitude_text_view);
            location_latitude_text_view = (TextView) itemView.findViewById(R.id.location_latitude_text_view);
            location_name_text_view = (TextView) itemView.findViewById(R.id.location_name_text_view);
        }

        @Override
        public void onClick(View v) {
            int id = (int) v.getTag();
            // Build appropriate uri with String row id appended
            String stringId = String.valueOf(id);
            Uri uri = LocationContract.LocationEntry.CONTENT_URI_NEARBY;
            uri = uri.buildUpon().appendPath(stringId).build();

            Cursor getNearby = mContext.getContentResolver().query(uri, null, null, null, null);

            Log.d("URI", String.valueOf(uri));
            Log.d("ItemView clicked", "ItemView onClick called");
            Log.d("Current Cursor", String.valueOf(getNearby));

            Log.d("Current Cursor","Column name: " + getNearby.getColumnName(0)+"");
            Log.d("Current Cursor",getNearby.getColumnName(1)+"");
            Log.d("Current Cursor",getNearby.getColumnName(2)+"");
            Log.d("Current Cursor",getNearby.getColumnName(3)+"");

            getNearby.moveToFirst();
            final int returnId = getNearby.getInt(0);
            final float returnLongitude = getNearby.getFloat(1);
            final float returnLatitude = getNearby.getFloat(2);


            String stringId2 = String.valueOf(id);
            Uri uri2 = LocationContract.LocationEntry.CONTENT_URI_OTHER_THAN_SELECTED;
            uri2 = uri2.buildUpon().appendPath(stringId).build();

            Cursor getNearby2 = mContext.getContentResolver().query(uri2, null, null, null, null);
//            Toast.makeText(mContext, Integer.toString(getNearby2.getCount()), Toast.LENGTH_SHORT).show();
            getNearby2.moveToFirst();
            float min=Float.MAX_VALUE;
            int minId = 0;
            float total = 0; //total of other longituee (for testing)
            for (int i = 0; i<getNearby2.getCount(); i++) {
                float tempLong = Math.abs((returnLongitude-getNearby2.getFloat(1)));
                float tempLat = Math.abs((returnLatitude-getNearby2.getFloat(2)));
                float temp = tempLong + tempLat;
                total+= getNearby2.getFloat(1);
                if (temp<min) {
                    min = temp;
                    minId = getNearby2.getInt(0);
                }
                getNearby2.moveToNext();
            }

            Toast.makeText(mContext, Integer.toString(minId), Toast.LENGTH_SHORT).show();
//            Toast.makeText(mContext, Float.toString(min), Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onLongClick(View v) {
            // Get the latitude and longitude to create the addressString
            String getLongitude = String.valueOf(location_longitude_text_view.getText());
            String getLatitude = String.valueOf(location_latitude_text_view.getText());
            String getLabel = String.valueOf(location_name_text_view.getText());

            // Use Uri.Builder with the appropriate scheme and query to form the Uri for the address
            String addressString = getLatitude + "," + getLongitude;
            Log.d("addressString",addressString);

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("geo")
                    .path("0.0")
                    .appendQueryParameter("q",addressString);
            Uri addressUri = builder.build();

            showMap(addressUri);
            return true;
        }
    }

    // Use to update the cursor in UI
    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // bc nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }

    private void showMap(Uri geoLocation) {
        // We create an Intent with the action, ACTION_VIEW because we want to VIEW the
        // contents of this Uri.
        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setData(geoLocation);

        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
            mContext.startActivity(intent);
        }
    }
}
