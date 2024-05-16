package com.librant.fragments.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.librant.R;
import com.librant.activities.auth.LoginActivity;
import com.librant.db.UserCollection;
import com.librant.models.User;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class EditProfileFragment extends Fragment {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UserCollection userCollection;
    private FusedLocationProviderClient fusedLocationClient;
    private TextInputEditText editTextName, editTextSurname, editTextPhoneNumber, editTextAddress;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }

        userCollection = new UserCollection();
        db = FirebaseFirestore.getInstance();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        Button backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

        ImageView buttonUpdateAddress = view.findViewById(R.id.imageViewUpdateAddress);
        Button buttonSave = view.findViewById(R.id.buttonUpdate);

        editTextName = view.findViewById(R.id.editTextName);
        editTextSurname = view.findViewById(R.id.editTextSurname);
        editTextPhoneNumber = view.findViewById(R.id.editTextPhoneNumber);
        editTextAddress = view.findViewById(R.id.editTextAddress);

        db.collection("users").document(mAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        editTextName.setText(documentSnapshot.getString("name"));
                        editTextSurname.setText(documentSnapshot.getString("surname"));
                        editTextPhoneNumber.setText(documentSnapshot.getString("phoneNumber"));
                        editTextAddress.setText(documentSnapshot.getString("address"));
                    }
                });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = new User();

                String name = String.valueOf(editTextName.getText());
                String surname = String.valueOf(editTextSurname.getText());
                String phoneNumber = String.valueOf(editTextPhoneNumber.getText());
                String address = String.valueOf(editTextAddress.getText());

                if (name.isEmpty() || surname.isEmpty()) {
                    Snackbar.make(view, "Name and surname fields cannot be empty",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (phoneNumber.isEmpty() && address.isEmpty()) {
                    Snackbar.make(view, "At least one contact field (phone number or address) must be filled",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }

                user.setId(mAuth.getCurrentUser().getUid());
                user.setName(name);
                user.setSurname(surname);
                user.setPhoneNumber(phoneNumber);
                user.setAddress(address);
                userCollection.updateUserInfo(view, user);
            }
        });

        buttonUpdateAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLocationPermissionGranted()) {
                    Snackbar.make(view, "Please allow access to your location",
                            Snackbar.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                    return;
                }

                getLocation(view);
            }
        });

        return view;
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

    }

    private void getLocation(View view) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            getAddressFromLocation(view, latitude, longitude);
                        } else {
                            Snackbar.make(view, "Please turn on location",
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void getAddressFromLocation(View view, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);

                editTextAddress.setText(address);
            } else {
                Snackbar.make(view, "Unable to retrieve address",
                        Snackbar.LENGTH_SHORT).show();
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
                getLocation(getView());
            } else {
                if (getView() != null) {
                    Snackbar.make(getView(), "Permission denied",
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }

}
