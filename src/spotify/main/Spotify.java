/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotify.main;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import spotify.controller.LoginController;

/**
 *
 * @author ADMIN
 */
public class Spotify extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
//            String host = "127.0.0.1";
            String host = "192.168.43.186";
            int port = 8888;

            URL url = Paths.get("src/spotify/view/ViewLogin.fxml").toUri().toURL();
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            LoginController loginController = loader.<LoginController>getController();
            loginController.initialize(host, port);

            primaryStage.setOnCloseRequest((WindowEvent e) -> {
                Platform.exit();
                System.exit(0);
            });
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
