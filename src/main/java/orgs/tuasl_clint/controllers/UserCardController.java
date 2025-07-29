package orgs.tuasl_clint.controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import orgs.tuasl_clint.client.ChatClient;
import orgs.tuasl_clint.models2.User;
import orgs.tuasl_clint.protocol.Response;
import orgs.tuasl_clint.utils.BackendThreadManager.Executor;
import orgs.tuasl_clint.utils.FilesHelper;
import orgs.tuasl_clint.utils.Navigation;

import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class UserCardController implements Initializable,Controller {

    public Button videoCallButton;
    public Button audioCallButton;
    @FXML
    private StackPane mainContainer;

    @FXML
    private ImageView userImage;

    @FXML
    private HBox userInfoHboxContainer;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userPhoneLabel;

    @FXML
    private CheckBox checkedItem;

    ObjectProperty<User> user = new SimpleObjectProperty<>();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.userPhoneLabel.textProperty().bind(Bindings.createStringBinding(() -> {return (user.get() != null? user.get().getFirstName()+ " " + user.get().getLastName():"UnKnown");},this.user));
        this.userPhoneLabel.textProperty().bind(Bindings.createStringBinding(() -> {return (user.get() != null ? user.get().getPhoneNumber():"UnKnown");},this.user));
        this.user.addListener((observableValue, oldVal, newVal) -> {
            Task<Image> task = new Task<Image>() {
                @Override
                protected Image call() throws Exception {
                    File file = new File(FilesHelper.getFilePath(FilesHelper.fileType.IMAGE)+ "/" + newVal.getProfilePictureUrl());
                    if(file.exists() && file.isFile())
                        return new Image(file.getAbsolutePath());
                    return null;
                }
            };
            task.setOnSucceeded(abc->{
                if(task.getValue() != null)
                    this.userImage.setImage(task.getValue());
            });
            Executor.submit(task);
        });
    }
    public void setData(ObjectProperty<User> usr){
        this.user.bind(usr);
        Task<Image> task = new Task<Image>() {
            @Override
            protected Image call() throws Exception {
                File file = new File(FilesHelper.getFilePath(FilesHelper.fileType.IMAGE)+ "/" + user.get().getProfilePictureUrl());
                if(file.exists() && file.isFile())
                    return new Image(file.getAbsolutePath());
                return null;
            }
        };
        task.setOnSucceeded(abc->{
            if(task.getValue() != null)
                this.userImage.setImage(task.getValue());
        });
        Executor.submit(task);
    }

    public CheckBox getCheckedItem(){
        return this.checkedItem;
    }

    public ObjectProperty<User> getUserProperty(){
        return user;
    }

    @Override
    public StackPane getView() {
        return mainContainer;
    }

    public void handleVideoCallButtonAction(ActionEvent event) {
        Task<Response> startCall = new Task<Response>() {
            @Override
            protected Response call() throws Exception {
                try {
                    Response response = ChatClient.getInstance().initiateVideoCall(String.valueOf(user.get().getId()));
                    if(response.isSuccess()){
                        Navigation.loadPage("VideoCallView.fxml");
                    }else{
                        JOptionPane.showMessageDialog(null,"Cannot Start The Video Call Error : "+response.getMessage());
                    }
                    return response;
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,"Error: "+e.getMessage());
                    return new Response(false,e.getMessage(),null);
                }
            }
        };
        startCall.setOnSucceeded(abc->{
            if(startCall.getValue().isSuccess()){
                soutt("Success Calling...........");
            }else{
                serrr("Error In Calling E-MSG: "+startCall.getValue().getMessage());
            }
        });
        Executor.submit(startCall);
    }
    public static void soutt(String msg){
        System.out.println("----- ["+Thread.currentThread().getName()+"][UserCardController] : "+ msg);
    }
    public static void serrr(String msg){
        System.err.println("----- ["+Thread.currentThread().getName()+"][UserCardController] : "+ msg);
    }
    public void handleAudioCallButtonAction(ActionEvent event) {

    }

    public interface OnClickListener{
        public void onClick();
    }
    private OnClickListener listener;

    public void setOnClickListener(OnClickListener listener){
        this.listener = listener;
    }

    public void handleItemClicked(MouseEvent event) {
        if(listener != null)
            listener.onClick();
    }
}
