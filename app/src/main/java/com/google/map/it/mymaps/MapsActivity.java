package com.google.map.it.mymaps;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,
        ActivityCompat.OnRequestPermissionsResultCallback, OnMapReadyCallback, AdapterView.OnItemSelectedListener  {

    @BindView(R.id.dest_loc)
    AutoCompleteTextView destLoc;
    @BindView(R.id.setDestination)
    Button setDestination;
    private GoogleMap mMap;
    boolean isMapReady = false;
    private static final int ZOOM_RANGE = 12;
    private Marker tracer;
    private boolean isSelection;
    private Pattern mPattern;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private View snackLayout;
    private boolean isLocationPermissionEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        snackLayout = findViewById(R.id.tracking_layout);
        ButterKnife.bind(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mPattern = Pattern.compile("[^A-Za-z0-9 ,]");
        destLoc.setText("17.449915, 78.362883");

        destLoc.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.item_autocomplete));
        destLoc.addTextChangedListener(new TextObserver(destLoc));
        destLoc.setOnItemSelectedListener(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        isMapReady = true;
        if (isLocationPermissionEnabled())
            initLayout();
        else
            locationPermissionCheck();
        // Add a marker in Sydney and move the camera
        log("Map is ready");
    }

    public void log(String val) {
        Log.d("MapsActivity", val);
    }

    @OnClick(R.id.setDestination)
    public void onClick() {
        if (isMapReady) {
            String latLngStr = destLoc.getText().toString();
            latLngStr = getLocationFromAddress(latLngStr);
            log(latLngStr);
            if (validate(latLngStr)) {
                try {
                    double lat = Double.parseDouble(latLngStr.split(", ")[0]);
                    double lng = Double.parseDouble(latLngStr.split(", ")[1]);
                    LatLng latLng = new LatLng(lat, lng);
                    addMarker(latLng);
                } catch (NumberFormatException ex) {
                    log(ex.toString());
                } catch (Exception ex) {
                    log(ex.toString());
                }

            } else {

            }
        } else {
            log("Wait till map is ready");
        }

    }

    private boolean validate(String latLng) {
        boolean qualified = false;
        if (latLng == null || latLng.equals(""))
            Toast.makeText(this, "Please enter Address/ latitude, longitude", Toast.LENGTH_LONG).show();
        else if (latLng.split(", ").length != 2)
            Toast.makeText(this, "Ex: format for lat and lng is 17.12344, 78.87474", Toast.LENGTH_LONG).show();
        else
            qualified = true;
        return qualified;
    }

    private boolean isLocationPermissionEnabled() {
        return (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    private void locationPermissionCheck() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                Snackbar.make(snackLayout, R.string.location_permssion_message,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                            }
                        })
                        .show();

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            isLocationPermissionEnabled = true;
        }
    }

    public boolean isGPSEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        return (gps_enabled || network_enabled);
    }

    protected void initLayout() {

        buildMapLayout();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(10000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isMapReady)
            setMGoogleApiClient();
    }

    protected void setMGoogleApiClient() {
        if (!isGPSEnabled())
            showSettingsAlert();
        if (isMapReady && isLocationPermissionEnabled())
            mGoogleApiClient.connect();
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Location Services");

        // Setting Dialog Message
        alertDialog.setMessage("Location services not enabled. Would you like to enable?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private void buildMapLayout() {
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        setUIControls();
    }

    private void setUIControls() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionCheck();
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setTrafficEnabled(false);

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
    }

    private void setCamera(LatLng location) {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(
                location.latitude, location.longitude)).zoom(ZOOM_RANGE).build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (isMapReady && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    private void handleNewLocation(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if(null != tracer)
            tracer.setVisible(false);
        tracer = addMarker(latLng);
        setCamera(latLng);
    }
    private Marker addMarker(LatLng loc) {
        if (loc != null) {
            MarkerOptions marker = new MarkerOptions().position(new LatLng(loc.latitude,
                    loc.longitude)).title("Plot");

            return mMap.addMarker(marker);
        }
        return null;
    }
    @Override
    public void onConnected(Bundle bundle) {
        log("Location services connected.");
        if (!isLocationPermissionEnabled) {
            locationPermissionCheck();
        }
        setGoogleApiClient();
    }

    private void setGoogleApiClient() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionCheck();
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);

        if (location != null) {
            handleNewLocation(location);
        } else {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if(requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isLocationPermissionEnabled = true;
                initLayout();
            } else {
                Log.d("DEBUG", "Location permission not granted");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
      log( "Location services suspended. Please reconnect.");
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        log("Connection Failed : " + connectionResult);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        isSelection = true;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private class TextObserver implements TextWatcher {
        private AutoCompleteTextView edtView;

        public TextObserver(AutoCompleteTextView edtView) {
            this.edtView = edtView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            if (isSelection) {
                isSelection = !isSelection;
                return;
            }

            if (mPattern.matcher(s).find()) {
                log("Please enter a valid route");
            } else if (!s.toString().isEmpty() && Character.isDigit(s.charAt(0))) {
                log("Please enter a valid route");
            } else if (s.toString().length() > 3) {
                ((PlacesAutoCompleteAdapter) edtView.getAdapter()).autocomplete(s.toString());
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
        }
    }

    public String getLocationFromAddress(String strAddress){

        Geocoder coder = new Geocoder(this);

        List<Address> address;
        try {
            address = coder.getFromLocationName(strAddress,5);
            if (address==null) {
                return null;
            }
            Address location=address.get(0);
            return location.getLatitude() + ", " + location.getLongitude();
        } catch (Exception ex)  {
            log(ex.toString());
            return strAddress;
        }

    }
}
