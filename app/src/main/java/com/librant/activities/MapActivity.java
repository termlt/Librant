package com.librant.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.maps.DirectionsApi;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.TravelMode;
import com.librant.BuildConfig;
import com.librant.R;
import com.librant.activities.auth.MainActivity;
import com.librant.databinding.ActivityMapBinding;
import com.librant.db.UserCollection;

import java.util.List;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap gMap;
    private MaterialCardView infoButton;
    private String currentLocation, ownerLocation;
    private String distance, duration;
    private ActivityMapBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        Intent intent = getIntent();

        UserCollection userCollection = new UserCollection();
        userCollection.getUserById(mAuth.getCurrentUser().getUid(), user -> {
            currentLocation = user.getAddress();

            if (intent != null && intent.hasExtra("ownerLocation")) {
                ownerLocation = intent.getStringExtra("ownerLocation");

                SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                if (supportMapFragment != null) {
                    supportMapFragment.getMapAsync(this);
                }

                bindViews();
            } else {
                finish();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        gMap = googleMap;

        drawPolyline();
    }

    private void bindViews() {
        infoButton = binding.infoButton;
        binding.backButton.setOnClickListener(v -> finish());
    }

    @SuppressLint("PotentialBehaviorOverride")
    private void drawPolyline() {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(BuildConfig.GOOGLE_MAPS_API_KEY)
                .build();

        DirectionsResult result = null;
        try {
            result = DirectionsApi.newRequest(context)
                    .origin(currentLocation)
                    .destination(ownerLocation)
                    .await();
        } catch (Exception e) {
            Snackbar.make(binding.getRoot(), "Please update your location", Snackbar.LENGTH_SHORT).show();
            new Handler().postDelayed(() ->  finish(), 4000);
            return;
        }

        DirectionsRoute route = result.routes[0];
        String polyline = route.overviewPolyline.getEncodedPath();

        List<com.google.android.gms.maps.model.LatLng> decodedPath = PolyUtil.decode(polyline);

        gMap.addPolyline(new PolylineOptions().addAll(decodedPath)).setColor(getResources().getColor(R.color.map_polyline));

        MarkerOptions startMarkerOptions = new MarkerOptions()
                .position(decodedPath.get(0))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        Marker startMarker = gMap.addMarker(startMarkerOptions);
        startMarker.setTag(currentLocation);

        MarkerOptions endMarkerOptions = new MarkerOptions()
                .position(decodedPath.get(decodedPath.size() - 1))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        Marker endMarker = gMap.addMarker(endMarkerOptions);
        endMarker.setTag(ownerLocation);

        gMap.setOnMarkerClickListener(marker -> {
            showDialog((String) marker.getTag());
            return false;
        });

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : decodedPath) {
            builder.include(point);
        }

        LatLngBounds bounds = builder.build();

        gMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

        DistanceMatrixApi.newRequest(context)
                .origins(currentLocation)
                .destinations(ownerLocation)
                .mode(TravelMode.DRIVING)
                .setCallback(new PendingResult.Callback<DistanceMatrix>() {
                    @Override
                    public void onResult(DistanceMatrix result) {
                        DistanceMatrixElement element = result.rows[0].elements[0];
                        distance = element.distance.humanReadable;
                        duration = element.duration.humanReadable;
                        runOnUiThread(() -> infoButton.setOnClickListener(v -> showBottomSheetDialog(distance, duration)));
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private void showDialog(String location) {
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Location")
                .setMessage(location)
                .setPositiveButton("OK", (dialogInterface, which) -> dialogInterface.dismiss())
                .show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GRAY);
    }

    private void showBottomSheetDialog(String distance, String eta) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_map, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        MaterialTextView distanceTextView = bottomSheetView.findViewById(R.id.distance);
        MaterialTextView etaTextView = bottomSheetView.findViewById(R.id.eta);

        distanceTextView.setText(distance);
        etaTextView.setText(eta);

        bottomSheetDialog.show();
    }
}