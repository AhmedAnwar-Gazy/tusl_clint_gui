package orgs.tuasl_clint.client;

import orgs.tuasl_clint.models2.User;

/**
 * Listener for successful login events.
 */
public interface OnLoginSuccessListener {
    void onLoginSuccess(User user);
}
