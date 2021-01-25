/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotify.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import spotify.MusicPlayer.MusicPlayer;
import spotify.MusicPlayer.OfflineMusicPlayer;

/**
 *
 * @author ADMIN
 */
public class AccountViewController {

    private String host;
    private int port;
    private Socket clientSocket;
    private int userId;
    homeViewController homeController;
    MusicPlayer musicPlayer;
    OfflineMusicPlayer offMusicPlayer;

    @FXML
    private Label accountName, phoneNumber, dob, city, email;

    @FXML
    private Button logout;

    @FXML
    private Button changePass;

    @FXML
    private void gotoChangePasswordView() throws IOException {
        musicPlayer.setPause();
        offMusicPlayer.setPause();
        Stage stage = (Stage) changePass.getScene().getWindow();
        URL url;
        url = Paths.get("src/spotify/view/ViewChangePass.fxml").toUri().toURL();
        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        ViewChangePassController viewChange = loader.<ViewChangePassController>getController();
        viewChange.initialize(host, port);

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    @FXML
    private void logOut() throws IOException {
        musicPlayer.setPause();
        offMusicPlayer.setPause();
        Stage stage = (Stage) logout.getScene().getWindow();
        URL url;
        url = Paths.get("src/spotify/view/ViewLogin.fxml").toUri().toURL();
        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        LoginController loginController = loader.<LoginController>getController();
        loginController.initialize(host, port);

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public void initialize(String host, int port, int userId, homeViewController homeController, MusicPlayer musicPlayer, OfflineMusicPlayer offMusicPlayer) {
        this.host = host;
        this.port = port;
        this.userId = userId;
        this.homeController = homeController;
        this.musicPlayer = musicPlayer;
        this.offMusicPlayer = offMusicPlayer;
        new GetUserInfor().start();
    }

    class GetUserInfor extends Thread {

        @Override
        public void run() {
            try {
                clientSocket = new Socket(host, port);
                try (DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
                        DataInputStream dis = new DataInputStream(clientSocket.getInputStream());) {

                    dos.writeBytes("getUserInfor");
                    dos.writeByte('\n');
                    dos.writeInt(userId);

                    String accName = dis.readUTF();
                    int phone = dis.readInt();
                    String birth = dis.readUTF();
                    String ct = dis.readUTF();
                    String em = dis.readUTF();

                    Platform.runLater(() -> {
                        accountName.setText(accName);
                        phoneNumber.setText(Integer.toString(phone));
                        dob.setText(birth);
                        city.setText(ct);
                        email.setText(em);
                    });

                }

            } catch (IOException ex) {
                Logger.getLogger(AccountViewController.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException ex) {
                    Logger.getLogger(AccountViewController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
