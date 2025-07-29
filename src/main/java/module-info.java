module orgs.tuasl_clint {
    requires javafx.fxml;
    requires java.desktop;
    requires emojisfx;
    requires mysql.connector.j;
    requires javafx.media;
    requires opencv;


    opens orgs.tuasl_clint to javafx.fxml;
    exports orgs.tuasl_clint;
    exports orgs.tuasl_clint.controllers;
    opens orgs.tuasl_clint.controllers to javafx.fxml;
    exports orgs.tuasl_clint.models2;
    opens orgs.tuasl_clint.models2 to javafx.fxml, com.google.gson;
    exports orgs.tuasl_clint.utils;
    opens orgs.tuasl_clint.utils to javafx.fxml;





    requires javafx.swing;
    requires com.google.gson;
    requires java.sql;
    requires com.gluonhq.richtextarea;
    requires javafx.controls;
    requires svg.salamander;
    requires org.jetbrains.annotations;


    opens orgs.tuasl_clint.protocol to com.google.gson;
    exports orgs.tuasl_clint.client;
    opens orgs.tuasl_clint.client to javafx.fxml;
    exports orgs.tuasl_clint.utils.BackendThreadManager;
    opens orgs.tuasl_clint.utils.BackendThreadManager to javafx.fxml; // Add this line


}