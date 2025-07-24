package orgs.tuasl_clint.controllers;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import orgs.tuasl_clint.client.*;
import orgs.tuasl_clint.models2.Chat;
import orgs.tuasl_clint.models2.ChatParticipant;
import orgs.tuasl_clint.models2.User;
import orgs.tuasl_clint.protocol.Response;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;


public class GroupInfoController implements Initializable,
        OnChatParticipantsRetrievedListener, OnCommandResponseListener,
        OnConnectionFailureListener, OnStatusUpdateListener {

    @FXML private ImageView groupPictureImageView; // New FXML element
    @FXML private Label groupNameLabel;
    @FXML private Label groupDescriptionLabel;
    @FXML private Label participantsCountLabel;
    @FXML private Label publicLinkLabel; // New FXML element
    @FXML private ListView<ChatParticipant> participantsListView;
    @FXML private Button addParticipantButton;
    @FXML private Label feedbackLabel;
    @FXML private Button editGroupInfoButton;
    @FXML private Button leaveGroupButton;
    @FXML private Button deleteGroupButton;

    private ChatClient chatClient;
    private Chat currentGroup; // The chat object for this group
    private User currentUser; // The currently logged-in user
    private ObservableList<ChatParticipant> groupParticipants = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chatClient = ChatClient.getInstance();
        // Assuming currentUser is set after login, you might need to get it from ChatClient
        // this.currentUser = chatClient.getCurrentUser(); // You'll need to implement this in ChatClient

        chatClient.addOnChatParticipantsRetrievedListener(this);
        chatClient.addOnCommandResponseListener(this);
        chatClient.addOnConnectionFailureListener(this);
        chatClient.addOnStatusUpdateListener(this);

        participantsListView.setItems(groupParticipants);
        participantsListView.setCellFactory(param -> new javafx.scene.control.ListCell<ChatParticipant>() {
            @Override
            protected void updateItem(ChatParticipant participant, boolean empty) {
                super.updateItem(participant, empty);
                if (empty || participant == null) {
                    setText(null);
                } else {
                    // In a real app, you'd fetch User details for participant.getUserId()
                    // For now, let's just display ID and role.
                    setText("User ID: " + participant.getUserId() + " (Role: " + participant.getRole() + ")");
                }
            }
        });

        // Initially disable buttons until group data is loaded and roles are checked
        setButtonsEnabled(false);
    }

    /**
     * Call this method from your main application controller to pass the Chat object
     * when navigating to this Group Info page.
     * @param group The Chat object representing the group to display.
     * @param currentUser The currently logged-in user.
     */
    public void setGroup(Chat group, User currentUser) {
        this.currentGroup = group;
        this.currentUser = currentUser; // Set the current user
        if (group != null) {
            groupNameLabel.setText(group.getChatName() != null ? group.getChatName() : "Unnamed Group");
            groupDescriptionLabel.setText(group.getChatDescription() != null ? group.getChatDescription() : "No description provided.");

            // Load group picture
            if (group.getChatPictureUrl() != null && !group.getChatPictureUrl().isEmpty()) {
                // Use a background thread for image loading to prevent UI freeze
                new Thread(() -> {
                    try {
                        Image image = new Image(group.getChatPictureUrl(), true); // true for background loading
                        Platform.runLater(() -> groupPictureImageView.setImage(image));
                    } catch (Exception e) {
                        System.err.println("Failed to load group picture: " + e.getMessage());
                        // Optionally set a default image or show error
                        Platform.runLater(() -> groupPictureImageView.setImage(new Image(getClass().getResourceAsStream("/images/default_group_icon.png"))));
                    }
                }).start();
            } else {
                // Set a default image if no URL is provided
                groupPictureImageView.setImage(new Image(getClass().getResourceAsStream("/images/default_group_icon.png")));
            }

            // Display public link
            if (group.getPublicLink() != null && !group.getPublicLink().isEmpty()) {
                publicLinkLabel.setText(group.getPublicLink());
                publicLinkLabel.setVisible(true);
            } else {
                publicLinkLabel.setText("Not Available");
                publicLinkLabel.setVisible(false); // Hide if no public link
            }


            // Fetch participants
            new Thread(() -> chatClient.getChatParticipants((int) group.getId())).start();
        }
    }

    private void setButtonsEnabled(boolean enable) {
        addParticipantButton.setDisable(!enable);
        editGroupInfoButton.setDisable(!enable);
        leaveGroupButton.setDisable(!enable);
        deleteGroupButton.setDisable(!enable); // Will be further restricted by role
    }

    private void updateButtonVisibilityBasedOnRole() {
        if (currentGroup == null || currentUser == null) {
            setButtonsEnabled(false);
            return;
        }

        // Find the current user's role in this group
        Optional<ChatParticipant> userParticipant = groupParticipants.stream()
                .filter(p -> p.getUserId() == currentUser.getId())
                .findFirst();

        boolean isAdmin = userParticipant.map(p -> "admin".equalsIgnoreCase(String.valueOf(p.getRole())) || "creator".equalsIgnoreCase(String.valueOf(p.getRole()))).orElse(false);
        boolean isCreator = userParticipant.map(p -> "creator".equalsIgnoreCase(String.valueOf(p.getRole()))).orElse(false);
        boolean isMember = userParticipant.isPresent();

        // Enable/disable based on general membership
        setButtonsEnabled(isMember);

        // Specific role-based permissions
        addParticipantButton.setDisable(!isAdmin); // Only admins/creators can add
        editGroupInfoButton.setDisable(!isAdmin); // Only admins/creators can edit
        deleteGroupButton.setDisable(!isCreator); // Only creator can delete

        // A user can always leave the group if they are a member
        leaveGroupButton.setDisable(!isMember);

        if (!isMember) {
            feedbackLabel.setText("You are not a member of this group.");
            feedbackLabel.getStyleClass().setAll("feedback-label");
        } else {
            feedbackLabel.setText(""); // Clear feedback if they are a member
        }
    }


    @FXML
    private void handleAddParticipant() {
        // Example: Navigate to the AddParticipant FXML
        // This would typically involve loading a new FXML scene
        // For simplicity, just show a status update here.
        Platform.runLater(() -> {
            // Your navigation logic here, e.g.:
            // FXMLLoader loader = new FXMLLoader(getClass().getResource("/your/package/name/AddParticipant.fxml"));
            // Parent root = loader.load();
            // AddParticipantController controller = loader.getController();
            // controller.setTargetChatId(currentGroup.getId()); // Pass the current group ID
            // Stage stage = (Stage) addParticipantButton.getScene().getWindow();
            // stage.setScene(new Scene(root));
            // stage.setTitle("Add Participants");

            feedbackLabel.setText("Navigating to Add Participant screen for Chat ID: " + currentGroup.getId());
            feedbackLabel.getStyleClass().setAll("success-label");
        });
    }

    @FXML
    private void handleEditGroupInfo() {
        Platform.runLater(() -> {
            feedbackLabel.setText("Navigating to Edit Group Info screen for Chat ID: " + currentGroup.getId());
            feedbackLabel.getStyleClass().setAll("success-label");
            // Implement navigation to an "Edit Group" FXML/controller
            // You would pass the 'currentGroup' object to the edit controller
        });
    }

    @FXML
    private void handleLeaveGroup() {
        if (currentGroup == null || currentUser == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Leave Group");
        alert.setHeaderText("Are you sure you want to leave '" + groupNameLabel.getText() + "'?");
        alert.setContentText("You will no longer receive messages from this group.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                Response response = chatClient.removeChatParticipant((int) currentGroup.getId(), (int) currentUser.getId());
                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        feedbackLabel.setText("Successfully left the group.");
                        feedbackLabel.getStyleClass().setAll("success-label");
                        // Optionally, navigate back to main chat list or show a confirmation
                        // (e.g., stage.close() or load main chat view)
                    } else {
                        feedbackLabel.setText("Failed to leave group: " + response.getMessage());
                        feedbackLabel.getStyleClass().setAll("feedback-label");
                    }
                });
            }).start();
        }
    }

    @FXML
    private void handleDeleteGroup() {
        if (currentGroup == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete Group");
        alert.setHeaderText("WARNING: This will permanently delete '" + groupNameLabel.getText() + "' for everyone.");
        alert.setContentText("Are you absolutely sure you want to delete this group?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                Response response = chatClient.deleteChat((int) currentGroup.getId());
                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        feedbackLabel.setText("Group deleted successfully.");
                        feedbackLabel.getStyleClass().setAll("success-label");
                        // Navigate back to main chat list or close window
                    } else {
                        feedbackLabel.setText("Failed to delete group: " + response.getMessage());
                        feedbackLabel.getStyleClass().setAll("feedback-label");
                    }
                });
            }).start();
        }
    }

    @FXML
    private void handlePublicLinkClick() {
        String link = publicLinkLabel.getText();
        if (link != null && !link.isEmpty() && !"Not Available".equals(link)) {
            try {
                // Check if Desktop is supported before trying to open
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(link));
                    chatClient.notifyStatusUpdate("Opened public link: " + link);
                } else {
                    feedbackLabel.setText("Cannot open link: Desktop browsing not supported.");
                    feedbackLabel.getStyleClass().setAll("feedback-label");
                }
            } catch (IOException | java.net.URISyntaxException e) {
                feedbackLabel.setText("Error opening link: " + e.getMessage());
                feedbackLabel.getStyleClass().setAll("feedback-label");
                System.err.println("Error opening public link: " + e.getMessage());
            }
        }
    }

    // --- ChatClientListener Implementations ---

    @Override
    public void onChatParticipantsRetrieved(List<ChatParticipant> participants) {
        // Ensure this update is for the current group
        if (currentGroup != null) {
            Platform.runLater(() -> {
                groupParticipants.clear();
                groupParticipants.addAll(participants);
                participantsCountLabel.setText(participants.size() + " Participants");
                updateButtonVisibilityBasedOnRole(); // Update button states after participants are loaded
            });
        }
    }

    @Override
    public void onCommandResponse(Response response) {
        Platform.runLater(() -> {
            if (response.isSuccess()) {
                feedbackLabel.setText(response.getMessage());
                feedbackLabel.getStyleClass().setAll("success-label");
                // If a participant was added/removed, refresh the list
                if (response.getMessage().contains("Participant added") || response.getMessage().contains("Participant removed")) {
                    if (currentGroup != null) {
                        new Thread(() -> chatClient.getChatParticipants((int) currentGroup.getId())).start();
                    }
                }
            } else {
                feedbackLabel.setText("Error: " + response.getMessage());
                feedbackLabel.getStyleClass().setAll("feedback-label");
            }
        });
    }

    @Override
    public void onConnectionFailure(String errorMessage) {
        Platform.runLater(() -> {
            feedbackLabel.setText("Connection Error: " + errorMessage);
            feedbackLabel.getStyleClass().setAll("feedback-label");
        });
    }

    @Override
    public void onStatusUpdate(String status) {
        Platform.runLater(() -> {
            // You might use a separate status bar for general updates
            // For now, just print to console or use feedbackLabel if appropriate
            System.out.println("[Status]: " + status);
        });
    }


    // Implement other listeners if needed (e.g., OnNewMessageListener if this page should show incoming messages)
}