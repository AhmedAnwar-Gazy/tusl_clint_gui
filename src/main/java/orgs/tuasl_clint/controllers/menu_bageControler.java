package orgs.tuasl_clint.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import orgs.tuasl_clint.client.ChatClient;
import orgs.tuasl_clint.models2.Chat;
import orgs.tuasl_clint.models2.ChatParticipant;
import orgs.tuasl_clint.protocol.Command;
import orgs.tuasl_clint.protocol.Request;
import orgs.tuasl_clint.protocol.Response;
import orgs.tuasl_clint.utils.Navigation;
import orgs.tuasl_clint.utils.TimeStampHelperClass;

import javax.swing.*;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class menu_bageControler implements Initializable {
    public TextField groupDicription;
    public TextField publicLinkTF;
    @FXML
    private TextField personNumberField;

    @FXML
    private Button backButton;

    @FXML
    private ListView<?> chatListView;


    @FXML
    private ImageView profileImage;
    @FXML
    private VBox sideMenuRoot;

    @FXML
    private Label userNameLabel;

    // إضافة حقل لواجهة إنشاء المجموعة
    @FXML
    private VBox createGroupPane;
    private String groupTypeSelected;

    @FXML
    void handleBackButtonAction(ActionEvent event) {
        Navigation.loadPage("chat.fxml");
    }

    @FXML
    void handleLogoutAction(ActionEvent event) {
        Navigation.loadPage("registration.fxml");
    }

    @FXML
    void handleMyAccountAction(ActionEvent event) {
        // اضافة وظيفة حسب الحاجة
    }
    @FXML
    private ComboBox<String> groupType;

    @FXML
    void handleNewContactAction(ActionEvent event) {
        // اضافة وظيفة حسب الحاجة
    }

    @FXML
    void handleNewGroupAction(ActionEvent event) {
        // إظهار واجهة إنشاء المجموعة وإخفاء قائمة الدردشة
        createGroupPane.setVisible(true);
        createGroupPane.setManaged(true);

        chatListView.setVisible(false);
        chatListView.setManaged(false);
    }

    @FXML
    void handleSettingsAction(ActionEvent event) {
        if(this.onGoBackButtonClickListener != null){
            this.onGoBackButtonClickListener.onGoBackButtonClickListener();
        }
    }
    @FXML
    private TextField groupNameField ;
    private Chat.ChatType chatType1;
    public interface CreateGroupListiner{
        public void onCreateGroup(Chat chat , boolean isSaved);
    }
    private CreateGroupListiner createGroupListiner;
    public void setOnCreateGroupLisiner(CreateGroupListiner createGroupListiner){
        this.createGroupListiner = createGroupListiner;
    }
    public interface OnGoBackButtonClickListener{
        public void onGoBackButtonClickListener();
    }
    private OnGoBackButtonClickListener onGoBackButtonClickListener;

    public void setOnGoBackButtonClickListener(OnGoBackButtonClickListener onGoBackButtonClickListener) {
        this.onGoBackButtonClickListener = onGoBackButtonClickListener;
    }


    public void handleCreateGroup(ActionEvent event) {
        try {
            Map<String, Object> data = new HashMap<>();
            String chatType = groupTypeSelected;
            String chatName = groupNameField.getText();
            String chatDescription = groupDicription.getText();
            String publicLink = publicLinkTF.getText();

            data.put("chat_type", chatType);
            data.put("chat_name", chatName.isEmpty() ? null : chatName);
            data.put("chat_description", chatDescription.isEmpty() ? null : chatDescription);
            data.put("public_link", publicLink.isEmpty() ? null : publicLink);
            Request request = new Request(Command.CREATE_CHAT, data);
            System.out.println("Sent Request to Server : "+request.toString());
            Response response = ChatClient.getInstance().sendRequestAndAwaitResponse(request);
            System.out.println( "----- Server Responsed By : "+ response.toString());

            Chat chat = null;
            boolean saved = false;
            if (response.isSuccess()) {
                chat = ChatClient.getInstance().getGson().fromJson(response.getData(),Chat.class);
                if(chat != null){
                    Response response1 = ChatClient.getInstance().addChatParticipant((int)chat.getId(),(int)chat.getCreatorId(),"creator");
                    chat.setUpdatedAt(TimeStampHelperClass.timeNow());
                    if(response1.isSuccess() && chat.save()){
                        System.out.println("You Are A participant in This Chat On server Now You Can Chat and Chat Is Saved Locally ");
                        chat.setPublicLink(publicLink);
                        chat.setChatDescription(chatDescription);
                        chat.setChatName(chatName);
                        ChatParticipant p = new ChatParticipant(chat.getId(),chat.getCreatorId(),chat.getCreatedAt());
                        boolean pp;
                        if(( p.save())){
                            System.out.println("Chat Created and you are the Owner now");
                            System.out.println("Chat Participant is add to this chat and State of process is : "+ true);
                        }else {
                            System.out.println("Sorry We Cannot Add You As A participant To this Chat Locally But You Can Chat ");
                        }

                    }else {
                        System.out.println("Sorry We couldnt save chat locally or Server refused add you as a participant");
                    }
                }
            }else {
                System.out.println("cannot Create the chat Server response by : "+response.getMessage());
                JOptionPane.showMessageDialog(null,response.getMessage());
            }
            if(this.createGroupListiner != null ){
                this.createGroupListiner.onCreateGroup(chat,saved);
            }
        }catch (SQLException e) {
            System.out.println("-------from the menu bage : Cannot Save th Chat !! Error: "+e.getMessage());

        }


    }
    public void setImage(Image img) {
        if (img != null) {
            // Set the image
            profileImage.setImage(img);

        }
    }
    public void handleCancelCreateGroup(ActionEvent event) {
        // إذا كنت تريد وظيفة للعودة من إنشاء مجموعة إلى القائمة
        if (createGroupPane.isVisible()) {
            createGroupPane.setVisible(false);
            createGroupPane.setManaged(false);
            chatListView.setVisible(true);
            chatListView.setManaged(true);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setImage(new Image("C:\\Users\\alraw\\OneDrive\\الصور\\67f958d43a91c9.23223583.jpg"));
        groupTypeSelected = "private";
        chatType1 = Chat.ChatType.PRIVATE;
        groupType.getItems().addAll("private","group","channel");
        groupType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                System.out.println("New selection: " + newValue);
                switch(newValue) {
                    case "group":
                        chatType1 = Chat.ChatType.GROUP;
                        break;
                    case "channel":
                        chatType1 = Chat.ChatType.CHANNEL;
                        break;
                    default:
                        chatType1 = Chat.ChatType.PRIVATE;
                }
                groupTypeSelected = newValue;
            }
        });


    }
}
