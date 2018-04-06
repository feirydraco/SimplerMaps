package com.syv.simplermaps;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import org.joda.time.DateTime;

import java.io.IOException;

public class DirectionResultLoader extends AsyncTaskLoader<DirectionsResult> {
    private String from;
    private String to;
    private GeoApiContext geoApiContext;

    public DirectionResultLoader(Context context, GeoApiContext geoApiContext, String from, String to) {
        super(context);
        this.from = from;
        this.to = to;
        this.geoApiContext = geoApiContext;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public DirectionsResult loadInBackground() {
        return fetchDirection();
    }

    private DirectionsResult fetchDirection() {
        DirectionsResult directionsResult = null;
        try {
            directionsResult = DirectionsApi.newRequest(geoApiContext)
                    .mode(TravelMode.DRIVING)
                    .origin(from)
                    .destination(to)
                    .departureTime(new DateTime())
                    .await();
            if (directionsResult != null) {
                Log.v("Map", "direction result is not null");
                Log.v("Map", directionsResult.toString());
            }

        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
            Log.v("Map", "exception catch");
        }
        return directionsResult;
    }
}
