package orgs.tuasl_clint.controllers;

import com.google.gson.Gson;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import orgs.tuasl_clint.client.ChatClient;
import orgs.tuasl_clint.models2.FactoriesSQLite.UserFactory;
import orgs.tuasl_clint.models2.User;
import orgs.tuasl_clint.models2.UserInfo;
import orgs.tuasl_clint.protocol.Response;
import orgs.tuasl_clint.utils.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    public Button login_using_data_last_btn;
    public VBox mainContainer;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label loginMessage;
    @FXML private CheckBox saveData;
    private String usernameInput, passwordInput;
    private Gson gson = ChatClient.getInstance().getGson();

    @FXML
    private void handleLoginButtonAction(ActionEvent event) {
        this.usernameInput = usernameField.getText();
        this.passwordInput = passwordField.getText();
        tryLogin(this.usernameInput,this.passwordInput);
    }
    @FXML
    private void handleRegisterButtonAction(ActionEvent event) {
        Navigation.loadPage("registration.fxml");
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        UserInfo.userInfo = new UserInfo();
        try {
            UserInfo.userInfo.getFirst();
            if(UserInfo.userInfo != null && UserInfo.userInfo.getPhone() != null && UserInfo.userInfo.getPassword() != null) {
                login_using_data_last_btn.setText("Login Using "+UserInfo.userInfo.getPhone() + " Account");
                login_using_data_last_btn.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        tryLogin(UserInfo.userInfo.getPhone(),UserInfo.userInfo.getPassword());
                    }
                });
            }else {
                mainContainer.getChildren().remove(login_using_data_last_btn);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private void tryLogin(String phone, String password){
        User userFromDb = UserFactory.findByPhoneAndPassword(phone,password);
        Response response = ChatClient.getInstance().login(phone,password);
        if(response != null && response.isSuccess()){
            User userFromServer = ChatClient.getInstance().getGson().fromJson(response.getData(),User.class);
            if(areEqalsUsers(userFromDb,userFromServer)){
                System.out.println("----- User From DB     : "+userFromDb.toString());
                System.out.println("----- User From Server :"+userFromServer.toString());
                User.user = userFromDb;
                sucessLogin();
            }else {
                DatabaseConnectionSQLite.DeleteData();
                userFromServer.setPassword(password);
                User.user = userFromServer;
                sucessLogin();
            }
        }else {
            if(response != null)
                loginMessage.setText(response.getMessage());
            else
                System.err.println("----- An Error Occurred While Trying To Login....!!");
        }
    }
    private void sucessLogin() {
       try {
           User.user.saveOrUpdate();
           if(saveData.isSelected()){
                   if(UserInfo.userInfo != null){
                       UserInfo.DeleteAll();
                       UserInfo.userInfo = new UserInfo(User.user.getPhoneNumber(),User.user.getPassword());
                       UserInfo.userInfo.setUser_id(1);
                       UserInfo.userInfo.setIsEnabled(1);
                       UserInfo.userInfo.save();
                   }else {
                       UserInfo.userInfo.setPhone(User.user.getPhoneNumber());
                       UserInfo.userInfo.setPassword(User.user.getPassword());
                       UserInfo.userInfo.update();
                   }
           }
       } catch (SQLException e) {
           System.err.println("----- Error While Trying To Save User Data : "+e.getMessage());
           e.printStackTrace();
       }
       Navigation.loadPage("chat.fxml");
    }
    private boolean areEqalsUsers(User userFromDatabase, User userFromServer) {
        if(userFromDatabase == null || userFromServer == null)
            return false;
        return  userFromDatabase.getId() == userFromServer.getId();
    }
}