package com.example.face2face.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.face2face.R;
import com.example.face2face.databinding.ActivityMainBinding;
import com.example.face2face.databinding.ActivityOutgoingInvitationBinding;
import com.example.face2face.models.User;

public class OutgoingInvitationActivity extends AppCompatActivity {
ActivityOutgoingInvitationBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOutgoingInvitationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String meetingtype=getIntent().getStringExtra("type");
        if (meetingtype !=null)
        {
            if (meetingtype.equals("video"))
            {
                binding.imagemeetingtype.setImageResource(R.drawable.ic_video);
            }
        }
        User user=(User) getIntent().getSerializableExtra("user");
        if (user !=null)
        {
            binding.textFirstChar.setText(user.firstName.substring(0,1));
            binding.textusername.setText(String.format("%s %s",user.firstName,user.lastName));
            binding.textEmail.setText(user.email);
        }
        binding.imagestopinvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}