package orgs.tuasl_clint.client;

import orgs.tuasl_clint.models2.User;

import java.util.List;

/**
 * Listener for retrieving a list of all users.
 */
public interface OnAllUsersRetrievedListener {
    void onAllUsersRetrieved(List<User> users);
}
