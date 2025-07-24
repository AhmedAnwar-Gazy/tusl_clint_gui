package orgs.tuasl_clint.controllers;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class IncomingCallController implements Initializable {

    @FXML
    private ImageView callerAvatarView;
    @FXML
    private Text callerInitialsText;
    @FXML
    private Text callerNameText;
    @FXML
    private Button declineButton;
    @FXML
    private Button acceptButton;
    @FXML
    private StackPane rootPane;

    private String callerName;
    private CallActionListener callActionListener; // Callback interface

    // Interface for callback to the main application
    public interface CallActionListener {
        void onCallAccepted(String callerName);
        void onCallDeclined(String callerName);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initially hide image view and show initials
        callerAvatarView.setVisible(false);
        callerInitialsText.setVisible(true);

        // Optional: Load FontAwesome font for icons (if not loaded via CSS or Main App)
        try {
            javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/com/example/videocallui/fonts/fa-solid-900.ttf"), 10);
        } catch (Exception e) {
            System.err.println("Failed to load FontAwesome font in IncomingCallController: " + e.getMessage());
        }
    }

    // Method to set caller information and listener
    public void setCallInfo(String name, String avatarPath, CallActionListener listener) {
        this.callerName = name;
        this.callActionListener = listener;
        callerNameText.setText(name);
        callerInitialsText.setText(String.valueOf(name.charAt(0)).toUpperCase());

        if (avatarPath != null && !avatarPath.isEmpty()) {
            try {
                Image avatar = new Image(getClass().getResourceAsStream(avatarPath));
                if (!avatar.isError()) {
                    callerAvatarView.setImage(avatar);
                    callerAvatarView.setVisible(true);
                    callerInitialsText.setVisible(false);
                } else {
                    System.err.println("Error loading avatar image: " + avatarPath);
                    callerAvatarView.setVisible(false);
                    callerInitialsText.setVisible(true);
                }
            } catch (Exception e) {
                System.err.println("Could not load avatar: " + avatarPath + " - " + e.getMessage());
                callerAvatarView.setVisible(false);
                callerInitialsText.setVisible(true);
            }
        } else {
            callerAvatarView.setVisible(false);
            callerInitialsText.setVisible(true);
        }
    }

    @FXML
    private void handleAccept() {
        if (callActionListener != null) {
            callActionListener.onCallAccepted(callerName);
        }
        closeWindow();
    }

    @FXML
    private void handleDecline() {
        if (callActionListener != null) {
            callActionListener.onCallDeclined(callerName);
        }
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }
}