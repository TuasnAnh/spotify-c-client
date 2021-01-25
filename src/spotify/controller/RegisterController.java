package spotify.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Paths;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import spotify.utils.StringUtils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author ADMIN
 */
public class RegisterController {

    private Socket clientSocket;
    private String host;
    private int port;
    private DataInputStream serverRes;
    private DataOutputStream serverReq;
    private String validateErrorType;
    private boolean onRegister;

    @FXML
    private TextField usernameTxt, passwordTxt, repasswordTxt, accountNameTxt, dobTxt, cityTxt, phoneTxt, emailTxt;

    @FXML
    private Button registerButton;

    @FXML
    private Label errorAlert;

    @FXML
    private void register(ActionEvent event) throws IOException {
        System.out.println("register");
        if (!onRegister) {
            onRegister = true;
            String username = usernameTxt.getText();
            String password = passwordTxt.getText();
            String repassword = repasswordTxt.getText();
            String accountName = accountNameTxt.getText();
            String dob = dobTxt.getText();
            String city = cityTxt.getText();
            String phone = phoneTxt.getText();
            String email = emailTxt.getText();

            boolean checkCon = checkCondition(username, password, repassword, accountName, dob, city, phone, email);
            if (checkCon) {
                try {
                    clientSocket = new Socket(host, port);
                    serverRes = new DataInputStream(clientSocket.getInputStream());
                    serverReq = new DataOutputStream(clientSocket.getOutputStream());

                    serverReq.writeBytes("register");
                    serverReq.writeByte('\n');
                    serverReq.writeBytes(username);
                    serverReq.writeByte('\n');

                    String check = serverRes.readLine();
                    if (check.equals("usernameVerified")) {
                        serverReq.writeBytes(password);
                        serverReq.writeByte('\n');
                        serverReq.writeBytes(accountName);
                        serverReq.writeByte('\n');
                        serverReq.writeBytes(dob);
                        serverReq.writeByte('\n');
                        serverReq.writeBytes(city);
                        serverReq.writeByte('\n');
                        serverReq.writeInt(Integer.parseInt(phone));
                        serverReq.writeBytes(email);
                        serverReq.writeByte('\n');

                        if (serverRes.readBoolean()) {
                            goToLoginView();
                        } else {
                            System.out.println("Failed insert to database");
                            errorAlert.setText("Failed insert to database");
                        }
                    }

                    clientSocket.close();
                    serverReq.close();
                    serverRes.close();
                } catch (IOException e) {
                    System.out.println("Socket Error!");
                }

            } else {
                errorAlert.setText(validateErrorType);
            }
            System.out.println("End Register");

            // finish register process
            onRegister = false;
        }

    }

    public void initialize(String host, int port, boolean onRegister) {
        this.host = host;
        this.port = port;
        this.onRegister = onRegister;
    }

    private boolean checkCondition(String username, String password, String repassword, String accountName, String dob, String city, String phone, String email) {
        if (!username.isEmpty() && !password.isEmpty() && !repassword.isEmpty() && !accountName.isEmpty() && !dob.isEmpty() && !city.isEmpty() && !phone.isEmpty() && !email.isEmpty()) {
            if (password.equals(repassword)) {
                if (StringUtils.isNumberic(phone)) {
                    return true;
                } else {
                    validateErrorType = "Phone must be number";
                }
            } else {
                validateErrorType = "Unequal password and repassword";
            }
        } else {
            validateErrorType = "empty spot";
        }
        return false;
    }

    @FXML
    private void goToLoginView() throws IOException {
        Stage stage = (Stage) registerButton.getScene().getWindow();

        URL url = Paths.get("src/spotify/view/ViewLogin.fxml").toUri().toURL();
        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        LoginController loginController = loader.<LoginController>getController();
        loginController.initialize(host, port);

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
}
