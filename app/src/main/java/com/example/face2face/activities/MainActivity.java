package com.example.face2face.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.example.face2face.R;
import com.example.face2face.adapters.UsersAdapter;
import com.example.face2face.databinding.ActivityMainBinding;
import com.example.face2face.databinding.ActivitySignInBinding;
import com.example.face2face.listeners.UsersListener;
import com.example.face2face.models.User;
import com.example.face2face.utilities.Constants;
import com.example.face2face.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UsersListener {
    ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<User> users;
    private UsersAdapter usersAdapter;

private int REQUEST_CODE_BATTERY_OPTIMIZATION=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        binding.textSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });


        binding.textTitle.setText(String.format("%s %s", preferenceManager.getString(Constants.KEY_FIRST_NAME)
                , preferenceManager.getString(Constants.KEY_LAST_NAME)));

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
           sentFCMTokenToDatabase(task.getResult().getToken());
            }
        });


        users=new ArrayList<>();
        usersAdapter=new UsersAdapter(users,this);
        binding.usersRecyclerView.setAdapter(usersAdapter);

        binding.swipeRefreshLayout.setOnRefreshListener(this::getUsers);

        getUsers();
        checkforbatteryoptimization();


    }
    private void getUsers()
    {
        binding.swipeRefreshLayout.setRefreshing(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                binding.swipeRefreshLayout.setRefreshing(false);
                String myUserId=preferenceManager.getString(Constants.KEY_USER_ID);
                if (task.isSuccessful() && task.getResult()!=null)
                {
                    users.clear();
                    for (QueryDocumentSnapshot documentSnapshot:task.getResult())
                    {
                        if (myUserId.equals(documentSnapshot.getId()))
                        {
                            continue;
                        }
                        User user=new User();
                        user.firstName=documentSnapshot.getString(Constants.KEY_FIRST_NAME);
                        user.lastName=documentSnapshot.getString(Constants.KEY_LAST_NAME);
                        user.email=documentSnapshot.getString(Constants.KEY_EMAIL);
                        user.token=documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                        users.add(user);
                    }
                    if (users.size()>0)
                    {
                        usersAdapter.notifyDataSetChanged();
                    }
                    else
                    {
                        binding.textErrorMessage.setText(String.format("%s","No users available"));
                        binding.textErrorMessage.setVisibility(View.VISIBLE);
                    }


                }
                else {
                    binding.textErrorMessage.setText(String.format("%s","No users available"));
                    binding.textErrorMessage.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void sentFCMTokenToDatabase(String token) {
      FirebaseFirestore database=FirebaseFirestore.getInstance();
      DocumentReference documentReference=database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));

      documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Token updated successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Unable to send token " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void signOut()
    {
        Toast.makeText(this,"Signing Out",Toast.LENGTH_SHORT).show();
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        HashMap<String,Object> updates=new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                preferenceManager.clearPreferences();
                startActivity(new Intent(getApplicationContext(),SignInActivity.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Unable to sign out", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void initiateVideoMeeting(User user) {
        if (user.token==null || user.token.trim().isEmpty())
        {
            Toast.makeText(this,user.firstName+" "+user.lastName+" is not available for meeting",Toast.LENGTH_SHORT).show();
        }
        else {
          Intent intent=new Intent(getApplicationContext(),OutgoingInvitationActivity.class);
          intent.putExtra("user",user);
          intent.putExtra("type","video");
          startActivity(intent);
        }
    }

    @Override
    public void initiateAudioMeeting(User user) {
        if (user.token==null || user.token.trim().isEmpty())
        {
            Toast.makeText(this,user.firstName+" "+user.lastName+" is not available for meeting",Toast.LENGTH_SHORT).show();
        }
        else {
           Intent intent=new Intent(getApplicationContext(),OutgoingInvitationActivity.class);
           intent.putExtra("user",user);
           intent.putExtra("type","audio");
           startActivity(intent);
        }
    }

    @Override
    public void onMultipleUsersAction(Boolean isMultipleUsersSelected) {
        if (isMultipleUsersSelected)
        {
            binding.imageconference.setVisibility(View.VISIBLE);
            binding.imageconference.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(getApplicationContext(),OutgoingInvitationActivity.class);
                    intent.putExtra("selectedUsers",new Gson().toJson(usersAdapter.getSelectedUsers()));
                    intent.putExtra("type","video");
                    intent.putExtra("isMultiple",true);
                    startActivity(intent);
                }
            });
        }
        else {
            binding.imageconference.setVisibility(View.GONE);
        }
    }
    private void checkforbatteryoptimization()
    {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            PowerManager powerManager=(PowerManager)getSystemService(POWER_SERVICE);
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName()))
            {
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Waring");
                builder.setMessage("Battery optimization is enabled.It can interrupt running backgroung serivces");
                builder.setPositiveButton("Disable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent=new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        startActivityForResult(intent,REQUEST_CODE_BATTERY_OPTIMIZATION);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_CODE_BATTERY_OPTIMIZATION)
        {
            checkforbatteryoptimization();
        }
    }
}