package orgs.tuasl_clint.controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import orgs.tuasl_clint.models2.Chat;
import orgs.tuasl_clint.utils.BackendThreadManager.DataModel;
import orgs.tuasl_clint.utils.BackendThreadManager.Executor;
import orgs.tuasl_clint.utils.TimeStampHelperClass;

import java.net.URL;
import java.util.ResourceBundle;

public class ChatListItemController implements Initializable {

    @FXML
    private HBox HboxAllProfile;

    @FXML
    private Label lastMessageLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private ImageView profilePictureImageView;

    @FXML
    private Label statusLabel;

    @FXML
    private Label timestampLabel;

    @FXML
    private Label unreadCountLabel;

    @FXML
    private StackPane unreadCountPane;

    private ObjectProperty<Chat> chatObjectProperty = new SimpleObjectProperty<>();
    private ObjectProperty<Integer> unreadCount;
    private ObjectProperty<Boolean> selected;

    public Chat getChat() {
        return chatObjectProperty.get();
    }

    public void setChat(ObjectProperty<Chat> chat) {
        this.chatObjectProperty.set(chat.get());
    }
    private ChangeListener<Chat> listener;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
         chatObjectProperty.addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Chat> observableValue, Chat oldVal, Chat newVal) {
                if (newVal != null) {
                    nameLabel.setText(newVal.getChatName());
                    lastMessageLabel.setText((!DataModel.getInstance().getMessages(newVal.getId()).values().isEmpty() ?DataModel.getInstance().getMessages(newVal.getId()).values().stream().toList().getLast().get().getContent():("...")));
                    if(newVal.getChatPictureUrl() != null && !(newVal.getChatPictureUrl().isEmpty())){
                        Task<Image> task = new Task<Image>(){
                            @Override
                            protected Image call() throws Exception {
                                try {
                                    return new Image(newVal.getChatPictureUrl());
                                } catch (Exception e) {
                                    System.out.println("an Error occured while trying to updata the chat Image");
                                    return null;
                                }
                            }
                        };
                        task.setOnSucceeded(_ ->{
                            if(task.getValue() != null)
                                updateProfilePicture(task.getValue());
                        });
                        Executor.submit(task);
                    }
                }
            }
        });
//        soutt("Binding Chat Values...");
//        nameLabel.textProperty().bind(Bindings.selectString(chatObjectProperty,(chatObjectProperty.get() != null?(chatObjectProperty.get().get() != null?(chatObjectProperty.get().get().getChatName()):("Unknown")):("UnSet"))));
////        unreadCountLabel.textProperty().bind(Bindings.selectString(chatObjectProperty,(chatObjectProperty.get() != null?(chatObjectProperty.get().get() != null?(chatObjectProperty.get().get().get):("Unknown")):("UnSet"))));
//        lastMessageLabel.textProperty().bind(Bindings.selectString(chatObjectProperty,(chatObjectProperty.get() != null?(chatObjectProperty.get().get() != null?(DataModel.getInstance().getMessages(chatObjectProperty.get().get().getId()).values().stream().toList().getLast().get().getContent()):("...")):("...."))));
//        timestampLabel.textProperty().bind(Bindings.selectString(chatObjectProperty,(chatObjectProperty.get() != null?(chatObjectProperty.get().get() != null?TimeStampHelperClass.formatTimeLeft(DataModel.getInstance().getMessages(chatObjectProperty.get().get().getId()).values().stream().toList().getLast().get().getSentAt()):("...")):("...."))));

    }
    private static void soutt(String msg){
        System.out.println("----- ["+Thread.currentThread().getName()+"][ChatListItemController] : "+msg);
    }
    public HBox getView() {
        return this.HboxAllProfile;
    }

    public interface OnImageItemClickedListener{
        public void onImageItemClicked(ObjectProperty<Chat> chat, ChatListItemController controller);
    }
    public interface OnItemClickedListener{
        public void onItemClicked(ObjectProperty<Chat> Chat, ChatListItemController controller);
    }

    private OnImageItemClickedListener onImageItemClickedListener;
    private OnItemClickedListener onItemClickedListener;

    public void setOnImageItemClickedListener(OnImageItemClickedListener onImageItemClickedListener) {
        this.onImageItemClickedListener = onImageItemClickedListener;
    }

    public void setOnItemClickedListener(OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }

    public void updateProfilePicture(Image image) {
        this.profilePictureImageView.setImage(image);
        makeCircularImage(profilePictureImageView);
    }

    @FXML
    void handelGroupImageClicked(MouseEvent event) {
        System.out.println("Image Of Item Clicked Of Chat ID: "+((chatObjectProperty.get() != null)?this.chatObjectProperty.get().getId(): "null"));
        if (chatObjectProperty.get() != null && onImageItemClickedListener != null) {
            onImageItemClickedListener.onImageItemClicked(chatObjectProperty, this);
        }
    }
    private void makeCircularImage(ImageView imageView) {
        Circle clip = new Circle(
                imageView.getFitWidth()/2,
                imageView.getFitHeight()/2,
                imageView.getFitWidth()/2
        );
        imageView.setClip(clip);
    }
    public void cleanup() {
        this.onImageItemClickedListener = null;
        this.onItemClickedListener = null;
        if (profilePictureImageView != null) {
            profilePictureImageView.setImage(null);
        }
    }
    @FXML
    void handleGoToChat(MouseEvent event) {
        System.out.println("Item Clicked With Chat_ID: "+((chatObjectProperty.get() != null)?this.chatObjectProperty.get().getId(): "null"));
        if (chatObjectProperty.get() != null) {
            if (onItemClickedListener != null) {
                onItemClickedListener.onItemClicked(chatObjectProperty, this);
            }
            this.unreadCountPane.setVisible(false);
        }
    }
    public void setSelected(Boolean b){
        this.selected.set(b);
    }

}
