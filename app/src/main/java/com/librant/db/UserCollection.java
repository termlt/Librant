package com.librant.db;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.librant.models.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserCollection {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface GetUserCallback {
        void onUserReceived(User user);
    }

    public void getUserById(String id, GetUserCallback callback) {
        db.collection("users").document(id).get().addOnSuccessListener(documentSnapshot -> {
            User user = new User();
            if (documentSnapshot.exists() && documentSnapshot.getData() != null) {
                user.setId((String) documentSnapshot.get("id"));
                user.setName((String) documentSnapshot.get("name"));
                user.setSurname((String) documentSnapshot.get("surname"));
                user.setAddress((String) documentSnapshot.get("address"));
                user.setPhoneNumber((String) documentSnapshot.get("phoneNumber"));
                user.setSavedBooks((List<String>) documentSnapshot.get("savedBooks"));
                user.setViewedBooks((List<String>) documentSnapshot.get("viewedBooks"));
                user.setAddressVisible((Boolean) documentSnapshot.get("addressVisible"));
                user.setPhoneNumberVisible((Boolean) documentSnapshot.get("phoneNumberVisible"));
            }
            callback.onUserReceived(user);
        });
    }

    public void updateUserInfo(View view, User user) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", user.getName());
        updates.put("surname", user.getSurname());
        updates.put("phoneNumber", user.getPhoneNumber());
        updates.put("address", user.getAddress());
        updates.put("phoneNumberVisible", user.isPhoneNumberVisible());
        updates.put("addressVisible", user.isAddressVisible());

        db.collection("users").document(user.getId()).set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Snackbar.make(view, "Profile updated successfully", Snackbar.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Snackbar.make(view, "Failed to update profile", Snackbar.LENGTH_SHORT).show());
    }

    public boolean isLocationPermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission(ActivityCompat.OnRequestPermissionsResultCallback callback, Context context, int requestCode) {
        ActivityCompat.requestPermissions((android.app.Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
    }

    public void getLocation(Context context, FusedLocationProviderClient fusedLocationClient, OnSuccessListener<Address> onSuccessListener, OnFailureListener onFailureListener) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            onFailureListener.onFailure(new Exception("Location permission not granted"));
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        getAddressFromLocation(context, latitude, longitude, onSuccessListener, onFailureListener);
                    } else {
                        onFailureListener.onFailure(new Exception("Location is null"));
                    }
                })
                .addOnFailureListener(onFailureListener);
    }

    private void getAddressFromLocation(Context context, double latitude, double longitude, OnSuccessListener<Address> onSuccessListener, OnFailureListener onFailureListener) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                onSuccessListener.onSuccess(addresses.get(0));
            } else {
                onFailureListener.onFailure(new Exception("Unable to retrieve address"));
            }
        } catch (IOException e) {
            onFailureListener.onFailure(e);
        }
    }
}
