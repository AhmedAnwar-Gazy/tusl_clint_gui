package orgs.tuasl_clint.client;

import orgs.tuasl_clint.protocol.Response;

/**
 * Listener for general command responses from the server.
 */
public interface OnCommandResponseListener {
    void onCommandResponse(Response response);
}
