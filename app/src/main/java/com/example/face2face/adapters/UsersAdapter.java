package com.example.face2face.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.face2face.R;
import com.example.face2face.databinding.ItemContainerUserBinding;
import com.example.face2face.listeners.UsersListener;
import com.example.face2face.models.User;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder>{
    private List<User> users;
    private UsersListener usersListener;

    public UsersAdapter(List<User> users,UsersListener usersListener) {
        this.users = users;
        this.usersListener=usersListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_user,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

     class UserViewHolder extends RecyclerView.ViewHolder
    {
        ItemContainerUserBinding binding;
         UserViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        void setUserData(User user)
        {
            binding.textFirstChar.setText(user.firstName.substring(0,1));
            binding.textusername.setText(String.format("%s %s",user.firstName,user.lastName));
            binding.textEmail.setText(user.email);
            binding.imageAudioMeet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    usersListener.initiateAudioMeeting(user);
                }
            });
            binding.imagevideomeeting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    usersListener.initiateVideoMeeting(user);
                }
            });

        }
    }
}
