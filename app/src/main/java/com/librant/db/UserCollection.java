package com.librant.db;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.view.View;

import androidx.core.app.ActivityCompat;

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

    public interface UserCallback {
        void onCallback(User user);
    }

    public interface SuccessCallback {
        void onCallback(boolean success);
    }

    public void getUserById(String userId, final UserCallback userCallback) {
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                userCallback.onCallback(user);
            } else {
                userCallback.onCallback(null);
            }
        }).addOnFailureListener(e -> userCallback.onCallback(null));
    }

    public void createUser(User user, final SuccessCallback successCallback) {
        db.collection("users").document(user.getId()).set(user)
                .addOnSuccessListener(aVoid -> successCallback.onCallback(true))
                .addOnFailureListener(e -> successCallback.onCallback(false));
    }

    public void updateUserInfo(View view, User user) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", user.getName());
        updates.put("surname", user.getSurname());
        updates.put("phoneNumber", user.getPhoneNumber());
        updates.put("address", user.getAddress());
        updates.put("phoneNumberVisible", user.isPhoneNumberVisible());
        updates.put("addressVisible", user.isAddressVisible());
        updates.put("savedBooks", user.getSavedBooks());
        updates.put("viewedBooks", user.getViewedBooks());

        System.out.println(user.getId());
        System.out.println(user.getId());
        System.out.println(user.getId());
        System.out.println(user.getId());
        System.out.println(user.getId());

        db.collection("users").document(user.getId()).set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Snackbar.make(view, "Profile updated successfully", Snackbar.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(view, "Failed to update profile", Snackbar.LENGTH_SHORT).show();
                });
    }


    public boolean isLocationPermissionGranted(Activity activity) {
        return ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission(Activity activity, ActivityCompat.OnRequestPermissionsResultCallback callback, int requestCode) {
        ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
    }

    public void getLocation(Activity activity, FusedLocationProviderClient fusedLocationClient, OnSuccessListener<Address> successListener, OnFailureListener failureListener) {
        if (isLocationPermissionGranted(activity)) {
            try {
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                successListener.onSuccess(addresses.get(0));
                            } else {
                                failureListener.onFailure(new Exception("No address found"));
                            }
                        } catch (IOException e) {
                            failureListener.onFailure(e);
                        }
                    } else {
                        failureListener.onFailure(new Exception("Location not found"));
                    }
                }).addOnFailureListener(failureListener);
            } catch (SecurityException e) {
                failureListener.onFailure(e);
            }
        } else {
            failureListener.onFailure(new Exception("Location permission not granted"));
        }
    }
}
