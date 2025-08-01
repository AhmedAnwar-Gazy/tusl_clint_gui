package orgs.tuasl_clint.client;

/**
 * Listener for errors related to network connection failures.
 */
public interface OnConnectionFailureListener {
    void onConnectionFailure(String errorMessage);
}
