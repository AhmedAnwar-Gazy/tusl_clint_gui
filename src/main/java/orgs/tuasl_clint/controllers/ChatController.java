package orgs.tuasl_clint.controllers;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import orgs.tuasl_clint.client.ChatClient;
import orgs.tuasl_clint.client.OnFileTransferListener;
import orgs.tuasl_clint.client.OnNewMessageListener;
import orgs.tuasl_clint.models2.*;
import orgs.tuasl_clint.models2.FactoriesSQLite.*;
import orgs.tuasl_clint.protocol.Response;
import orgs.tuasl_clint.utils.*;
import javafx.event.ActionEvent;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.URL;
import java.nio.file.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.List;
import javax.sound.sampled.*;
import javax.swing.*;

import javafx.scene.input.MouseEvent;
import javafx.fxml.FXML;
import orgs.tuasl_clint.utils.BackendThreadManager.*;

import static java.lang.System.exit;

public class ChatController{
    @FXML
    public VBox chatsMainContainer;
    @FXML
    public HBox menuItemContainer;
//    @FXML
//    public VBox leftMainAllContainer;
    @FXML
    public TextField searchTF;
    @FXML
    public StackPane leftMainStackPane;
    @FXML
    public Button addParticipantButton;
    @FXML
    public StackPane centerStackPane;
    @FXML
    public BorderPane centerChatChatting;
    @FXML
    private BorderPane rootBorderPane;
    @FXML
    private ListView<ObjectProperty<Chat>> chatListView;
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
    private ScrollPane emojiScrollPane;


    private volatile boolean isRecording = false;
    private TargetDataLine line;    // Define the folder where recordings should be saved
    private final String RECORDING_FOLDER = "src/main/resources/orgs/tuasl_clint/voiceNote/";

    private File audioFile;
    private FileItemController mediaFileController;
    private final ObjectProperty<Chat> currentChat = new SimpleObjectProperty<>();

    private void soutt(String message){
        System.out.println("----- ["+Thread.currentThread().getName()+"] : "+message);
    }
    private void serrr(String message){
        System.err.println("----- ["+Thread.currentThread().getName()+"] : "+message);
    }


    @FXML
    private void handleSettingsButtonAction(ActionEvent event) {
        Navigation.loadPage("settings.fxml");
    }

    @FXML
    private void handleSendButtonAction(ActionEvent event) {
        if(currentChat.get() == null){
            this.messageDisplayArea.getChildren().add(new Label("Select Chat To send The Message To"));
            return;
        }
        String messageText = messageInputField.getText().trim();
        if (!this.message_media_selected_container.getChildren().isEmpty()) {
            this.message_media_selected_container.getChildren().clear();
            File file = mediaFileController.getFile();
            Media m = new Media(mediaFileController.getFile().getName(), file.getAbsolutePath(), FilesHelper.getFileExtension(file), FilesHelper.getFileSize(file), new Timestamp(new Date().getTime()));
            m.setUploadedByUserId(User.user.getId());
            Message mm = new Message(messageText);
            mm.setMediaId(m.getId());
            mm.setMedia(m);
            mm.setSenderName(User.user.getFirstName());
            mm.setSenderId(User.user.getId());
            mm.setChatId(currentChat.get().getId());
            mm.setMessageType(FilesHelper.getFileType(mediaFileController.getFile()).name().toLowerCase());
            mediaFileController.action.OnClearedAction();
            messageInputField.clear();
            this.mediaFileController.clear();
            System.out.println("----- ["+Thread.currentThread().getName()+"] :  Now The File Is On Sending ....");
            Executor.execute(()->{
                Response response = ChatClient.getInstance().sendMediaMessage((int) currentChat.get().getId(), m.getFilePathOrUrl(), mm.getContent(), m.getMediaType(), new OnFileTransferListener() {
                    @Override
                    public void onFail(String msg) {
                        System.err.println("----- ["+Thread.currentThread().getName()+"] :  Fail To send The Media Message , Error MSG: " + msg);
                    }

                    @Override
                    public void onProgress(long transferredBytes, long totalSize) {
                        System.out.println("Media Message Is Sent Now and Status Is : " + transferredBytes + " b/ " + totalSize + " b   ");
                    }

                    @Override
                    public void onComplete(File file) {
                        soutt("---- Message Sent SuccessFully----- ["+Thread.currentThread().getName()+"] : -");
                        Platform.runLater(()->dataModel.addMessageToChat(mm));
                    }
                });
            });
            return;
        }
        // Message  : Long messageId, Long chatId, Long senderUserId, String messageType, String content, Long mediaId, Long repliedToMessageId, Long forwardedFromUserId, Long forwardedFromChatId, Timestamp sentAt, Timestamp editedAt, Boolean isDeleted, Integer viewCount
        if (!messageText.isEmpty()) {
            System.out.println("Sending message: " + messageText);
            Message m = new Message(messageText);
            m.setMediaId(m.getMediaId());
            m.setSenderName(User.user.getFirstName());
            m.setSenderId(User.user.getId());
            m.setChatId(currentChat.get().getId());
            m.setMessageType(FilesHelper.fileType.TEXT.name().toLowerCase());
            try {
                if (m.save()) {
                    soutt("----- ["+Thread.currentThread().getName()+"] : ----- ["+Thread.currentThread().getName()+"] : ---sending text message");
                    Response res = ChatClient.getInstance().sendTextMessage((int) currentChat.get().getId(), m.getContent());
                    if (res.isSuccess()) {
                        messageInputField.clear();
                    } else {
                        JOptionPane.showMessageDialog(null, "Cannot send the  Message Error : " + res.getMessage());
                    }
                } else {
                    System.out.println("cannot save the message");
                }
            } catch (SQLException e) {
                System.out.println("an error occurred while trying to save the message error: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSendVoiceButtonPressed(MouseEvent event) {
        System.out.println("Send Voice button pressed...");
        animateButton(true); // Start animation
        startRecording(); // Begin recording
    }

    @FXML
    private void handleSendVoiceButtonReleased(MouseEvent event) {
        System.out.println("Send Voice button Released");
        stopRecording();// Stop recording when released
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
        Executor.submit(()->{
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
        });
    }

    public void setMediaFile(File file, FileItemController.Action action) {
        Platform.runLater(()->{
            if (!this.message_media_selected_container.getChildren().isEmpty() && this.mediaFileController != null && this.mediaFileController.getState() != FileItemController.State.DELETED) {
                mediaFileController.setFile(file, action);
            } else if (!this.message_media_selected_container.getChildren().isEmpty() && this.mediaFileController == null) {
                this.message_media_selected_container.getChildren().clear();
                setMediaFile(file, action);
            } else if (this.message_media_selected_container.getChildren().isEmpty()) {
                Task<Parent> task = new Task<Parent>() {
                    @Override
                    protected Parent call() throws Exception {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/orgs/tuasl_clint/fxml/fileItem.fxml"));
                            Parent pp = loader.load();
                            pp.setUserData(new FileItemController().setFile(file, action));
                            return pp;
                        } catch (IOException e) {
                            System.out.println("Cannot load the file Error is : " + e.getMessage());
                            e.printStackTrace();
                            return null;
                        }
                    }
                };
                task.setOnSucceeded(workerStateEvent ->{
                    Parent p = task.getValue();
                    if(p != null)
                        message_media_selected_container.getChildren().addFirst(task.getValue());
                    else
                        soutt("Cannot Create File Item Controller For Selected File");
                });
                Executor.submit(task);
            }
        });
    }
    private void stopRecording() {
        if (isRecording && line != null) {
            line.stop();
            line.close();
            isRecording = false;
            //TODO: send the audio file to current chat !!!!!!!!!! Finished
            this.setMediaFile(audioFile, new FileItemController.Action() {
                @Override
                public void OnDeleteAction() {
                    try {
                        Files.delete(audioFile.toPath());
                        System.out.println("----- ["+Thread.currentThread().getName()+"] :  Sharing the file Canceled and File is Deleted");
                    } catch (IOException e) {
                        System.out.println("----- ["+Thread.currentThread().getName()+"] :  cannot delete recorded voice file");
                    }
                }
                @Override
                public void OnClearedAction() {
                    audioFile = null;
                }

                @Override
                public void OnItemClickedAction() {
                    try {
                        if (Desktop.isDesktopSupported())
                            Desktop.getDesktop().open(audioFile);
                    } catch (IOException e) {
                        System.out.println("----- ["+Thread.currentThread().getName()+"] :  Cannot Open the Sound File");
                    }
                }
            });
            System.out.println("----- ["+Thread.currentThread().getName()+"] :  Recording stopped and saved.");
        }
    }

    private menu_bageControler menu_bageControler;
    private VBox menu_bageRootVbox;

    @FXML
    private void handleMenuButtonAction(ActionEvent event) {
        if (menuItemContainer.getChildren().isEmpty()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/orgs/tuasl_clint/fxml/menu_bage.fxml"));
                menu_bageRootVbox = loader.load();
                menu_bageControler = loader.getController();
                menu_bageControler.setOnCreateGroupLisiner(new menu_bageControler.CreateGroupListiner() {
                    @Override
                    public void onCreateGroup(Chat chat, boolean isSaved) {
                        dataModel.addChat(chat);
                    }
                });
                menu_bageControler.setOnGoBackButtonClickListener(new menu_bageControler.OnGoBackButtonClickListener() {
                    @Override
                    public void onGoBackButtonClickListener() {
                        menuItemContainer.setVisible(false);
                        menuItemContainer.setManaged(false);
                        chatsMainContainer.setVisible(true);
                        chatsMainContainer.setManaged(true);
                    }
                });
                this.menuItemContainer.getChildren().add(menu_bageRootVbox);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        menuItemContainer.setVisible(true);
        menuItemContainer.setManaged(true);
        chatsMainContainer.setManaged(false);
        chatsMainContainer.setVisible(false);
    }

    private final String SHARE_FOLDER = "src/main/resources/orgs/tuasl_clint/file/";

    @FXML
    private void handleShareButtonAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Share");
        Stage stage = (Stage) shareButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            soutt("Selected path : "+selectedFile.getPath());
            soutt("File Viewer Path To Show is : "+ FilesHelper.getMediaViewerPath(selectedFile));
            this.setMediaFile(selectedFile, new FileItemController.Action() {
                @Override
                public void OnDeleteAction() {
                    System.out.println("Sharing The File Canceled");
                }

                @Override
                public void OnClearedAction() {
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
                    } catch (Exception e) {
                        System.err.println("Error copying file: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                @Override
                public void OnItemClickedAction() {
                    try {
                        if (Desktop.isDesktopSupported())
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
            messageInputField.requestFocus();
            e.consume();
        });
        return emojiLabel;
    }


    public void handleAddParticipantButtonClicked(ActionEvent event) {
        soutt("Loading Add Participant View .......");
        var task = DataModel.createAddChatParticipantControllerTask(((int) currentChat.get().getId()));
        task.setOnSucceeded(abc->{
            soutt("View Controller Loaded........");
            var controller = task.getValue();
            if(controller != null){
                var view = controller.getView();
                this.centerStackPane.getChildren().addLast(view);
                centerChatChatting.setVisible(false);
                controller.setOnCancel(() -> {
                    centerChatChatting.setVisible(true);
                    this.centerStackPane.getChildren().remove(view);
                });
            }else {
                serrr("cannot Load Add Participant For This Chat..........");
                JOptionPane.showMessageDialog(null,"Cannot Open An Add Participant View.....!!");
            }
        });
        Executor.submit(task);
    }



    private final ObservableList<ObjectProperty<Message>> messages = FXCollections.observableArrayList();
    private final DataModel dataModel = DataModel.getInstance();

    public void initialize() {
        menuItemContainer.setVisible(false);
        menuItemContainer.setManaged(false);
        this.addParticipantButton.visibleProperty().bind(Bindings.createBooleanBinding(() -> currentChat.get() != null,currentChat));
        dataModel.setNewMessageReceivedListener(new OnNewMessageListener() {
            @Override
            public void onNewMessageReceived(Message message) {
                if(currentChat.get() != null && currentChat.get().getId() == message.getChatId()){
                    var controller = dataModel.getSendMessageItemControllerOf(message);
                    if(controller != null && controller.get() != null)
                        messageDisplayArea.getChildren().add(controller.get().getView());
                }
            }
        });
        this.emojiScrollPane.setManaged(false);
        this.emojiScrollPane.setVisible(false);
        setupChatsList();
        setupCurrentChatBinding();
        setupMessagesBinding();
        setupMessageDisplay();
        if(!chatListView.getItems().isEmpty()){
            currentChat.set(chatListView.getItems().getFirst().get());
        }
    }

    private void setupChatsList() {
        // ربط قائمة الشاتات
        dataModel.getChats().addListener((MapChangeListener<Long, ObjectProperty<Chat>>) change -> {
            if (change.wasRemoved()) {
                chatListView.getItems().remove(change.getValueRemoved());
            }
            if (change.wasAdded()) {
                chatListView.getItems().add(change.getValueAdded());
            }
        });
        chatListView.setItems(FXCollections.observableArrayList(
                dataModel.getChats().values()
        ));
        chatListView.setCellFactory(param -> new ListCell<ObjectProperty<Chat>>() {
            @Override
            protected void updateItem(ObjectProperty<Chat> chat, boolean empty) {
                super.updateItem(chat, empty);
                if(chat == null || empty || chat.get() == null){
                    return;
                }
                var b = dataModel.getChatListItemControllerProperty(chat.get().getId()).get();
                if(b != null && b.getView() != null)
                    setGraphic(b.getView());
                else {
                    var task = DataModel.createChatListItemControllerTask(chat.get());
                    task.setOnSucceeded(workerStateEvent -> {
                        if(task.getValue() != null)
                            setGraphic(task.getValue().getView());
                    });
                    Executor.submit(task);
                }
            }
        });
    }

    private void setupCurrentChatBinding() {
        // ربط اختيار القائمة مع الشات الحالي
        chatListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> currentChat.set(newVal.get())
        );
    }
    // Track active listener to remove it later
    private MapChangeListener<Long, ObjectProperty<Message>> activeMapListener;
    private ObservableMap<Long, ObjectProperty<Message>> currentMessageMap;


    private void setupMessagesBinding() {
        // ربط الشات الحالي مع الرسائل
        currentChat.addListener((obs, oldChat, newChat) -> {
            // 1. Clean up previous listener
            if (activeMapListener != null && currentMessageMap != null) {
                currentMessageMap.removeListener(activeMapListener);
                activeMapListener = null;
                currentMessageMap = null;
            }

            messages.clear(); // Clear previous chat's messages
//            messageDisplayArea.getChildren().setAll(dataModel.getSendMessageItemControllers(newChat.getId()).values().stream().map(s -> s.get().getView()).toList());
            if (newChat != null) {
                // 2. Get current chat's message map
                currentMessageMap = dataModel.getMessages(newChat.getId());
                messages.setAll(currentMessageMap.values());
                // 3. Create new listener
                activeMapListener = change -> {
                    if (change.wasAdded()) messages.add(change.getValueAdded());
                    if (change.wasRemoved()) messages.remove(change.getValueRemoved());
                };
                currentMessageMap.addListener(activeMapListener);
            }
        });
    }

    private void setupMessageDisplay() {
        messages.addListener(new ListChangeListener<ObjectProperty<Message>>() {
            @Override
            public void onChanged(Change<? extends ObjectProperty<Message>> change) {
                while (change.next()){
                    if(change.wasAdded()){
                        change.getAddedSubList().forEach(item->{
                            var controller = dataModel.getSendMessageItemControllerOf(item.get());
                            if(controller != null && controller.get() != null){
                                messageDisplayArea.getChildren().add(controller.get().getView());
                            }
                        });
                    }
                    if(change.wasRemoved()){
                        change.getRemoved().forEach(item->{
                            var controller = dataModel.getSendMessageItemControllerOf(item.get());
                            if(controller != null && controller.get() != null)
                                messageDisplayArea.getChildren().remove(controller.get().getView());
                        });
                    }
                }
            }
        });
    }
}
