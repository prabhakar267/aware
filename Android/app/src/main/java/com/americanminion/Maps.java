package com.americanminion;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Maps extends Fragment {


    SwipeRefreshLayout swipeLayout;
    Double latitude, longitude;
    GoogleMap map;
    TextView title;
    com.google.android.gms.maps.MapFragment mapFragment;

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


        title = (TextView) rootView.findViewById(R.id.tit);
        Log.e("here", "in map");

        l = (LinearLayout) rootView.findViewById(R.id.con);
        this.mapFragment = (com.google.android.gms.maps.MapFragment) activity.getFragmentManager()
                .findFragmentById(R.id.map);
        map = mapFragment.getMap();
        final CameraUpdate center =
                CameraUpdateFactory.newLatLng(new LatLng(21,
                        78));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

        if (map != null) {
            map.moveCamera(center);
            map.animateCamera(zoom);
        }


        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                l.setVisibility(View.VISIBLE);
                /*int i = 0;
                for (i = 0; i < centres.size(); i++) {
                    Centres_object o = centres.get(i);
                    if (o.title.equals(marker.getTitle())) {
                        index = i;
                        break;
                    }
                }

                title = (TextView) rootView.findViewById(R.id.tit);
                if (centres.get(index).title != null && !centres.get(index).title.equals("null"))
                    title.setText(centres.get(index).title);
                title = (TextView) rootView.findViewById(R.id.add);
                if (centres.get(index).add != null && !centres.get(index).add.equals("null"))
                    title.setText(centres.get(index).add);
                title = (TextView) rootView.findViewById(R.id.web);
                if (centres.get(index).web != null && !centres.get(index).web.equals("null"))
                    title.setText(centres.get(index).web);
                title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(centres.get(index).web));
                        startActivity(i);
                    }
                });
                title = (TextView) rootView.findViewById(R.id.email);
                if (centres.get(index).sontact != null && !centres.get(index).sontact.equals("null"))
                    title.setText(centres.get(index).sontact);

                title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/html");
                        intent.putExtra(Intent.EXTRA_EMAIL, centres.get(index).sontact);
                        startActivity(Intent.createChooser(intent, "Send Email"));
                    }
                });


                title = (TextView) rootView.findViewById(R.id.phone);
                if (centres.get(index).phone != null && !centres.get(index).phone.equals("null"))
                    title.setText(centres.get(index).phone);
                title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + centres.get(index).phone));
                        startActivity(intent);
                    }
                });*/

                return false;
            }
        });


        GPSTracker tracker = new GPSTracker(activity);
        if (!tracker.canGetLocation()) {
            tracker.showSettingsAlert();

        } else {
            latitude = tracker.getLatitude();
            longitude = tracker.getLongitude();
        }

        return rootView;
    }


    public void ShowMarker(Double LocationLat, Double LocationLong, String LocationName, Integer LocationIcon) {
        LatLng Coord = new LatLng(LocationLat, LocationLong);

        if (map != null) {
            map.setMyLocationEnabled(true);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(Coord, 5));

            MarkerOptions abc = new MarkerOptions();
            MarkerOptions x = abc
                    .title(LocationName)
                    .position(Coord)
                    .icon(BitmapDescriptorFactory.fromResource(LocationIcon));
            map.addMarker(x);

        }
    }


}