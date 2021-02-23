package com.example.face2face.listeners;

import com.example.face2face.models.User;

public interface UsersListener {
    void initiateVideoMeeting(User user);

    void initiateAudioMeeting(User user);

    void onMultipleUsersAction(Boolean isMultipleUsersSelected);

}
