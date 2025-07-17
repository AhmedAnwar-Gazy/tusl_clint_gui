package orgs.tuasl_clint.client;

/**
 * Listener for status updates (e.g., "connecting...", "file sent").
 */
public interface OnStatusUpdateListener {
    void onStatusUpdate(String status);
}
