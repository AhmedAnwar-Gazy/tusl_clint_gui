package orgs.tuasl_clint.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import orgs.tuasl_clint.client.ChatClient;
import orgs.tuasl_clint.client.OnAllUsersRetrievedListener;
import orgs.tuasl_clint.client.OnCommandResponseListener;
import orgs.tuasl_clint.client.OnUserRetrievedListener;
import orgs.tuasl_clint.protocol.Response;
import orgs.tuasl_clint.models2.User;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AddParticipantController implements Initializable,
        OnAllUsersRetrievedListener, OnCommandResponseListener, OnUserRetrievedListener {

    @FXML private TextField searchTextField;
    @FXML private ListView<User> userListView; // Or a custom UserCellFactory for better display
    @FXML private Label feedbackLabel;
    @FXML private Button addParticipantsButton;
    @FXML private Label groupInfoLabel; // If you decide to use it

    private ChatClient chatClient;
    private ObservableList<User> allUsers = FXCollections.observableArrayList();
    private int targetChatId; // This would be passed when opening this view

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chatClient = ChatClient.getInstance();
        chatClient.addOnAllUsersRetrievedListener(this);
        chatClient.addOnCommandResponseListener(this);
        chatClient.addOnUserRetrievedListener(this); // For single user search

        userListView.setItems(allUsers);
        userListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE); // Allow selecting multiple users

        // Set a custom cell factory to display user names nicely
        userListView.setCellFactory(param -> new javafx.scene.control.ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(user.getFirstName() + " " + user.getLastName() + " (" + user.getPhoneNumber() + ")");
                }
            }
        });

        // Add listener for search input
        searchTextField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.isEmpty()) {
                // If search box is empty, show all users (or clear list)
                // For now, let's re-fetch all users or display previously fetched ones
                new Thread(() -> chatClient.getAllUsers()).start();
            } else {
                // Implement search logic:
                // You might search by phone number, name, or ID
                // For simplicity, let's assume searching by phone number for now.
                // In a real app, you'd likely have a more sophisticated server-side search.
                new Thread(() -> {
                    // Try to parse as ID first, then search by phone number/name
                    try {
                        int userId = Integer.parseInt(newText);
                        chatClient.getUserById(userId);
                    } catch (NumberFormatException e) {
                        chatClient.getUserByPhoneNumber(newText); // Assume this searches by phone or name
                    }
                }).start();
            }
        });

        // Initial fetch of all users when the view loads
        new Thread(() -> chatClient.getAllUsers()).start();

        // Example of setting targetChatId (you'd pass this from the previous scene)
        // setTargetChatId(1); // Replace with actual chat ID
    }

    // Method to set the target chat ID when the scene is loaded/displayed
    public void setTargetChatId(int chatId) {
        this.targetChatId = chatId;
        groupInfoLabel.setText("To Group: Chat ID " + chatId);
        groupInfoLabel.setVisible(true);
    }

    @FXML
    private void handleAddParticipants() {
        ObservableList<User> selectedUsers = userListView.getSelectionModel().getSelectedItems();
        if (selectedUsers.isEmpty()) {
            feedbackLabel.setText("Please select at least one user to add.");
            feedbackLabel.getStyleClass().setAll("feedback-label");
            return;
        }

        // Add each selected user to the chat
        for (User user : selectedUsers) {
            new Thread(() -> {
                // Default role can be "member" or based on UI selection
                Response response = chatClient.addChatParticipant(targetChatId,(int) user.getId(), "member");
                // The onCommandResponse listener will handle feedback
            }).start();
        }
        feedbackLabel.setText("Adding selected participants...");
        feedbackLabel.getStyleClass().setAll("success-label");
    }

    // --- ChatClientListener Implementations ---

    @Override
    public void onAllUsersRetrieved(List<User> users) {
        javafx.application.Platform.runLater(() -> {
            allUsers.clear();
            allUsers.addAll(users);
            feedbackLabel.setText(""); // Clear previous feedback
        });
    }

    @Override
    public void onUserRetrieved(User user) {
        javafx.application.Platform.runLater(() -> {
            allUsers.clear();
            if (user != null) {
                allUsers.add(user);
                feedbackLabel.setText("");
            } else {
                feedbackLabel.setText("No user found matching your search.");
                feedbackLabel.getStyleClass().setAll("feedback-label");
            }
        });
    }

    @Override
    public void onCommandResponse(Response response) {
        javafx.application.Platform.runLater(() -> {
            if (response.isSuccess()) {
                feedbackLabel.setText(response.getMessage());
                feedbackLabel.getStyleClass().setAll("success-label");
                // Optionally, refresh the user list or mark added users
            } else {
                feedbackLabel.setText("Error: " + response.getMessage());
                feedbackLabel.getStyleClass().setAll("feedback-label");
            }
        });
    }

    // Implement other necessary listeners from ChatClient if this controller needs them
    // For example, OnConnectionFailureListener, OnStatusUpdateListener etc.
}