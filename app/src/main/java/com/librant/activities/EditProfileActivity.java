package com.librant.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.librant.activities.auth.MainActivity;
import com.librant.databinding.ActivityEditProfileBinding;
import com.librant.db.UserCollection;
import com.librant.models.User;

public class EditProfileActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FirebaseAuth mAuth;
    private ActivityEditProfileBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private TextInputEditText editTextName, editTextSurname, editTextPhoneNumber, editTextAddress;
    private SwitchMaterial switchPhoneNumber, switchAddress;
    private ImageButton backButton;
    private Button saveButton;
    private MaterialButton buttonUpdateAddress;

    private UserCollection userCollection;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViews();

        loadUserProfile();
    }


    private void setupViews() {
        mAuth = FirebaseAuth.getInstance();
        userCollection = new UserCollection();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        editTextName = binding.editTextName;
        editTextSurname = binding.editTextSurname;
        editTextPhoneNumber = binding.editTextPhoneNumber;
        editTextAddress = binding.editTextAddress;
        switchPhoneNumber = binding.switchPhoneNumber;
        switchAddress = binding.switchAddress;

        switchPhoneNumber.setChecked(true);
        switchAddress.setChecked(true);

        backButton = binding.btnBack;
        saveButton = binding.buttonSave;
        buttonUpdateAddress = binding.buttonUpdateAddress;

        backButton.setOnClickListener(v -> finish());
        saveButton.setOnClickListener(this::saveProfile);
        buttonUpdateAddress.setOnClickListener(v -> {
            if (!userCollection.isLocationPermissionGranted(this)) {
                Snackbar.make(v, "Please allow access to your location", Snackbar.LENGTH_SHORT).show();
                userCollection.requestLocationPermission(this, this, LOCATION_PERMISSION_REQUEST_CODE);
            }
            getLocation(v);
        });

    }

    private void loadUserProfile() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();

            userCollection.getUserById(userId, user -> {
                if (user != null) {
                    currentUser = user;
                    editTextName.setText(user.getName());
                    editTextSurname.setText(user.getSurname());
                    editTextPhoneNumber.setText(user.getPhoneNumber());
                    editTextAddress.setText(user.getAddress());

                    switchPhoneNumber.setChecked(user.isPhoneNumberVisible());
                    switchAddress.setChecked(user.isAddressVisible());

                    setupSwitchListeners();
                } else {
                    currentUser = new User();
                    currentUser.setId(userId);
                    currentUser.setName("");
                    currentUser.setSurname("");
                    currentUser.setPhoneNumber("");
                    currentUser.setAddress("");
                    currentUser.setPhoneNumberVisible(true);
                    currentUser.setAddressVisible(true);

                    userCollection.createUser(currentUser, success -> {
                        if (success) {
                            setupSwitchListeners();
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), "Failed to create user profile", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
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
        if (currentUser == null) {
            Snackbar.make(v, "Failed to save profile.", Snackbar.LENGTH_SHORT).show();
            return;
        }

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

        currentUser.setId(mAuth.getCurrentUser().getUid());
        currentUser.setName(name);
        currentUser.setSurname(surname);
        currentUser.setPhoneNumber(phoneNumber);
        currentUser.setAddress(address);
        currentUser.setPhoneNumberVisible(switchPhoneNumber.isChecked());
        currentUser.setAddressVisible(switchAddress.isChecked());

        userCollection.updateUserInfo(v, currentUser);
    }

    private void getLocation(View view) {
        userCollection.getLocation(this, fusedLocationClient, address ->
                editTextAddress.setText(address.getAddressLine(0)),
                e -> Snackbar.make(view, "Ensure location services are enabled", Snackbar.LENGTH_SHORT).show());
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
