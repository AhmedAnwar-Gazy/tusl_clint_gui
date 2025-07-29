package orgs.tuasl_clint;


import javafx.application.Application;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import org.opencv.core.Core;
import orgs.tuasl_clint.client.ChatClient;
import orgs.tuasl_clint.utils.BackendThreadManager.DataModel;
import orgs.tuasl_clint.utils.DatabaseConnectionSQLite;
import orgs.tuasl_clint.utils.DiagnosticLogger;
import orgs.tuasl_clint.utils.FilesHelper;
import orgs.tuasl_clint.utils.Navigation;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;


public class MainApp extends Application {

    private Stage primaryStage;
    @Override
    public void start(Stage stage) throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ChatClient.getInstance().addOnLoginSuccessListener(user -> {
                DataModel.getInstance();
        });
        System.out.println(" ------------- Files Path is : "+ FilesHelper.getFilesRootPath());
        try(Connection conn = DatabaseConnectionSQLite.getInstance().getConnection()){
            System.out.println("Success Connect sqlite database");
        }catch (SQLException e){
            System.out.println("Connection Faild");
        }
        this.primaryStage = stage;
        this.primaryStage.setTitle("Chat Application");

        // Initialize Navigation helper
        Navigation.setMainApp(this); // Pass the instance to the helper
        Navigation.setPrimaryStage(primaryStage); // Pass the stage

        // Load the initial login screen
        Navigation.loadPage("login.fxml");

        primaryStage.setMinWidth(800); // Minimum responsive width
        primaryStage.setMaxHeight(1200);
        primaryStage.setMinHeight(600); // Minimum responsive height
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}