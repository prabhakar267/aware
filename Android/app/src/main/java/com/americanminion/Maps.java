package com.americanminion;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class Maps extends Fragment implements Constants, OnMapReadyCallback {

    Double latitude, longitude;
    GoogleMap map;
    Place Splace, Dplace;
    int random;

    com.google.android.gms.maps.MapFragment mapFragment;
    private Handler mHandler;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    Activity activity;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        this.activity = (Activity) activity;
    }

    LinearLayout l;
    int index = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.content_maps, container, false);
        if (activity != null) {
            InputMethodManager input = (InputMethodManager) activity
                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (activity.getCurrentFocus() != null)
                input.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }


        mHandler = new Handler(Looper.getMainLooper());


        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                activity.getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Splace = place;
                Log.i(TAG, "SPlace: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        autocompleteFragment.setHint("Enter source");


        autocompleteFragment = (PlaceAutocompleteFragment)
                activity.getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment2);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Dplace = place;
                Log.i(TAG, "DPlace: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        autocompleteFragment.setHint("Enter destination");


        Button button = (Button) rootView.findViewById(R.id.findRoute);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Splace == null) {
                    Toast.makeText(activity, "Enter source", Toast.LENGTH_SHORT).show();
                } else if (Dplace == null) {
                    Toast.makeText(activity, "Enter destination", Toast.LENGTH_SHORT).show();
                } else {
                    getDirections();
                }
            }
        });


        this.mapFragment = (com.google.android.gms.maps.MapFragment) activity.getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        return rootView;
    }


    /**
     * Calls API to get directions
     */
    public void getDirections() {

        map.clear();

        final String sorcelat = Double.toString(Splace.getLatLng().latitude),
                sorcelon = Double.toString(Splace.getLatLng().longitude),
                deslat = Double.toString(Dplace.getLatLng().latitude),
                deslon = Double.toString(Dplace.getLatLng().longitude);


        ShowMarker(Splace.getLatLng().latitude, Splace.getLatLng().longitude, "Source", R.drawable.ic_pin_drop_black_24dp);
        ShowMarker(Dplace.getLatLng().latitude, Dplace.getLatLng().longitude, "destination", R.drawable.ic_pin_drop_black_24dp);

        // Show a dialog box
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Fetching route, Please wait...");
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        String uri = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                sorcelat + "," + sorcelon + "&destination=" + deslat + "," + deslon +
                "&key=" +
                MAPS_KEY +
                "&mode=walking&alternatives=true";

        Log.e("CALLING : ", uri);


        random = (int) (Splace.getLatLng().latitude + Splace.getLatLng().longitude +
                Dplace.getLatLng().latitude + Dplace.getLatLng().longitude );



        //Set up client
        OkHttpClient client = new OkHttpClient();
        //Execute request
        Request request = new Request.Builder()
                .url(uri)
                .build();
        //Setup callback
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Request Failed", "Message : " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String res = response.body().string();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("RESPONSE : ", "Done");
                        try {



                            final JSONObject json = new JSONObject(res);
                            JSONArray routeArray = json.getJSONArray("routes");


                            random = random % (routeArray.length());



                            for(int j=0; j<routeArray.length(); j++) {

                                String col = "#00bcd4";
                                if(j==random){
                                    col = "#4caf50";
                                }



                                JSONObject routes = routeArray.getJSONObject(j);
                                JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
                                String encodedString = overviewPolylines.getString("points");
                                List<LatLng> list = decodePoly(encodedString);
                                Polyline line = map.addPolyline(new PolylineOptions()
                                        .addAll(list)
                                        .width(12)
                                        .color(Color.parseColor(col))//Google maps green color
                                        .geodesic(true)
                                );

                                for (int z = 0; z < list.size() - 1; z++) {
                                    LatLng src = list.get(z);
                                    LatLng dest = list.get(z + 1);
                                    Polyline line2 = map.addPolyline(new PolylineOptions()
                                            .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude, dest.longitude))
                                            .width(2)
                                            .color(Color.BLUE).geodesic(true));
                                }



                            }


                            progressDialog.hide();
                            LatLng coordinate = new LatLng(Double.parseDouble(sorcelat), Double.parseDouble(sorcelon));
                            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
                            map.animateCamera(yourLocation);
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                    }
                });
            }
        });
    }

    /**
     * Displays path on path
     *
     * @param encoded Encoded string that contains path
     * @return Points on map
     */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }


    @Override
    public void onMapReady(GoogleMap map) {

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMyLocationEnabled(true);
        map.setTrafficEnabled(false);
        map.setIndoorEnabled(false);
        map.setBuildingsEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(true);


        final CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(21, 78));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

        map.moveCamera(center);
        map.animateCamera(zoom);

        GPSTracker tracker = new GPSTracker(activity);
        if (!tracker.canGetLocation()) {
            tracker.showSettingsAlert();

        } else {
            latitude = tracker.getLatitude();
            longitude = tracker.getLongitude();
            LatLng coordinate = new LatLng(latitude, longitude);
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 5);
            map.animateCamera(yourLocation);

        }

        this.map = map;

    }


    /**
     * Sets marker at given location on map
     *
     * @param LocationLat  latitude
     * @param LocationLong longitude
     * @param LocationName name of location
     * @param LocationIcon icon
     */
    public void ShowMarker(Double LocationLat, Double LocationLong, String LocationName, Integer LocationIcon) {
        LatLng Coord = new LatLng(LocationLat, LocationLong);

        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (map != null) {
                map.setMyLocationEnabled(true);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(Coord, 10));

                MarkerOptions abc = new MarkerOptions();
                MarkerOptions x = abc
                        .title(LocationName)
                        .position(Coord)
                        .icon(BitmapDescriptorFactory.fromResource(LocationIcon));
                map.addMarker(x);

            }
        }
    }


}