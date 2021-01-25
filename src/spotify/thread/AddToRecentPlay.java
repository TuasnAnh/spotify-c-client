/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotify.thread;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import spotify.controller.SearchViewController;

/**
 *
 * @author ADMIN
 */
public class AddToRecentPlay extends Thread {

    private Socket clientSocket;
    private final String host;
    private final int port;
    private final int userId;
    private final int songId;

    public AddToRecentPlay(String host, int port, int userId, int songId) {
        this.host = host;
        this.port = port;
        this.userId = userId;
        this.songId = songId;
    }


    @Override
    public void run() {
       try {
                clientSocket = new Socket(host, port);
                try (DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
                        DataInputStream dis = new DataInputStream(clientSocket.getInputStream());) {

                    dos.writeBytes("addRecentplay");
                    dos.writeByte('\n');
                    dos.writeInt(userId);
                    dos.writeInt(songId);

                }
            } catch (IOException ex) {
                Logger.getLogger(SearchViewController.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException ex) {
                    Logger.getLogger(SearchViewController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
    }
}
