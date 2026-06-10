package com.example.lostandfound;

import com.google.firebase.firestore.FirebaseFirestore;

public final class FirestoreProvider {
    private static final String DATABASE_ID = "unifound";

    private FirestoreProvider() {}

    public static FirebaseFirestore getFirestore() {
        return FirebaseFirestore.getInstance(DATABASE_ID);
    }
}

