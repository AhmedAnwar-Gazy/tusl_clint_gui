package orgs.tuasl_clint.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class VideoCallController implements Initializable {

    @FXML
    private StackPane rootStackPane;
    @FXML
    private ImageView remoteVideoView; // For remote participant's video feed
    @FXML
    private Text remoteUserPlaceholderText;
    @FXML
    private StackPane selfViewContainer;
    @FXML
    private ImageView selfVideoView; // For local user's video feed
    @FXML
    private Text selfUserPlaceholderText;
    @FXML
    private BorderPane overlayPane;
    @FXML
    private Text callStatusText;
    @FXML
    private Text callDurationText;
    @FXML
    private Text connectionStatusIndicator; // New: To show P2P connection status
    @FXML
    private Button micButton;
    @FXML
    private Text micIcon;
    @FXML
    private Button cameraButton;
    @FXML
    private Text cameraIcon;
    @FXML
    private Button endCallButton;

    private boolean isMicrophoneMuted = false;
    private boolean isCameraOn = true;
    private Timeline callTimer;
    private int secondsElapsed = 0;

    // Enum to simulate connection status
    public enum P2PConnectionStatus {
        CONNECTING("Connecting..."),
        CONNECTED("Connected"),
        DISCONNECTED("Disconnected"),
        FAILED("Failed");

        private final String displayName;

        P2PConnectionStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load placeholder images (replace with actual video feeds in a real app)
        loadPlaceholderImages();

        // Load FontAwesome font (if not loaded globally)
        try {
            javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/com/example/videocallui/fonts/fa-solid-900.ttf"), 10);
        } catch (Exception e) {
            System.err.println("Failed to load FontAwesome font in VideoCallController: " + e.getMessage());
        }

        // Initial state
        updateConnectionStatus(P2PConnectionStatus.CONNECTING);
        callStatusText.setText("Connecting to Remote User...");
        startCallTimer(); // Start timer, but only show "Connected" time when actually connected

        // Simulate connection after a delay
        new Thread(() -> {
            try {
                Thread.sleep(3000); // Simulate connection delay
                Platform.runLater(() -> {
                    updateConnectionStatus(P2PConnectionStatus.CONNECTED);
                    callStatusText.setText("Call with Remote User");
                    // In a real app, video streams would start appearing here
                    // setRemoteVideo(new Image("path/to/remote_live_feed.png"));
                    // setLocalVideo(new Image("path/to/local_live_feed.png"));
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void loadPlaceholderImages() {
        try {
            Image remotePlaceholder = new Image(getClass().getResourceAsStream("/com/example/videocallui/images/remote_placeholder.png"));
            remoteVideoView.setImage(remotePlaceholder);
            remoteVideoView.setVisible(true);
            remoteUserPlaceholderText.setVisible(false);
        } catch (Exception e) {
            System.err.println("Could not load remote_placeholder.png: " + e.getMessage());
            remoteVideoView.setVisible(false);
            remoteUserPlaceholderText.setVisible(true);
        }

        try {
            Image selfPlaceholder = new Image(getClass().getResourceAsStream("/com/example/videocallui/images/self_placeholder.png"));
            selfVideoView.setImage(selfPlaceholder);
            selfVideoView.setVisible(true);
            selfUserPlaceholderText.setVisible(false);
        } catch (Exception e) {
            System.err.println("Could not load self_placeholder.png: " + e.getMessage());
            selfVideoView.setVisible(false);
            selfUserPlaceholderText.setVisible(true);
        }
    }

    private void startCallTimer() {
        callTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            secondsElapsed++;
            long minutes = secondsElapsed / 60;
            long seconds = secondsElapsed % 60;
            callDurationText.setText(String.format("%02d:%02d", minutes, seconds));
        }));
        callTimer.setCycleCount(Animation.INDEFINITE);
        callTimer.play();
    }

    /**
     * Updates the displayed P2P connection status.
     * In a real WebRTC app, this would be called based on RTCPeerConnection events.
     */
    public void updateConnectionStatus(P2PConnectionStatus status) {
        Platform.runLater(() -> {
            connectionStatusIndicator.setText("P2P Status: " + status.getDisplayName());
            switch (status) {
                case CONNECTING:
                    connectionStatusIndicator.setStyle("-fx-fill: yellow;");
                    break;
                case CONNECTED:
                    connectionStatusIndicator.setStyle("-fx-fill: lightgreen;");
                    break;
                case DISCONNECTED:
                case FAILED:
                    connectionStatusIndicator.setStyle("-fx-fill: red;");
                    break;
            }
        });
    }

    /**
     * Method to simulate setting the remote video stream.
     * In a real WebRTC app, this would be invoked by the WebRTC binding/library
     * when new video frames from the remote peer are available.
     *
     * @param image The JavaFX Image representing a frame from the remote video.
     */
    public void setRemoteVideo(Image image) {
        Platform.runLater(() -> {
            remoteVideoView.setImage(image);
            remoteVideoView.setVisible(true);
            remoteUserPlaceholderText.setVisible(false);
        });
    }

    /**
     * Method to simulate setting the local video stream (from your webcam).
     * In a real WebRTC app, this would be invoked by your webcam capture library
     * when new video frames from your local camera are available.
     *
     * @param image The JavaFX Image representing a frame from your local webcam.
     */
    public void setLocalVideo(Image image) {
        Platform.runLater(() -> {
            selfVideoView.setImage(image);
            selfVideoView.setVisible(true);
            selfUserPlaceholderText.setVisible(false);
        });
    }

    @FXML
    private void toggleMicrophone() {
        isMicrophoneMuted = !isMicrophoneMuted;
        if (isMicrophoneMuted) {
            micIcon.setText("\uf131"); // FontAwesome mute icon
            micButton.getStyleClass().add("muted");
            System.out.println("Microphone Muted (Simulated)");
        } else {
            micIcon.setText("\uf130"); // FontAwesome microphone icon
            micButton.getStyleClass().remove("muted");
            System.out.println("Microphone Unmuted (Simulated)");
        }
        // In a real app: Call underlying WebRTC/audio library to mute/unmute audio track
    }

    @FXML
    private void toggleCamera() {
        isCameraOn = !isCameraOn;
        if (isCameraOn) {
            cameraIcon.setText("\uf03d"); // FontAwesome video-camera icon
            cameraButton.getStyleClass().remove("muted");
            selfVideoView.setVisible(true);
            selfUserPlaceholderText.setVisible(false);
            System.out.println("Camera On (Simulated)");
        } else {
            cameraIcon.setText("\uf03e"); // FontAwesome video-slash icon
            cameraButton.getStyleClass().add("muted");
            selfVideoView.setVisible(false);
            selfUserPlaceholderText.setVisible(true); // Show placeholder when camera is off
            System.out.println("Camera Off (Simulated)");
        }
        // In a real app: Call underlying WebRTC/video library to enable/disable video track
    }

    @FXML
    private void endCall() {
        if (callTimer != null) {
            callTimer.stop();
        }
        System.out.println("Call Ended!");
        callStatusText.setText("Call Ended");
        updateConnectionStatus(P2PConnectionStatus.DISCONNECTED);

        // In a real app:
        // 1. Send signaling message to remote peer that call is ending.
        // 2. Close RTCPeerConnection and related resources.
        // 3. Stop local webcam/microphone streams.
        // 4. Navigate back to the main application screen.

        // For this example, we'll just close the window
        Stage stage = (Stage) rootStackPane.getScene().getWindow();
        stage.close();
    }



}