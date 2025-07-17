package orgs.tuasl_clint.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.bytedeco.opencv.opencv_core.AsyncArray;
import orgs.tuasl_clint.client.ChatClient;
import orgs.tuasl_clint.client.OnAllUsersRetrievedListener;
import orgs.tuasl_clint.client.OnFileTransferListener;
import orgs.tuasl_clint.livecall.AudioCallWindow;
import orgs.tuasl_clint.livecall.AudioReceiverUDP;
import orgs.tuasl_clint.livecall.AudioSendUDP;
import orgs.tuasl_clint.livecall.VideoCallWindowUDP;
import orgs.tuasl_clint.models2.*;
import orgs.tuasl_clint.models2.FactoriesSQLite.*;
import orgs.tuasl_clint.protocol.Response;
import orgs.tuasl_clint.utils.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.nio.file.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.sound.sampled.*;
import javax.swing.*;

import javafx.scene.input.MouseEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;

import static java.lang.System.exit;

public class ChatController {
    public VBox chatsMainContainer;
    public HBox menuItemContainer;
    public VBox leftMainAllContainer;
    public TextField searchTF;
    @FXML
    private BorderPane rootBorderPane;
    @FXML
    private ListView<Chat> chatListView;
    @FXML
    private Label chatTitleLabel;
    @FXML
    private ScrollPane messageScrollPane;
    @FXML
    private VBox messageDisplayArea;
    @FXML
    private TextField messageInputField;
    @FXML
    private Button settingsButton; // Added FXML annotation
    @FXML
    private Button sendButton; // Added FXML annotation
    @FXML
    private Button sendVoiceButton; //Added FXML annotation
    @FXML
    private Button emojiButton; //Added FXML annotation
    @FXML
    private Button shareButton; //Added FXML annotation
    @FXML
    private FlowPane areaOfEmojis; //Added FXML annotation
    @FXML
    private Button audioCallButton;
    @FXML
    private Button menuButton;
    @FXML
    private Button videoCallButton;
    @FXML
    private Button cancel_message_media;
    @FXML
    private VBox main_message_input_container;
    @FXML
    private HBox message_media_selected_container;
    @FXML
    private HBox chatListItem;
    @FXML
    private ScrollPane emojiScrollPane;


    private static Stage mediaStageShower;


    private volatile boolean isRecording = false;
    private TargetDataLine line;    // Define the folder where recordings should be saved
    private final String RECORDING_FOLDER = "src/main/resources/orgs/tuasl_clint/voiceNote/";
    private volatile ObservableList<Message> messageItemsMessage;// = FXCollections.observableArrayList();


    private File audioFile;
    private FileItemController mediaFileController;
    private Chat currentChat;

    private volatile HashMap<Long,User> userMap;
    private volatile HashMap<Long,Chat> chatMap;
    private volatile  HashMap<Chat ,ObservableList<Message>> chatsMessagesMap;
    private volatile HashMap<Chat,ObservableList<ChatParticipant>> chatsParticipantsMap;
    private volatile HashMap<Chat,ChatListItemController> chatChatListItemControllerHashMap;




    public synchronized  void addChatItem(Chat chat) {
        if (chat != null && chat.getId() > 0 && !chatMap.containsKey(chat.getId())) {
            chatListView.getItems().add(chat);
            this.chatMap.put(chat.getId(),chat);
            chatsMessagesMap.put(chat,FXCollections.observableArrayList());
            chatsParticipantsMap.put(chat,FXCollections.observableArrayList());
            chat.saveOrUpdate();
        }
    }
    public void addnewMessage(Message message){
        if(chatMap.containsKey(message.getChatId())){
            chatsMessagesMap.get(chatMap.get(message.getChatId())).add(message);
            if(currentChat == chatMap.get(message.getChatId())){
                loadMessages(message);
            }
        }
    }

    private void addChatParticipant(ChatParticipant cp, Chat chat) {
        if(this.chatsParticipantsMap.containsKey(chat))
            this.chatsParticipantsMap.get(chat).add(cp);
    }

    @FXML
    public void initialize() {
        if(User.user == null){
            JOptionPane.showMessageDialog(null,"You Should Sign in First");
            exit(1);
        }
        /*
        *  initialize variables
        *  create method that returns observable fx collections for messages
        *  get data from db
        *  get data from server
        *  save data from server
        *  update the show
         */
        userMap = new HashMap<>();
        chatMap = new HashMap<>();
        chatsMessagesMap = new HashMap<>();
        chatsParticipantsMap = new HashMap<>();
        chatChatListItemControllerHashMap = new HashMap<>();

        initChatListView();

        getUsersFromDB();// finish
        getChatsFromDB();// finish
        getParticipantsFromDB();// finish

        new Runnable() {
            @Override
            public void run() {
                ChatClient.getInstance().getAllUsers();
                ChatClient.getInstance().getUserChats();
            }
        }.run();

        ChatClient.getInstance().addOnAllUsersRetrievedListener( users -> {
            new Runnable() {
                @Override
                public void run() {
                    users.forEach(user ->{
                        if(!userMap.containsKey(user.getId())){
                            userMap.put(user.getId(),user);
                            user.saveOrUpdate();
                        }
                    });
                }
            }.run();
        });
        ChatClient.getInstance().addOnUserChatsRetrievedListener(chats -> {
            new Runnable(){
                @Override
                public void run() {
                    chats.forEach(chat -> {
                        if (!chatMap.containsKey(chat)) {
                            addChatItem(chat);
                            ChatClient.getInstance().getChatMessages((int)chat.getId(),50,0);
                            ChatClient.getInstance().getChatParticipants((int)chat.getId());
                        }
                    });
                }
            }.run();
        });

        // TODO check adding the participants from the the server and Messages

    }



    private void getParticipantsFromDB() {
        ChatParticipantFactory.getAll().forEach(p -> {
            Long chat_id = p.getChatId();
            if(chatMap.containsKey(chat_id))
                addChatParticipant(p , chatMap.get(p.getChatId()));
        });
    }

    private void getChatsFromDB() {
        try (Connection con = DatabaseConnectionSQLite.getInstance().getConnection()){
            String sql = "SELECT c.* FROM users LEFT JOIN chat_participants on users.user_id = chat_participants.user_id LEFT JOIN chats c on chat_participants.chat_id = c.chat_id WHERE users.user_id = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setLong(1,User.user.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Chat chat = orgs.tuasl_clint.models2.FactoriesSQLite.ChatFactory.createFromResultSet(rs);
                if(chat != null){
                    addChatItem(chat);
                    getMessagesFromDBToChat(chat);
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("----- An error occurred during database operations: " + e.getMessage());
        }
    }

    private void getMessagesFromDBToChat(Chat chat) {
        try {
            MessageFactory.findByChatId(chat.getId()).forEach(message -> {
                if(chatsMessagesMap.containsKey(chat)){
                    chatsMessagesMap.get(chat).add(message);
                }
            });
        } catch (SQLException e) {
            System.err.println("----- Cannot Get Chats Messages");
        }
    }

    private void getUsersFromDB() {
        UserFactory.getAll().forEach(user -> this.userMap.put(user.getId(),user));
    }

    private void initChatListView() {
        chatListView.setCellFactory(listView -> new ListCell<Chat>() {
            @Override
            protected void updateItem(Chat chat, boolean empty) {
                super.updateItem(chat, empty);

                if (empty || chat == null || chat.getId() <= 0) {
                    return;
                } else {
                    try {
                        // Load the HBox for each item
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/orgs/tuasl_clint/fxml/chatListItem.fxml"));
                        HBox hbox = loader.load();
                        ChatListItemController controller = loader.getController();
                        controller.setChat(chat);
                        setGraphic(hbox);
                    } catch (IOException e) {
                        e.printStackTrace();
                        setGraphic(new Label("Error loading chat item"));
                    }
                }
            }
        });
        chatListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            System.out.println("selected chat is changed");
            if (newSelection != null) {
//                if(chatsParticipantsMap.containsKey(newSelection)){
//                    loadChatParticipantFromLocalDB(newSelection);
//                }
                messageDisplayArea.getChildren().clear();
                messageDisplayArea.getChildren().add(new Label("Messages for " + newSelection.getChatName() + " are encrypted between all participles."));
                currentChat = newSelection;
                try {
                    int count = ChatParticipantFactory.getChatParticipantCount(currentChat.getId());
                    System.out.println("--------------------------------------------Participants count is : "+ count);
                    if(count > 2){
                        this.audioCallButton.setVisible(false);
                        this.videoCallButton.setVisible(false);
                    }else{
                        this.audioCallButton.setVisible(true);
                        this.videoCallButton.setVisible(true);
                    }
                } catch (SQLException e) {
                    System.err.println("-------cannot get this chat participles...!!!\n------Error Message is : "+ e.getMessage());
                }
//                loadChatsMessages(newSelection);                // --- NEW CODE: Populate messageDisplayArea directly after loading messages ---
                for (Message message : messageItemsMessage) {
                    loadMessages(message);
                }
                System.out.println("Selected chat: " + newSelection);
            }
        });
    }

//    private void LoadChatsFromServer() {
//        getAllUsersFromServer();
//        List<Chat> chats =  getMyChatsFromServer();
//        chats.forEach(chat -> {
//            getChatParticipantsFromServer(chat);
//            loadMessagesFromServerForChat(chat,50,0);
//        });
//    }
//
//    private List<Message> loadMessagesFromServerForChat(Chat chat, int limit, int offset){
//        // Get Chat Messages ------------------------------------
//            HashMap<String, Object> data = new HashMap();
//            data.put("chat_id", chat.getId());
//            data.put("limit", limit);
//            data.put("offset", offset);
//            Request request = new Request(Command.GET_CHAT_MESSAGES, data);
//            Response messagesResponse = Clint_Methods.getInstance().sendRequestAndAwaitResponse(request);
//            if (messagesResponse != null && messagesResponse.isSuccess() && "Messages retrieved.".equals(messagesResponse.getMessage())) {
//                Type messageListType = new TypeToken<List<Message>>() {
//                }.getType();
//                List<Message> messages = Clint_Methods.getInstance().getGson().fromJson(messagesResponse.getData(), messageListType);
//                System.out.println("\n--- Messages in Chat ID: " + chat.getId() + " ---");
//                if (messages == null || messages.isEmpty()) {
//                    System.out.println("No messages found in this chat.");
//                } else {
//                    for (Message msg : messages) {
//                        System.out.println("----- Recived Message : " + msg.toString());
//                        try {
//                            if (msg.save()) {
//                                if(msg.getMedia() != null){
//                                    System.out.println("----- Feching A message with a media : "+ msg.getMedia());
//                                    Media m = msg.getMedia();
//                                }
//                                System.out.println("Message : " + msg.toString() + " Was Saved Locally...!!!");
//                                loadMessages(msg);
//                            }
//                        } catch (SQLException e) {
//                            System.err.println("----- An Error Occored While Trying To Save The Message : " + msg.toString() + "  Error msg is : " + e.getMessage());
//                            e.printStackTrace();
//                        }
//                    }
//                }
//                return messages;
//            }
//        return null;
//    }
//
//    private static void getChatParticipantsFromServer(Chat chat){
//        // Get Chat Participants---------------------------------
//            Map<String, Object> params = new HashMap<>();
//            params.put("chat_id", chat.getId());
//            Request request = new Request(Command.GET_CHAT_PARTICIPANTS, params);
//            Response response = Clint_Methods.getInstance().sendRequestAndAwaitResponse(request);
//
//            if (response != null && response.isSuccess()) {
//                Type participantListType = new TypeToken<List<ChatParticipant>>() {
//                }.getType();
//                List<ChatParticipant> participants = Clint_Methods.getInstance().getGson().fromJson(response.getData(), participantListType);
//                System.out.println("\n----- Participants in Chat ID: " + chat.getId() + " and Name :" + chat.getChatName() + " ---");
//                if (participants == null || participants.isEmpty()) {
//                    System.out.println("----- No participants found in this chat or you don't have permission to view them.");
//                } else {
//                    for (ChatParticipant p : participants) {
//                        System.out.println("----- User ID: " + p.getUserId() + ", Role: " + p.getRole() + ", Joined: " + TimeStampHelperClass.formatTimeLeft(p.getJoinedAt()));
//                        try {
//                            if (p.save()) {
//                                System.out.println("----- Participant Was Successfully in Local DataBase");
//                            } else {
//                                System.out.println("----- UNKNOWN ERROR : While trying to  Save participant");
//                            }
//                        } catch (SQLException e) {
//                            System.err.println("----- Error Saving participant Error Message is : " + e.getMessage() + " Participant is : " + p.toString());
//    //                                    e.printStackTrace();
//                        }
//                    }
//                }
//            } else if (response != null) {
//                System.out.println("Failed to get chat participants: " + response.getMessage());
//            }
//    }
//
//    private List<Chat> getMyChatsFromServer(){
//            Request request = new Request(Command.GET_USER_CHATS);
//            Response response = Clint_Methods.getInstance().sendRequestAndAwaitResponse(request);
//
//            Type chatListType = new TypeToken<List<Chat>>() {
//            }.getType();
//            List<Chat> chats = Clint_Methods.getInstance().getGson().fromJson(response.getData(), chatListType);
//            // View Chats ON the Frame or the ListView
//            for (Chat chat : chats) {
//                System.out.println("----- Recived Chat : " + chat.toString());
//                addChatItem(chat);
//                try {
//                    if (!chatItems.contains(chat) && chat.save()) {
//                        System.out.println("----- This Chat Was Saved Successfully ------ ");
//                        System.out.println("----- Gettint Chats Messages and Participans for Chat id : " + chat.getId());
//                    }
//                } catch (SQLException e) {
//                    System.err.println("----- An Error Wile Trying To Save This Chat That Its Id is : " + chat.getId() + "   Error is : " + e.getMessage());
//                    e.printStackTrace();
//                }
//            }
//            return chats;
//    }
//
//    private static void getAllUsersFromServer(){
//        // Get All Users :------------------------------------------
//            Request request = new Request(Command.GET_ALL_USERS);
//            Response response = Clint_Methods.getInstance().sendRequestAndAwaitResponse(request);
//            if (response != null && response.isSuccess() && "All users retrieved.".equals(response.getMessage())) {
//                Type userListType = new TypeToken<List<User>>() {
//                }.getType();
//                List<User> users = Clint_Methods.getInstance().getGson().fromJson(response.getData(), userListType);
//                if (!(users == null || users.isEmpty())) {
//                    for (User user : users) {
//                        System.out.println("----- Feching The User : " + user.toString());
//                        try {
//                            user.save();
//                            System.out.println("----- User Saved Successfully ....!!");
//                        } catch (SQLException e) {
//                            System.err.println("----- Cannot Save The User :" + user.toString() + "Error MSG : " + e.getMessage());
//    //                            e.printStackTrace();
//                        }
//                    }
//                } else {
//                    System.err.println("----- Failed to get all users: " + response.getMessage());
//                }
//            }
//    }

    @FXML
    private void handleSettingsButtonAction(ActionEvent event) {
        Navigation.loadPage("settings.fxml");
    }

    @FXML
    private void handleSendButtonAction(ActionEvent event) {
        String messageText = messageInputField.getText().trim();
        if(!this.message_media_selected_container.getChildren().isEmpty()){
            this.message_media_selected_container.getChildren().clear();
            Media m = new Media(mediaFileController.getFile().getName(), mediaFileController.getFile().getAbsolutePath(),FilesHelper.getFileExtension(mediaFileController.getFile()),FilesHelper.getFileSize(mediaFileController.getFile()),new Timestamp(new Date().getTime()));
            m.setUploadedByUserId(User.user.getId());
            try {
                if(m.save()){
                    Message mm = new Message(messageText);
                    mm.setMediaId(m.getId());
                    mm.setSenderName(User.user.getFirstName());
                    mm.setSenderId(User.user.getId());
                    mm.setChatId(currentChat.getId());
                    mm.setMessageType(FilesHelper.getFileType(mediaFileController.getFile()).name().toLowerCase());
                    if(mm.save()){
                        mediaFileController.action.OnActionCleared();
                        messageInputField.clear();
                        this.mediaFileController.clear();
                        System.out.println("----- Now The File Is On Sending ....");
                        ((Runnable) () -> {
                            Response response = ChatClient.getInstance().sendMediaMessage((int) currentChat.getId(), m.getFilePathOrUrl(), mm.getContent(), m.getMediaType(), new OnFileTransferListener() {
                                @Override
                                public void onFail(String msg) {
                                    System.err.println("----- Fail To send The Media Message , Error MSG: " + msg);
                                }

                                @Override
                                public void onProgress(long transferredBytes, long totalSize) {
                                    System.out.println("Media Message Is Sent Now and Status Is : "+transferredBytes+" b/ "+totalSize+" b   ");
                                }

                                @Override
                                public void onComplete(File file) {
                                    System.out.println("---- Message Sent SuccessFully------");
                                    messageItemsMessage.add(mm);
                                    loadMessages(mm);
                                }
                            });
                        }).run();
                        return;
                    }
                    else {
                        System.out.println("cannot save the message");
                    }
                }
                else {
                    System.out.println("cannot save the media file");
                }
            } catch (SQLException e) {
                System.out.println("an error occurred while trying to save the message or its media error: "+ e.getMessage());
            }
        }
        // Message  : Long messageId, Long chatId, Long senderUserId, String messageType, String content, Long mediaId, Long repliedToMessageId, Long forwardedFromUserId, Long forwardedFromChatId, Timestamp sentAt, Timestamp editedAt, Boolean isDeleted, Integer viewCount
        if (!messageText.isEmpty()) {
            System.out.println("Sending message: " + messageText);
            Message m = new Message(messageText);
            m.setMediaId(m.getMediaId());
            m.setSenderName(User.user.getFirstName());
            m.setSenderId(User.user.getId());
            m.setChatId(currentChat.getId());
            m.setMessageType(FilesHelper.fileType.TEXT.name().toLowerCase());
            try {
                if(m.save()){
                    System.out.println("@@@@@@=-------------sending text message");
                    Response res = ChatClient.getInstance().sendTextMessage((int)currentChat.getId(),m.getContent());
                    if(res.isSuccess()){
                        messageInputField.clear();
                        loadMessages(m);
                    }else {
                        JOptionPane.showMessageDialog(null,"Cannot send the  Message Error : "+res.getMessage());
                    }
                }
                else{
                    System.out.println("cannot save the message");
                }
            } catch (SQLException e) {
                System.out.println("an error occurred while trying to save the message error: "+ e.getMessage());
            }
        }
    }

    @FXML
    private void loadMessages(Message messageText) {
        if(messageText == null || (messageText.getMediaId() != null && messageText.getContent() == null) || messageText.getId() == 0)
            return;
        System.out.println("----- LoadMessages Methos is Work to add Tht Message : "+messageText.toString()+" to The View");
        try {
            // Create an FXMLLoader instance
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/orgs/tuasl_clint/fxml/sendMessageItem.fxml"));
            Parent userCardNode = loader.load();
            SendMessageItemController sendMessageItemController = loader.getController();
            sendMessageItemController.setUserData(messageText);
            messageDisplayArea.getChildren().add(userCardNode);
            if(!chatsMessagesMap.containsKey(currentChat)){
                chatsMessagesMap.put(currentChat,FXCollections.observableArrayList());
                chatsMessagesMap.get(currentChat).add(messageText);
            }
            if(chatsMessagesMap.containsKey(currentChat) && !chatsMessagesMap.get(currentChat).contains(messageText)){
                chatsMessagesMap.get(currentChat).add(messageText);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load UserCard.fxml: " + e.getMessage());
        }
    }
    @FXML
    private void handleSendVoiceButtonPressed(MouseEvent event){
        System.out.println("Send Voice button pressed...");
        animateButton(true); // Start animation
        startRecording(); // Begin recording
    }
    @FXML
    private void handleSendVoiceButtonReleased(MouseEvent event){
        System.out.println("Send Voice button Released");
        stopRecording(); // Stop recording when released
        animateButton(false); // Reset animation
    }

    private void animateButton(boolean isClicked) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), sendVoiceButton);
        if (isClicked) {
            scaleTransition.setToX(1.2);
            scaleTransition.setToY(1.2);
        } else {
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
        }
        scaleTransition.play();
    }

    private void startRecording() {
        try {
            // Ensure the directory exists
            File folder = new File(RECORDING_FOLDER);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            audioFile = new File(RECORDING_FOLDER, "recording_" + timeStamp + ".wav");
            AudioFormat format = new AudioFormat(16000, 8, 2, true, true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("Line not supported");
                return;
            }
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            isRecording = true;
            System.out.println("Recording started: " + audioFile.getAbsolutePath());
            Thread recordingThread = new Thread(() -> {
                try (AudioInputStream audioStream = new AudioInputStream(line)) {
                    AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, audioFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
            recordingThread.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    public void setMediaFile(File file, FileItemController.Action action) {
        if(!this.message_media_selected_container.getChildren().isEmpty() && this.mediaFileController != null && this.mediaFileController.getState() != FileItemController.State.DELETED){
            mediaFileController.setFile(file,action);
        }
        else if(!this.message_media_selected_container.getChildren().isEmpty() && this.mediaFileController == null){
            this.message_media_selected_container.getChildren().clear();
            setMediaFile(file,action);
        }
        else if(this.message_media_selected_container.getChildren().isEmpty()){
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/orgs/tuasl_clint/fxml/fileItem.fxml"));
                Parent pp = loader.load();
                this.mediaFileController = loader.getController();
                this.mediaFileController.setFile(file,action);
                this.message_media_selected_container.getChildren().addFirst(pp);
            } catch (IOException e) {
                System.out.println("Cannot load the file Error is : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    public void setMessageText(String text){
        this.messageInputField.setText(text);
    }
    public void SetMessage(Message message){
        this.messageInputField.setText(message.getContent());
        if(!message.getMessageType().equals("text")){
            Media m = null;
            try {
                m = MediaFactory.findById(message.getMediaId());
            } catch (SQLException e) {
                System.out.println("Cannot load this message");
                e.printStackTrace();
            }
            if (m != null) {
                File f = new File(m.getFilePathOrUrl());
                this.setMediaFile(f,null);
            }
        }
    }
    public void SetMessage(Message message,Media m){
        this.messageInputField.setText(message.getContent());
        File f = new File(m.getFilePathOrUrl());
        message.setMessageType(FilesHelper.getFileType(f).name().toLowerCase());
        setMediaFile(f,null);
    }
    public void SetMessage(Message message, File f){
        this.messageInputField.setText(message.getContent());
        message.setMessageType(FilesHelper.getFileType(f).name().toLowerCase());
        setMediaFile(f,null);
    }
    public void  reciveMessage(Message message){
        Chat c = null;
        try {
            c = ChatFactory.findById(message.getChatId());
        } catch (SQLException e) {
            System.out.println("Cannot recive this message becouse no Chat in database has this message chat");
            e.printStackTrace();
            return;
        }

        if(c != null){
            if(c == currentChat){
                loadMessages(message);
                //TODO play the message received sound
            }else if(chatsMessagesMap.containsKey(c)) {
                chatsMessagesMap.get(c).add(message);
                //TODO: play the notifications sound
            }else{
//                chatsMap.put(c.getChatName(),c);
                chatsMessagesMap.put(c,FXCollections.observableArrayList());
                chatsMessagesMap.get(c).add(message);
                this.addChatItem(c);
                //TODO: add the unread messages count to this item and play the notifications sound
            }
        }
    }
    private void stopRecording() {
        if (isRecording && line != null) {
            line.stop();
            line.close();
            isRecording = false;
            //TODO: send the audio file to current chat !!!!!!!!!! Finished
            this.setMediaFile(audioFile,new FileItemController.Action() {
                @Override
                public void OnActionDelete() {
                    try {
                        Files.delete(audioFile.toPath());
                        System.out.println("Sharing the file Canceled and File is Deleted");
                    } catch (IOException e) {
                        System.out.println("cannot delete recorded voice file");
                    };
                }
                @Override
                public void OnActionCleared() {
                    audioFile = null;
                }

                @Override
                public void OnClickItem() {
                    try {
                        if(Desktop.isDesktopSupported())
                            Desktop.getDesktop().open(audioFile);
                    } catch (IOException e) {
                        System.out.println("Cannot Open the Sound File");
                    }
                }
            });
            System.out.println("Recording stopped and saved.");
        }
    }
    private menu_bageControler menu_bageControler;
    private VBox menu_bageRootVbox;
    @FXML
    private void handleMenuButtonAction(ActionEvent event) {
        if(menuItemContainer.getChildren().isEmpty()){
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/orgs/tuasl_clint/fxml/menu_bage.fxml"));
                menu_bageRootVbox = loader.load();
                menu_bageControler =  loader.getController();
                menu_bageControler.setOnCreateGroupLisiner(new menu_bageControler.CreateGroupListiner() {
                    @Override
                    public void onCreateGroup(Chat chat, boolean isSaved) {
                        if(isSaved){
                            addChatItem(chat);
                        }
                    }
                });
                menu_bageControler.setOnGoBackButtonClickListener(new menu_bageControler.OnGoBackButtonClickListener() {
                    @Override
                    public void onGoBackButtonClickListener() {
                        leftMainAllContainer.getChildren().remove(menuItemContainer);
                        leftMainAllContainer.getChildren().add(chatsMainContainer);
                    }
                });
                this.menuItemContainer.getChildren().add(menu_bageRootVbox);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.leftMainAllContainer.getChildren().remove(chatsMainContainer);
        if(!this.leftMainAllContainer.getChildren().contains(this.menuItemContainer)){
            this.leftMainAllContainer.getChildren().add(this.menuItemContainer);
        }
    }

    private final String SHARE_FOLDER = "src/main/resources/orgs/tuasl_clint/file/";

    @FXML
    private void handleShareButtonAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Share");
        Stage stage = (Stage) shareButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            this.setMediaFile(selectedFile, new FileItemController.Action() {
                @Override
                public void OnActionDelete() {
                    System.out.println("Sharing The File Canceled");
                }
                @Override
                public void OnActionCleared() {
                    try {
                        String FileItem = FilesHelper.getMediaViewerPath(selectedFile);
                        Path destinationDirectory = Paths.get(FilesHelper.getFilePath(selectedFile));
                        if (!Files.exists(destinationDirectory)) {
                            Files.createDirectories(destinationDirectory);
                            System.out.println("Created directory: " + destinationDirectory.toAbsolutePath());
                        }
                        Path destinationPath = destinationDirectory.resolve(selectedFile.getName());                    // Copy the selected file to the destination directory
                        try {
                            Files.copy(selectedFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                        } catch (FileSystemException e) {
                            System.err.println("Cannot Copy The File Error : " + e.getMessage());
                        }
                        System.out.println("File copied successfully: " + selectedFile.getAbsolutePath() +
                                " to " + destinationPath.toAbsolutePath());
                    }catch (Exception e) {
                        System.err.println("Error copying file: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                @Override
                public void OnClickItem() {
                        try {
                            if(Desktop.isDesktopSupported())
                                Desktop.getDesktop().open(selectedFile);
                        } catch (IOException e) {
                            System.err.println("Error : Cannot Open the File");
                        }
                }
            });
        } else {
            System.out.println("File selection cancelled.");
        }
    }

    @FXML
    private void handleEmojiButtonAction() {
        System.out.println("Emoji button clicked!");        // تبديل حالة الظهور للـ ScrollPane
        boolean isVisible = emojiScrollPane.isVisible();
        emojiScrollPane.setVisible(!isVisible);
        emojiScrollPane.setManaged(!isVisible); // تبديل خاصية Managed        // إذا أصبحت اللوحة مرئية، قم بتعبئة الإيموجي إذا كانت حاوية الإيموجي فارغة
        if (!isVisible) { // بمعنى إذا كانت ستصبح مرئية الآن
            if (areaOfEmojis.getChildren().isEmpty()) { // نتحقق من FlowPane الفعلي
                String[] emojis = {
                        "\uD83D\uDE00", "\uD83D\uDE01", "\uD83D\uDE02", "\uD83D\uDE03", "\uD83D\uDE04",
                        "\uD83D\uDE05", "\uD83D\uDE06", "\uD83D\uDE07", "\uD83D\uDE08", "\uD83D\uDE09",
                        "\uD83D\uDE0A", "\uD83D\uDE0B", "\uD83D\uDE0C", "\uD83D\uDE0D", "\uD83D\uDE0E",
                        "\uD83D\uDE0F", "\uD83D\uDE10", "\uD83D\uDE11", "\uD83D\uDE12", "\uD83D\uDE13",
                        "\uD83D\uDE14", "\uD83D\uDE15", "\uD83D\uDE16", "\uD83D\uDE17", "\uD83D\uDE18",
                        "\uD83D\uDE19", "\uD83D\uDE1A", "\uD83D\uDE1B", "\uD83D\uDE1C", "\uD83D\uDE1D",
                        "\uD83D\uDE1E", "\uD83D\uDE1F", "\uD83D\uDE20", "\uD83D\uDE21", "\uD83D\uDE22",
                        "\uD83D\uDE23", "\uD83D\uDE24", "\uD83D\uDE25", "\uD83D\uDE26", "\uD83D\uDE27",
                        "\uD83D\uDE28", "\uD83D\uDE29", "\uD83D\uDE2A", "\uD83D\uDE2B", "\uD83D\uDE2C",
                        "\uD83D\uDE2D", "\uD83D\uDE2E", "\uD83D\uDE2F", "\uD83D\uDE30", "\uD83D\uDE31",
                        "\uD83D\uDE32", "\uD83D\uDE33", "\uD83D\uDE34", "\uD83D\uDE35", "\uD83D\uDE36",
                        "\uD83D\uDE37", "\uD83E\uDD2A", "\uD83E\uDD2B", "\uD83E\uDD2C", "\uD83E\uDD2D",
                        "\uD83E\uDD2E", "\uD83E\uDD2F", "\uD83E\uDD70", "\uD83E\uDD71", "\uD83E\uDD72",
                        "\uD83E\uDD73", "\uD83E\uDD74", "\uD83E\uDD75", "\uD83E\uDD76", "\uD83E\uDD77",
                        "\uD83E\uDD78", "\uD83E\uDD7A", "\uD83E\uDD7B", "\uD83E\uDD7C", "\uD83E\uDD7D",
                        "\uD83E\uDD7E", "\uD83D\uDC4D", "\uD83D\uDC4F", "\uD83D\uDC4C", "\uD83D\uDC4A",
                        "\uD83D\uDC4B", "\uD83D\uDC4E", "\uD83D\uDE4B", "\u270C\uFE0F", "\uD83D\uDC50",
                        "\uD83D\uDE4C", "\u2764\uFE0F", "\uD83D\uDC99", "\uD83D\uDC9A", "\uD83D\uDC9B",
                        "\uD83D\uDC9C", "\uD83D\uDC9D", "\uD83D\uDC9E", "\uD83D\uDC9F", "\uD83D\uDCAF",
                        "\uD83D\uDCA3", "\uD83D\uDCA4", "\uD83D\uDCA6", "\uD83D\uDCA8", "\uD83D\uDCAB",
                        "\uD83D\uDCC8", "\uD83D\uDCC9", "\uD83D\uDCCC", "\uD83D\uDCCD", "\uD83D\uDCE0",
                        "\uD83D\uDCE1", "\uD83D\uDCE2", "\uD83D\uDCE3", "\uD83D\uDCE4", "\uD83D\uDCE5",
                        "\uD83D\uDCE6", "\uD83D\uDCE7", "\uD83D\uDCE8", "\uD83D\uDCE9", "\uD83D\uDCEA",
                        "\uD83D\uDCED", "\uD83D\uDCEF", "\uD83D\uDCF0", "\uD83D\uDCF1", "\uD83D\uDCF2",
                        "\uD83D\uDCF3", "\uD83D\uDCF4", "\uD83D\uDCF5", "\uD83D\uDCF6", "\uD83D\uDCF7",
                        "\uD83D\uDCF8", "\uD83D\uDCF9", "\uD83D\uDCFA", "\uD83D\uDCFB", "\uD83D\uDCFC",
                        "\uD83D\uDCFD", "\uD83D\uDCFE", "\uD83D\uDCFF", "\uD83D\uDD00", "\uD83D\uDD01",
                        "\uD83D\uDD02", "\uD83D\uDD03", "\uD83D\uDD04", "\uD83D\uDD05", "\uD83D\uDD06",
                        "\uD83D\uDD07", "\uD83D\uDD08", "\uD83D\uDD09", "\uD83D\uDD0A", "\uD83D\uDD0B",
                        "\uD83D\uDD0C", "\uD83D\uDD0D", "\uD83D\uDD0E", "\uD83D\uDD0F", "\uD83D\uDD10",
                        "\uD83D\uDD11", "\uD83D\uDD12", "\uD83D\uDD13", "\uD83D\uDD14", "\uD83D\uDD15",
                        "\uD83D\uDD16", "\uD83D\uDD17", "\uD83D\uDD18", "\uD83D\uDD19", "\uD83D\uDD1A",
                        "\uD83D\uDD1B", "\uD83D\uDD1C", "\uD83D\uDD1D", "\uD83D\uDD1E", "\uD83D\uDD1F",
                        "\uD83D\uDD20", "\uD83D\uDD21", "\uD83D\uDD22", "\uD83D\uDD23", "\uD83D\uDD24",
                        "\uD83D\uDD25", "\uD83D\uDD26", "\uD83D\uDD27", "\uD83D\uDD28", "\uD83D\uDD29",
                        "\uD83D\uDD2A", "\uD83D\uDD2B", "\uD83D\uDD2C", "\uD83D\uDD2D", "\uD83D\uDD2E",
                        "\uD83D\uDD2F", "\uD83D\uDD30", "\uD83D\uDD31", "\uD83D\uDD32", "\uD83D\uDD33",
                        "\uD83D\uDD34", "\uD83D\uDD35", "\uD83D\uDD36", "\uD83D\uDD37", "\uD83D\uDD38",
                        "\uD83D\uDD39", "\uD83D\uDD3A", "\uD83D\uDD3B", "\u2B1C", "\u2B1B",
                        "\u25FC", "\u25FB", "\u25FD", "\u25FE"
                };
                for (String emoji : emojis) {
                    Label emojiLabel = getEmojiLabel(emoji);
                    areaOfEmojis.getChildren().add(emojiLabel);
                }

            }
        }
    }

    private Label getEmojiLabel(String emoji) {
        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 30px; -fx-padding: 5px;");
        emojiLabel.setOnMouseClicked((MouseEvent e) -> {
            messageInputField.appendText(emojiLabel.getText());
            // عند اختيار إيموجي، قم بإخفاء الـ ScrollPane وإعادة منطقة المنتصف إلى التوسع
            //emojiScrollPane.setVisible(false);
            //emojiScrollPane.setManaged(false);
            messageInputField.requestFocus();
            e.consume();
        });
        return emojiLabel;
    }

//    private void loadChatParticipantFromLocalDB( Chat chat){
//        if(chat == null)
//            return;
//        String sql1 = "SELECT * FROM chat_participants WHERE chat_id = ?";
//        try(PreparedStatement stmt = DatabaseConnectionSQLite.getInstance().getConnection().prepareStatement(sql1)){
//            stmt.setLong(1, chat.getId());
//            ResultSet rs1 = stmt.executeQuery();
//            while (rs1.next()){
//                addChatParticipant(ChatParticipantFactory.createFromResultSet(rs1), chat);
//            }
//        } catch (SQLException e) {
//            System.err.println("----- Error Loading Participants Of Chat : "+chat.getChatName());
//            e.printStackTrace();
//        }
//    }


    /*
        @FXML
        public void handleAudioCallButtonAction(ActionEvent event) {
            String selectedUser = chatListView.getSelectionModel().getSelectedItem();
            if (selectedUser == null) {
                System.out.println("❌ لم يتم اختيار مستخدم لإجراء المكالمة");
                return;
            }
            try {
                //TODO: use the socket to share call between these users | participles
                Socket audioSocket = new Socket("localhost", 6001); // منفذ الصوت
                AudioCallWindow audioCallWindow = new AudioCallWindow("📞 مع " + selectedUser);
                AudioSender audioSender = new AudioSender();
                audioSender.start(audioSocket);

                AudioReceiver audioReceiver = new AudioReceiver();
                audioReceiver.start(audioSocket);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("❌ فشل الاتصال بمكالمة الصوت");
            }

        }

        public Stage getMediaStageShower(){
            if(mediaStageShower != null){
                mediaStageShower = new Stage();
            }
            return mediaStageShower;
        }
        */
    @FXML
    public void handleAudioCallButtonAction(ActionEvent event) {
        Chat selectedUser = chatListView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            System.out.println("❌ لم يتم اختيار مستخدم لإجراء المكالمة");
            return;
        }

        try {
            // مثال: IP الطرف الآخر هو 192.168.1.100 و البورت 7711
            String remoteIP = "localhost";
            int remotePort = 7711;

            // توليد بورت عشوائي للاستقبال
            DatagramSocket tempSocket = new DatagramSocket(0);
            int localPort = tempSocket.getLocalPort();
            tempSocket.close();

            AudioCallWindow audioCallWindow = new AudioCallWindow("📞 مع " + selectedUser);
            AudioSendUDP sender = new AudioSendUDP();
            sender.start(remoteIP, remotePort);

            AudioReceiverUDP receiver = new AudioReceiverUDP();
            receiver.start(localPort); // استقبل على نفس البورت

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ فشل الاتصال بمكالمة الصوت");
        }
    }

    /*

    @FXML
    public void handleVideoCallButtonAction(ActionEvent event) {
        String selectedUser = chatListView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            System.out.println("❌ لم يتم اختيار مستخدم لإجراء المكالمة");
            return;
        }

        try {
            String remoteIP = "localhost";          // أو IP الجهاز الآخر
            int remoteVideoPort = 8811;             // هذا بورت السيرفر الثابت

            VideoCallWindowUDP callWindow = new VideoCallWindowUDP("📹 مكالمة فيديو مع " + selectedUser);
            callWindow.startSending(remoteIP, remoteVideoPort);  // إرسال للفيديو
            callWindow.startReceiving();                         // استقبال على بورت عشوائي

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ فشل الاتصال بالفيديو");
        }
    }
    */
    @FXML
    public void handleVideoCallButtonAction(ActionEvent event) {
        Chat selectedUser = chatListView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            System.out.println("❌ لم يتم اختيار مستخدم لإجراء المكالمة");
            return;
        }

        try {
            String remoteIP = "localhost";          // أو IP الجهاز الآخر
            int remoteVideoPort = 8811;             // هذا بورت السيرفر الثابت

            VideoCallWindowUDP callWindow = new VideoCallWindowUDP("📹 مكالمة فيديو مع " + selectedUser);
            callWindow.startSending(remoteIP, remoteVideoPort);  // إرسال للفيديو
            callWindow.startReceiving();                         // استقبال على بورت عشوائي

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ فشل الاتصال بالفيديو");
        }
    }


    public void handleAddParticipantButtonClicked(ActionEvent event) {

    }
}
