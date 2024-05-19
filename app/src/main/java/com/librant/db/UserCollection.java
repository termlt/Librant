package com.librant.db;

import android.view.View;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.librant.models.User;

import java.util.List;

public class UserCollection {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface GetUserCallback {
        void onUserReceived(User user);
    }

    public void updateUserInfo(View view, User updatedUser) {
        DocumentReference userRef = db.collection("users").document(updatedUser.getId());

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            User currentUser = documentSnapshot.toObject(User.class);

            if (currentUser != null) {
                if (updatedUser.getName() != null) currentUser.setName(updatedUser.getName());
                if (updatedUser.getSurname() != null) currentUser.setSurname(updatedUser.getSurname());
                if (updatedUser.getPhoneNumber() != null) currentUser.setPhoneNumber(updatedUser.getPhoneNumber());
                if (updatedUser.getAddress() != null) currentUser.setAddress(updatedUser.getAddress());
                if (updatedUser.getSavedBooks() != null) currentUser.setSavedBooks(updatedUser.getSavedBooks());
                if (updatedUser.getViewedBooks() != null) currentUser.setViewedBooks(updatedUser.getViewedBooks());

                userRef.set(currentUser, SetOptions.merge())
                        .addOnSuccessListener(unused -> Snackbar.make(view, "Profile updated", Snackbar.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Snackbar.make(view, "Failed to update profile", Snackbar.LENGTH_SHORT).show());
            }
        }).addOnFailureListener(e -> Snackbar.make(view, "Failed to fetch current user data", Snackbar.LENGTH_SHORT).show());
    }


    public void getUserById(String id, GetUserCallback callback) {
        db.collection("users").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = new User();
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.getData() != null) {
                        user.setId((String) documentSnapshot.getData().get("id"));
                        user.setName((String) documentSnapshot.getData().get("name"));
                        user.setSurname((String) documentSnapshot.getData().get("surname"));
                        user.setAddress((String) documentSnapshot.getData().get("address"));
                        user.setPhoneNumber((String) documentSnapshot.getData().get("phoneNumber"));
                        user.setSavedBooks((List<String>) documentSnapshot.getData().get("savedBooks"));
                        user.setViewedBooks((List<String>) documentSnapshot.getData().get("viewedBooks"));
                        user.setAddressVisible((Boolean) documentSnapshot.getData().get("addressVisible"));
                        user.setPhoneNumberVisible((Boolean) documentSnapshot.getData().get("phoneNumberVisible"));
                    }
                }
                callback.onUserReceived(user);
            }
        });
    }
}
