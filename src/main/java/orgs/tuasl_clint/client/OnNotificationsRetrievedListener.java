package orgs.tuasl_clint.client;

import orgs.tuasl_clint.models2.Notification;

import java.util.List;

/**
 * Listener for retrieving a list of user notifications.
 */
public interface OnNotificationsRetrievedListener {
    void onNotificationsRetrieved(List<Notification> notifications);
}
