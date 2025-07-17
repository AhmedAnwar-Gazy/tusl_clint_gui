package orgs.tuasl_clint.client;

import orgs.tuasl_clint.models2.User;

import java.util.List;

/**
 * Listener for retrieving a list of user contacts.
 */
public interface OnContactsRetrievedListener {
    void onContactsRetrieved(List<User> contacts);
}
