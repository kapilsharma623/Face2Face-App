package com.example.face2face.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.face2face.R;
import com.example.face2face.databinding.ActivityMainBinding;
import com.example.face2face.databinding.ActivityOutgoingInvitationBinding;
import com.example.face2face.models.User;
import com.example.face2face.network.ApiClient;
import com.example.face2face.network.ApiService;
import com.example.face2face.utilities.Constants;
import com.example.face2face.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

public class OutgoingInvitationActivity extends AppCompatActivity {
    ActivityOutgoingInvitationBinding binding;
    private PreferenceManager preferenceManager;
    private String inviteToken = null;
    private String meetingRoom=null;
    private String meetingType=null;
    private int rejectionCount=0;
    private int totalReceivers=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOutgoingInvitationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        meetingType=getIntent().getStringExtra("type");

        String meetingtype = getIntent().getStringExtra("type");
        if (meetingtype != null) {
            if (meetingtype.equals("video")) {
                binding.imagemeetingtype.setImageResource(R.drawable.ic_video);
            }
            else {
                binding.imagemeetingtype.setImageResource(R.drawable.ic_audio);
            }
        }
        User user = (User) getIntent().getSerializableExtra("user");
        if (user != null) {
            binding.textFirstChar.setText(user.firstName.substring(0, 1));
            binding.textusername.setText(String.format("%s %s", user.firstName, user.lastName));
            binding.textEmail.setText(user.email);
        }
        binding.imagestopinvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getIntent().getBooleanExtra("isMultiple", false))
                {
                    Type type = new TypeToken<User>() {
                    }.getType();
                    ArrayList<User> receivers = new Gson().fromJson(getIntent().getStringExtra("selectedUsers"), type);
                    cancelInvitation(null, receivers);
                }
                else {
                    if (user != null) {
                        cancelInvitation(user.token,null);
                    }
                }
            }
        });

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    inviteToken = task.getResult().getToken();
                    if (meetingtype != null) {
                        if (getIntent().getBooleanExtra("isMultiple", false)) {
                            Type type = new TypeToken<User>() {

                            }.getType();
                            ArrayList<User> receivers = new Gson().fromJson(getIntent().getStringExtra("selectedUsers"), type);
                            if (receivers!=null)
                            {
                                totalReceivers=receivers.size();
                            }
                            initiateMeeting(meetingType, null, receivers);
                        } else {
                            if (user != null) {
                                totalReceivers=1;
                                initiateMeeting(meetingtype, user.token,null);
                            }
                        }
                    }
                }
            }
        });

    }

    private void initiateMeeting(String meetingType, String receiverToken, ArrayList<User> receivers) {
        try {
            JSONArray tokens = new JSONArray();

            if (receiverToken!=null)
            {
                tokens.put(receiverToken);
            }
            if (receivers!=null && receivers.size()>0)
            {
                StringBuilder userNames=new StringBuilder();
                for (int i=0;i<receivers.size();i++)
                {
                    tokens.put(receivers.get(i).token);
                    userNames.append(receivers.get(i).firstName).append(" ").append(receivers.get(i).lastName).append("\n");
                }
                binding.textFirstChar.setVisibility(View.GONE);
                binding.textEmail.setVisibility(View.GONE);
                binding.textusername.setText(userNames.toString());
            }

            tokens.put(receiverToken);
            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);
            data.put(Constants.KEY_FIRST_NAME, preferenceManager.getString(Constants.KEY_FIRST_NAME));
            data.put(Constants.KEY_LAST_NAME, preferenceManager.getString(Constants.KEY_LAST_NAME));
            data.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, inviteToken);

            meetingRoom=preferenceManager.getString(Constants.KEY_USER_ID)+" "+ UUID.randomUUID().toString().substring(0,5);
            data.put(Constants.REMOTE_MSG_MEETING_ROOM,meetingRoom);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION);
        } catch (Exception exception) {
            Toast.makeText(OutgoingInvitationActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type) {
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(Constants.getRemoteMessageHeader(), remoteMessageBody)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            if (type.equals(Constants.REMOTE_MSG_INVITATION)) {
                                Toast.makeText(OutgoingInvitationActivity.this, "Invitation Sent Sucessfully", Toast.LENGTH_SHORT).show();
                            } else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)) {
                                Toast.makeText(OutgoingInvitationActivity.this, "Invitation Cancelled", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(OutgoingInvitationActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Toast.makeText(OutgoingInvitationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void cancelInvitation(String receiverToken,ArrayList<User> receivers) {
        try {
            JSONArray tokens = new JSONArray();
            if (receiverToken!=null)
            {
                tokens.put(receiverToken);
            }
            if (receivers!=null && receivers.size()>0)
            {
                for (User user:receivers)
                {
                    tokens.put(user.token);
                }
            }


            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, Constants.REMOTE_MSG_INVITATION_CANCELLED);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION_CANCELLED);
        } catch (Exception exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if (type != null) {
                if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)) {
                   try {
                       URL serverURL=new URL("htptps://meet.jit.si");

                       JitsiMeetConferenceOptions.Builder builder=new JitsiMeetConferenceOptions.Builder();
                       builder.setServerURL(serverURL);
                       builder.setWelcomePageEnabled(false);
                       builder.setRoom(meetingRoom);
                       if (meetingType.equals("audio"))
                       {
                           builder.setVideoMuted(true);
                       }
                       JitsiMeetActivity.launch(OutgoingInvitationActivity.this,builder.build());
                       finish();
                   }
                   catch (Exception exception)
                   {
                       Toast.makeText(context,exception.getMessage(), Toast.LENGTH_SHORT).show();
                       finish();
                   }
                } else if (type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)) {
                    rejectionCount+=1;
                    if (rejectionCount==totalReceivers)
                    {
                        Toast.makeText(context, "Invitation Rejected", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );
    }

}