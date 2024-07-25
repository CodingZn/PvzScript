package src.interf;

import src.api.User;

public interface UserChangeCallback {
    void onCallback(User newUser);
}
