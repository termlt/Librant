package com.librant.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.librant.R;
import com.librant.activities.auth.LoginActivity;
import com.librant.db.UserCollection;
import com.librant.models.User;

public class EditProfileActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FirebaseAuth mAuth;
    private FusedLocationProviderClient fusedLocationClient;
    private TextInputEditText editTextName, editTextSurname, editTextPhoneNumber, editTextAddress;
    private SwitchMaterial switchPhoneNumber, switchAddress;
    private UserCollection userCollection;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        userCollection = new UserCollection();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

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

        userCollection.getUserById(mAuth.getCurrentUser().getUid(), user -> {
            if (user != null) {
                currentUser = user;
                editTextName.setText(user.getName());
                editTextSurname.setText(user.getSurname());
                editTextPhoneNumber.setText(user.getPhoneNumber());
                editTextAddress.setText(user.getAddress());

                switchPhoneNumber.setChecked(user.isPhoneNumberVisible());
                switchAddress.setChecked(user.isAddressVisible());

                setupSwitchListeners();
            }
        });

        buttonSave.setOnClickListener(this::saveProfile);

        buttonUpdateAddress.setOnClickListener(v -> {
            if (!userCollection.isLocationPermissionGranted(this)) {
                Snackbar.make(v, "Please allow access to your location", Snackbar.LENGTH_SHORT).show();
                userCollection.requestLocationPermission(this, this, LOCATION_PERMISSION_REQUEST_CODE);
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

    private void saveProfile(View v) {
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

        currentUser.setName(name);
        currentUser.setSurname(surname);
        currentUser.setPhoneNumber(phoneNumber);
        currentUser.setAddress(address);
        currentUser.setPhoneNumberVisible(switchPhoneNumber.isChecked());
        currentUser.setAddressVisible(switchAddress.isChecked());

        userCollection.updateUserInfo(v, currentUser);
    }

    private void getLocation(View view) {
        userCollection.getLocation(this, fusedLocationClient, address -> {
            editTextAddress.setText(address.getAddressLine(0));
        }, e -> Snackbar.make(view, "Unable to retrieve address", Snackbar.LENGTH_SHORT).show());
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
