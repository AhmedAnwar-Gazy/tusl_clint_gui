package orgs.tuasl_clint.controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import orgs.tuasl_clint.models2.FactoriesSQLite.MediaFactory;
import orgs.tuasl_clint.models2.Media;
import orgs.tuasl_clint.models2.Message;
import orgs.tuasl_clint.utils.BackendThreadManager.DataModel;
import orgs.tuasl_clint.utils.FilesHelper;
import orgs.tuasl_clint.utils.TimeStampHelperClass;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;

public class SendMessageItemController {

    @FXML
    private VBox VboxMessage;

    @FXML
    private Text contentText;

    @FXML
    private Label emojiLabel;

    @FXML
    private Label heartEmoji;

    @FXML
    private VBox mediaContainers;

    @FXML
    private Label noActionEmoji;

    @FXML
    private Label okEmoji;

    @FXML
    private Label optionsButton;

    @FXML
    private VBox reactionsContainer;

    @FXML
    private Label sadEmoji;

    @FXML
    private Label senderLabel;

    @FXML
    private Label smileEmoji;

    @FXML
    private Label statusLabel;

    @FXML
    private Label timeLabel;

    private final ObjectProperty<Message> message = new SimpleObjectProperty<>();
    private static synchronized void soutt(String msg){
        System.out.println("----- ["+Thread.currentThread().getName()+"][SendMessageController] : "+msg);
    }
    private static synchronized void serrr(String msg){
        System.err.println("----- ["+Thread.currentThread().getName()+"][SendMessageController] : "+msg);
    }
    public void setMessageData(ObjectProperty<Message> message) {
        soutt("setting the Message : "+ message.get().toString());
        if(message.get() != null)
            this.message.set(message.get());
        setMessageDataFromMessage();
    }

    private void setMessageDataFromMessage() {
        soutt("Loading Message Data............");
        if (message.get().getSenderName() != null && !message.get().getSenderName().isEmpty()) {
            this.senderLabel.setText(message.get().getSenderName());
        }
        contentText.textProperty().bind(Bindings.createStringBinding( ()->{
            if(message.get() != null){
                return message.get().getContent();
            }else {
                return "";
            }
        },message));
        contentText.setFont(new Font("Segoe UI Emoji", 12));
        senderLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            if(message.get().getSenderName() != null && !message.get().getSenderName().isEmpty())
                return message.get().getSenderName();
            else
                return "UnKnown";
        }, message));
        timeLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            if(message.get() != null && message.get().getSentAt() != null)
                return TimeStampHelperClass.formatTimeLeft(message.get().getSentAt());
            else
                return "--:--";
        },message));

//        emojiLabel.setText(message.getMessageType());
        //ENUM('text', 'image', 'video', 'voiceNote', 'file', 'system')
        switch (FilesHelper.toMediaType(message.get().getMessageType())){
            case TEXT:
                break;
            case IMAGE:
                loadImageMessages();
                break;
            case VIDEO:
                loadVideoMessages();
                break;
            case AUDIO:
                loadAudioMessages();
                break;
            case FILE, STICKER:
                soutt("Handle file message");
                loadFileMessages();
                break;
            default:
                soutt("Unknown message type");
        }
    }

    private void loadFileMessages() {
        soutt("Loading File Message..."+message);
        try {
            Media m = message.get().getMedia();
            if(m == null)
                m = DataModel.getInstance().getMedia(message.get().getMediaId());
            if(m != null){
                File f = new File(m.getFilePathOrUrl());
                if(f.exists() && f.isFile()){
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/orgs/tuasl_clint/fxml/fileItem.fxml"));
                    Parent mesiaCard = loader.load();
                    FileItemController fileItemController = loader.getController();
                    fileItemController.setFile(f, new FileItemController.Action() {
                        @Override
                        public void OnDeleteAction() {

                        }
                        @Override
                        public void OnClearedAction() {

                        }

                        @Override
                        public void OnItemClickedAction() {
                            try {
                                if(Desktop.isDesktopSupported())
                                    Desktop.getDesktop().open(f);
                            } catch (IOException e) {
                                soutt("Cannot Open The File Error : "+ e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    });
                    fileItemController.desableCloseButton();
                    mediaContainers.getChildren().add(mesiaCard);
                }else {
                    soutt("\n\n------------Media File Is not Fount , Loading File Item Loader........");
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/orgs/tuasl_clint/fxml/loadItem.fxml"));
                    Parent loadCard = loader.load();
                    LoadItemController loadItemController = loader.getController();
                    loadItemController.setMedia(m);
                    mediaContainers.getChildren().add(loadCard);
                    loadItemController.setOnReadyItemListener(new LoadItemController.OnReadyItemListener() {
                        @Override
                        public void onReadyItem(HBox fileItemContainer) {
                            loadFileMessages();
                        }
                    });

                }
            }
            //messageScrollPane.setVvalue(1.0);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the error, e.g., show an alert
            serrr("Error : Failed to load UserCard.fxml: " + e.getMessage());
        }
    }
    
    private void loadAudioMessages() {
        soutt("Loading Audio Item  for message is :"+message.toString());
        try {
            Media media = message.get().getMedia();
            if(media == null)
                media = DataModel.getInstance().getMedia(message.get().getMediaId());
            if(media != null){
                soutt("Media Audio Message : "+ media.toString());
                File f = new File(media.getFilePathOrUrl());
                if(f.exists() && f.isFile()){
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/orgs/tuasl_clint/fxml/audioItem.fxml"));
                    Parent mesiaCard = loader.load();
                    AudioController audioController = loader.getController();
                    String urlOfFile = media.getFilePathOrUrl().substring(36);
                    soutt("File with Url : "+ urlOfFile);
                    soutt("Main name is : "+ media.getFilePathOrUrl());
                    URL url = getClass().getResource(media.getFilePathOrUrl());
                    audioController.loadAudioMedia(url.toURI().toString());
                    mediaContainers.getChildren().add(mesiaCard);
                }else {
                    soutt("\n\n------------Media File Is not Fount , Loading File Item Loader........");
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/orgs/tuasl_clint/fxml/loadItem.fxml"));
                    Parent loadCard = loader.load();
                    LoadItemController loadItemController = loader.getController();
                    loadItemController.setMedia(media);
                    mediaContainers.getChildren().add(loadCard);
                    Media finalMedia = media;
                    loadItemController.setOnReadyItemListener(new LoadItemController.OnReadyItemListener() {
                        @Override
                        public void onReadyItem(HBox fileItemContainer) {
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/orgs/tuasl_clint/fxml/audioItem.fxml"));
                                Parent mesiaCard = loader.load();
                                AudioController audioController = loader.getController();
                                audioController.loadAudioMedia(finalMedia.getFilePathOrUrl());
                                fileItemContainer.getChildren().add(mesiaCard);
                                fileItemContainer.setVisible(true);
                            } catch (IOException e) {
                                soutt("Fail to Load the media downloaded");
                            }
                        }
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the error, e.g., show an alert
            serrr("Error : Failed to load UserCard.fxml: " + e.getMessage());
        } catch (URISyntaxException e) {
            serrr("----- Fail Convert Url to Uri");
        }
    }

    private void loadVideoMessages() {
        soutt("Loading Video Message : "+message.toString());
        try {
            // Load FXML
            Media   m = message.get().getMedia();
            if (m == null)
                m = DataModel.getInstance().getMedia(message.get().getMediaId());
            if (m != null) {
                String mediaUri = "src/main/resources/orgs/tuasl_clint/videos/" + m.getFileName();
                File mediaFile = new File(mediaUri);
                if(mediaFile.exists() && mediaFile.isFile()){
                    URL fxmlUrl = getClass().getResource("/orgs/tuasl_clint/fxml/videoItem.fxml");
                    FXMLLoader loader = new FXMLLoader(fxmlUrl);
                    Parent videoItem = loader.load();
                    VideoPlayerController controller = loader.getController();
                    controller.setVideoFile(mediaUri);
                    mediaContainers.getChildren().add(videoItem);
                    soutt("complete loading the Message ....");
                }
                else {
                    soutt("\n\n------------Media File Is not Fount , Loading File Item Loader........");
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/orgs/tuasl_clint/fxml/loadItem.fxml"));
                    Parent loadCard = loader.load();
                    LoadItemController loadItemController = loader.getController();
                    loadItemController.setMedia(m);
                    mediaContainers.getChildren().add(loadCard);
                    Media finalM = m;
                    loadItemController.setOnReadyItemListener(new LoadItemController.OnReadyItemListener() {
                        @Override
                        public void onReadyItem(HBox fileItemContainer) {
                            try {
                                URL fxmlUrl = getClass().getResource("/orgs/tuasl_clint/fxml/videoItem.fxml");
                                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                                Parent videoItem = loader.load();
                                VideoPlayerController controller = loader.getController();
                                String mediaUri = "src/main/resources/orgs/tuasl_clint/videos/" + finalM.getFileName();
                                controller.setVideoFile(mediaUri);
                                fileItemContainer.getChildren().add(videoItem);
                            } catch (IOException e) {
                                soutt("Fail to Load the media downloaded");
                            }
                        }
                    });
                }
            }else {
                serrr("Error : Cannot get the media from database");
            }

        } catch (Exception e) {
            showErrorAlert("Video Error", "Failed to load video: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadImageMessages() {
        soutt("loading Image Message : "+message.toString());
        try {
            Media m = message.get().getMedia();
            if(m == null)
                m = DataModel.getInstance().getMedia(message.get().getMediaId());
            if(m != null) {
                URL url = getClass().getResource("/orgs/tuasl_clint/images/"+m.getFileName());
                if(url != null){
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/orgs/tuasl_clint/fxml/imageItem.fxml"));
                    Parent mesiaCard = loader.load();
                    ImageMessageController imageMessageController = loader.getController();
                    imageMessageController.loadImage(url.toURI().toString());
                    soutt("Message id : "+message.get().getId()+" with image media : " + url.toURI().toString());
                    mediaContainers.getChildren().add(mesiaCard);
                }else {
                    soutt("\n\n------------Media File Is not Fount , Loading File Item Loader........");
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/orgs/tuasl_clint/fxml/loadItem.fxml"));
                    Parent loadCard = loader.load();
                    LoadItemController loadItemController = loader.getController();
                    loadItemController.setMedia(m);
                    mediaContainers.getChildren().add(loadCard);
                    Media finalM = m;
                    loadItemController.setOnReadyItemListener(new LoadItemController.OnReadyItemListener() {
                        @Override
                        public void onReadyItem(HBox fileItemContainer) {
                            try {
                                URL fxmlUrl = getClass().getResource("/orgs/tuasl_clint/fxml/imageItem.fxml");
                                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                                Parent videoItem = loader.load();
                                ImageMessageController controller = loader.getController();
                                URL mediaUri = getClass().getResource("/orgs/tuasl_clint/videos/" + finalM.getFileName());
                                if(mediaUri != null){
                                    controller.loadImage(mediaUri.toString());
                                    fileItemContainer.getChildren().add(videoItem);
                                    mediaContainers.getChildren().add(videoItem);
                                }
                            } catch (IOException e) {
                                serrr("----- Fail to Load the media downloaded");
                            }
                        }
                    });
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            // Handle the error, e.g., show an alert
            serrr("Error : Failed to load UserCard.fxml: " + e.getMessage());
        } catch (URISyntaxException e) {
            soutt("Cannot Convert Url Into URI ........................!!!!!!!!!!!!");
        }
    }
    private void showErrorAlert(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
    }
    @FXML
    void handleReaction(MouseEvent event) {
        String reaction = ((Label) event.getSource()).getText();
        soutt("user clicked on emoji on a message : "+ reaction+ " with x : "+event.getX()+ " Y : "+ event.getY() + " ON MESSAGE ID : "+ this.getMessage().get().getId());
        // عرض الإيموجي في الـ emojiLabel
        emojiLabel.setText(reaction);
        emojiLabel.setVisible(true);
        emojiLabel.setManaged(true);

        // يمكنك حفظ هذا التفاعل في كائن Message أو قاعدة البيانات لاحقًا
//        message.get().setMessageType(reaction);
    }

    public ObjectProperty<Message> getMessage() {
        return message;
    }

    @FXML
    void handleMessageHoverEnter(MouseEvent event) {
//        soutt("mouse enter message with x : "+ event.getX()+ " and y : "+ event.getY() + "messh : "+ VboxMessage.getHeight() + " sum... : " + + sumofChildsHeights(reactionsContainer.getChildren()) );
//        if(VboxMessage.getHeight() <= sumofChildsHeights(reactionsContainer.getChildren()))
//            reactionsContainer.setLayoutY(-1 * VboxMessage.getHeight() + 3);
//        else if(VboxMessage.getHeight() > event.getY() + sumofChildsHeights(reactionsContainer.getChildren()))
//            reactionsContainer.setLayoutY(( event.getY()-VboxMessage.getHeight()));
//        else
//            reactionsContainer.setLayoutY(1 - sumofChildsHeights(reactionsContainer.getChildren()));
//        reactionsContainer.setLayoutX(VboxMessage.getWidth());
//        this.reactionsContainer.setVisible(true);

    }


    @FXML
    void handleMessageHoverExit(MouseEvent event) {
//        soutt("mouse exit message");
//        if(message.getViewCount() == 0)
        this.reactionsContainer.setVisible(false);
    }


    public VBox getView() {
        return this.VboxMessage;
    }
}
