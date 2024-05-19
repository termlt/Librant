package com.librant.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.librant.R;
import com.librant.activities.auth.LoginActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private TextInputEditText editTextName, editTextSurname, editTextPhoneNumber, editTextAddress;
    private SwitchMaterial switchPhoneNumber, switchAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        ImageView buttonUpdateAddress = findViewById(R.id.imageViewUpdateAddress);
        Button buttonSave = findViewById(R.id.buttonUpdate);

        editTextName = findViewById(R.id.editTextName);
        editTextSurname = findViewById(R.id.editTextSurname);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextAddress = findViewById(R.id.editTextAddress);
        switchPhoneNumber = findViewById(R.id.switchPhoneNumber);
        switchAddress = findViewById(R.id.switchAddress);

        switchPhoneNumber.setChecked(true);
        switchAddress.setChecked(true);

        db.collection("users").document(mAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String phoneNumber = documentSnapshot.getString("phoneNumber");
                        String address = documentSnapshot.getString("address");
                        Boolean phoneNumberVisible = documentSnapshot.getBoolean("phoneNumberVisible");
                        Boolean addressVisible = documentSnapshot.getBoolean("addressVisible");

                        editTextName.setText(documentSnapshot.getString("name"));
                        editTextSurname.setText(documentSnapshot.getString("surname"));
                        editTextPhoneNumber.setText(phoneNumber);
                        editTextAddress.setText(address);

                        if (phoneNumberVisible != null) {
                            switchPhoneNumber.setChecked(phoneNumberVisible);
                        }
                        if (addressVisible != null) {
                            switchAddress.setChecked(addressVisible);
                        }

                        setupSwitchListeners();
                    }
                });

        buttonSave.setOnClickListener(v -> {
            String name = String.valueOf(editTextName.getText());
            String surname = String.valueOf(editTextSurname.getText());
            String phoneNumber = String.valueOf(editTextPhoneNumber.getText());
            String address = String.valueOf(editTextAddress.getText());

            if (name.isEmpty() || surname.isEmpty()) {
                Snackbar.make(v, "Name and surname fields cannot be empty", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (phoneNumber.isEmpty() && address.isEmpty()) {
                Snackbar.make(v, "At least one contact field (phone number or address) must be filled", Snackbar.LENGTH_SHORT).show();
                return;
            }

            boolean isPhoneNumberVisible = switchPhoneNumber.isChecked();
            boolean isAddressVisible = switchAddress.isChecked();

            Map<String, Object> updates = new HashMap<>();
            updates.put("name", name);
            updates.put("surname", surname);
            updates.put("phoneNumber", phoneNumber);
            updates.put("address", address);
            updates.put("phoneNumberVisible", isPhoneNumberVisible);
            updates.put("addressVisible", isAddressVisible);

            db.collection("users").document(mAuth.getCurrentUser().getUid()).set(updates, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Snackbar.make(v, "Profile updated successfully", Snackbar.LENGTH_SHORT).show();
                        new android.os.Handler().postDelayed(() -> finish(), 3000);
                    })
                    .addOnFailureListener(e -> Snackbar.make(v, "Failed to update profile", Snackbar.LENGTH_SHORT).show());
        });

        buttonUpdateAddress.setOnClickListener(v -> {
            if (!isLocationPermissionGranted()) {
                Snackbar.make(v, "Please allow access to your location", Snackbar.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                return;
            }

            getLocation(v);
        });
    }

    private void setupSwitchListeners() {
        switchPhoneNumber.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked && (editTextAddress.getText() == null || editTextAddress.getText().toString().isEmpty())) {
                switchPhoneNumber.setChecked(true);
                Snackbar.make(findViewById(android.R.id.content), "At least one contact field must be visible", Snackbar.LENGTH_SHORT).show();
            } else if (!isChecked) {
                switchAddress.setChecked(true);
            }
        });

        switchAddress.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked && (editTextPhoneNumber.getText() == null || editTextPhoneNumber.getText().toString().isEmpty())) {
                switchAddress.setChecked(true);
                Snackbar.make(findViewById(android.R.id.content), "At least one contact field must be visible", Snackbar.LENGTH_SHORT).show();
            } else if (!isChecked) {
                switchPhoneNumber.setChecked(true);
            }
        });
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void getLocation(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        getAddressFromLocation(view, latitude, longitude);
                    } else {
                        Snackbar.make(view, "Please turn on location", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void getAddressFromLocation(View view, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);

                editTextAddress.setText(address);
            } else {
                Snackbar.make(view, "Unable to retrieve address", Snackbar.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation(findViewById(android.R.id.content));
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Permission denied", Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
