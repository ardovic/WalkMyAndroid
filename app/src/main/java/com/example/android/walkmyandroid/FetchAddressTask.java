package com.example.android.walkmyandroid;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FetchAddressTask extends AsyncTask<Location, Void, String> {

    private Context context;
    private OnTaskCompleteListener listener;

    public FetchAddressTask(Context context) {
        this.context = context;
        listener = (MainActivity) context;
    }

    @Override
    protected String doInBackground(Location... locations) {

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        Location location = locations[0];
        List<Address> addresses = null;
        String resultMessage = "";

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if(addresses == null || addresses.size() == 0) {
                resultMessage = "Location not found";
            } else {
                Address address = addresses.get(0);
                ArrayList<String> addressPart = new ArrayList<>();

                for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressPart.add(address.getAddressLine(i));
                }

                resultMessage = TextUtils.join("\n", addressPart);
            }

        } catch (IOException e) {
            resultMessage = "Service not available";
            e.printStackTrace();
        }

        return resultMessage;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        listener.onTaskComplete(s);

    }

}
