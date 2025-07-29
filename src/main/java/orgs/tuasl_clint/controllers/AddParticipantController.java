package orgs.tuasl_clint.controllers;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import orgs.tuasl_clint.client.*;
import orgs.tuasl_clint.models2.ChatParticipant;
import orgs.tuasl_clint.protocol.Response;
import orgs.tuasl_clint.models2.User;
import orgs.tuasl_clint.utils.BackendThreadManager.DataModel;
import orgs.tuasl_clint.utils.BackendThreadManager.Executor;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class AddParticipantController implements Initializable,
        OnAllUsersRetrievedListener, OnCommandResponseListener, OnUserRetrievedListener , OnChatParticipantsRetrievedListener, Controller{

    @FXML public VBox mainContainer;
    @FXML public Button cancelButton;
    @FXML private TextField searchTextField;
    @FXML private ListView<ObjectProperty<User>> userListView; // Or a custom UserCellFactory for better display
    @FXML private Label feedbackLabel;
    @FXML private Button addParticipantsButton;
    @FXML private Label groupInfoLabel; // If you decide to use it

    private ChatClient chatClient = ChatClient.getInstance();

    private ObservableList<ObjectProperty<User>> allUsers = FXCollections.observableArrayList();
    private ListProperty<ObjectProperty<User>> selectedUsers = new SimpleListProperty<>();
    private SortedList<ObjectProperty<User>> sortedList = new SortedList<>(allUsers);

    private int targetChatId; // This would be passed when opening this view

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        soutt("Initializing The Add Participant View");
        userListView.itemsProperty().bind(Bindings.createObjectBinding(() -> {
            soutt("Binding Work Now .....");
            allUsers.forEach(item->soutt("Item : "+item.get().toString()));
            return sortedList.filtered(user -> {
                return (String.valueOf(user.get().getId()).toLowerCase().contains(searchTextField.getText().toLowerCase())) ||
                        ((user.get().getUsername() != null ?user.get().getUsername().toLowerCase() : "").contains(searchTextField.getText().toLowerCase())) ||
                        ((user.get().getFirstName() != null ? user.get().getFirstName().toLowerCase() :"").contains(searchTextField.getText().toLowerCase())) ||
                        ((user.get().getLastName() != null ? user.get().getLastName().toLowerCase() : "").contains(searchTextField.getText().toLowerCase()));
            }).sorted((o1,o2)->{
                if (selectedUsers.contains(o1) && !selectedUsers.contains(o2))
                    return -1;
                else {
                    return 0;
                }
            });
        },sortedList,searchTextField.textProperty()));

        userListView.setCellFactory(param -> new ListCell<ObjectProperty<User>>() {
            @Override
            protected void updateItem(ObjectProperty<User> user, boolean empty) {
                super.updateItem(user, empty);
                if(user!= null &&  user.get() == null || empty)
                    return;
                var task = DataModel.createUserCardControllerTask(user);
                soutt("Loading The Item To The View........Partiipants");
                task.setOnSucceeded(abc->{
                    var controller = task.getValue();
                    if(controller != null){
                        controller.getCheckedItem().selectedProperty().bind(Bindings.createBooleanBinding(() ->{ return selectedUsers.contains(user);},selectedUsers));
                        controller.setOnClickListener(() ->{
                            if(selectedUsers.contains(user))
                                selectedUsers.remove(user);
                            else
                                selectedUsers.add(user);
                        });
                    }else {
                        serrr("Error The UserCard Loaded For User : "+ user.get().toString()+" Is Null");
                    }
                    setGraphic(task.getValue()!= null? task.getValue().getView():null);
                });
                Executor.submit(task);
            }
        });

//        searchTextField.textProperty().addListener((observableValue, oldVal, newVal) -> {
//            userListView.getItems().clear();
//            userListView.getItems().addAll(allUsers.filtered(user -> {
//                return (String.valueOf(user.get().getId()).toLowerCase().contains(newVal.toLowerCase())) ||
//                    (user.get().getUsername().toLowerCase().contains(newVal.toLowerCase())) ||
//                    (user.get().getFirstName().toLowerCase().contains(newVal.toLowerCase())) ||
//                    (user.get().getLastName().toLowerCase().contains(newVal.toLowerCase()));
//            }).sorted(new Comparator<ObjectProperty<User>>() {
//                @Override
//                public int compare(ObjectProperty<User> o1, ObjectProperty<User> o2) {
//                    if (selectedUsers.contains(o1) && !selectedUsers.contains(o2))
//                        return -1;
//                    else {
//                        return 1;
//                    }
//                }
//            }));
//        });


        Executor.execute(()->{
            chatClient.addOnAllUsersRetrievedListener(this);
            chatClient.addOnCommandResponseListener(this);
            chatClient.addOnUserRetrievedListener(this); // For single user search
            chatClient.addOnChatParticipantsRetrievedListener(this);
            chatClient.getAllUsers();
        });
    }

    // Method to set the target chat ID when the scene is loaded/displayed
    public void setDataChatID(int chatId) {
        this.targetChatId = chatId;
        chatClient.getChatParticipants(chatId);
        groupInfoLabel.setText("To Group: Chat ID " + chatId);
        groupInfoLabel.setVisible(true);
    }

    private static void soutt(String msg){
        System.out.println("-----["+Thread.currentThread().getName()+"][AddParticipantController]  : "+ msg);
    }
    private static void serrr(String msg){
        System.err.println("-----["+Thread.currentThread().getName()+"][AddParticipantController]  : "+ msg);
    }
    @FXML
    private void handleAddParticipants() {
        if (selectedUsers.isEmpty()) {
            feedbackLabel.setText("Please select at least one user to add.");
            feedbackLabel.getStyleClass().setAll("feedback-label");
            return;
        }

        // Add each selected user to the chat
        for (var user1 : selectedUsers) {
            Executor.execute(()->{
                Response response = chatClient.addChatParticipant(targetChatId,(int) user1.get().getId(), "member");
                if(response.isSuccess()){
                    soutt("Success Adding A participant : "+user1.get().toString());
                }
                else
                    serrr("Cannot Add the Participant : "+ user1.get().toString()+"\n            Response : "+ response.toString());
            });
        }
        feedbackLabel.setText("Adding selected participants...");
        feedbackLabel.getStyleClass().setAll("success-label");
    }

    // --- ChatClientListener Implementations ---

    @Override
    public void onAllUsersRetrieved(List<User> users) {
        Platform.runLater(() -> {
            allUsers.clear();
            soutt("Adding The Participants To The Map....."+users);
            allUsers.addAll(users.stream().map(SimpleObjectProperty::new).toList());
            feedbackLabel.setText(""); // Clear previous feedback
        });
    }

    @Override
    public void onUserRetrieved(User user) {
        Platform.runLater(() -> {
            var userProperty = (new SimpleObjectProperty<>(user));
            if(!allUsers.contains(userProperty))
                allUsers.add(userProperty);
        });
    }

    @Override
    public void onCommandResponse(Response response) {
        Platform.runLater(() -> {
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

    @Override
    public StackPane getView() {
        return new StackPane(mainContainer);
    }

    @Override
    public void onChatParticipantsRetrieved(List<ChatParticipant> participants) {
        participants.forEach((u)->{
            chatClient.getChatById(((int) u.getUserId()));
        });
    }
    public interface OnCancelListener{
        public void onCancel();
    }

    OnCancelListener listener;

    public void setOnCancel(OnCancelListener listener){
        this.listener = listener;
    }

    public void handleCancelButtonClicked(ActionEvent event) {
        if(this.listener != null)
            listener.onCancel();
    }

    // Implement other necessary listeners from ChatClient if this controller needs them
    // For example, OnConnectionFailureListener, OnStatusUpdateListener etc.
}