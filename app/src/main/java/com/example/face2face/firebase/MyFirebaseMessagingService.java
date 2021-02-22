package com.example.face2face.firebase;

import android.nfc.Tag;
import android.util.Log;
import android.widget.Toast;

import com.example.face2face.activities.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;

import androidx.annotation.NonNull;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
       Log.d(TAG,"Token :"+token);

    }
}
