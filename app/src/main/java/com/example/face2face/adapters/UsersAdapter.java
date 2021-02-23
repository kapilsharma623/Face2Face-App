package com.example.face2face.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.face2face.R;
import com.example.face2face.databinding.ItemContainerUserBinding;
import com.example.face2face.listeners.UsersListener;
import com.example.face2face.models.User;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder>{
    private List<User> users;
    private UsersListener usersListener;
    private List<User> selectedUsers;

    public UsersAdapter(List<User> users,UsersListener usersListener) {
        this.users = users;
        this.usersListener=usersListener;
        selectedUsers=new ArrayList<>();
    }

    public List<User> getSelectedUsers() {
        return selectedUsers;
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
            binding.userContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (binding.imageselected.getVisibility()!=View.VISIBLE)
                    {
                        selectedUsers.add(user);
                        binding.imageselected.setVisibility(View.VISIBLE);
                        binding.imagevideomeeting.setVisibility(View.GONE);
                        binding.imageAudioMeet.setVisibility(View.GONE);
                        usersListener.onMultipleUsersAction(true);
                    }

                    return true;
                }
            });
            binding.userContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (binding.imageselected.getVisibility() ==View.VISIBLE)
                    {
                        selectedUsers.remove(user);
                        binding.imageselected.setVisibility(View.GONE);
                        binding.imagevideomeeting.setVisibility(View.VISIBLE);
                        binding.imageAudioMeet.setVisibility(View.VISIBLE);
                        if (selectedUsers.size() == 0)
                        {
                            usersListener.onMultipleUsersAction(false);

                        }
                        else {
                            if (selectedUsers.size()>0)
                            {
                                selectedUsers.add(user);
                                binding.imageselected.setVisibility(View.VISIBLE);
                                binding.imageAudioMeet.setVisibility(View.GONE);
                                binding.imagevideomeeting.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            });

        }
    }
}
