/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotify.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import spotify.Bean.Song;
import spotify.MusicPlayer.MusicPlayer;
import spotify.MusicPlayer.OfflineMusicPlayer;
import spotify.thread.AddToRecentPlay;
import spotify.utils.StringUtils;

/**
 *
 * @author ADMIN
 */
public class SearchViewController {

    private Socket clientSocket;
    private Socket addPlaylistSocket;
    private Socket downloadSocket;
    private List<Song> listSongSearched;
    private List<Integer> searchedSongid;
    private int userId;
    int port;
    String host;
    MusicPlayer musicPlayer;
    OfflineMusicPlayer offMusicPlayer;
    private homeViewController homeController;

    @FXML
    private TextField searchField;

    @FXML
    private VBox searchFieldVbox;

    public void initialize(String host, int port, int userId, homeViewController homeController, MusicPlayer musicPlayer, OfflineMusicPlayer offMusicPlayer) throws IOException {
        this.userId = userId;
        this.host = host;
        this.port = port;
        this.homeController = homeController;
        this.musicPlayer = musicPlayer;
        this.offMusicPlayer = offMusicPlayer;

        searchField.setOnKeyPressed((KeyEvent ke) -> {
            Platform.runLater(() -> {
                searchFieldVbox.getChildren().clear();
            });

            if (!searchField.getText().equals("")) {
                if (ke.getCode().equals(KeyCode.ENTER)) {
                    System.out.println(searchField.getText());
                    new GetSearchedSong().start();
                }
            }
        });
    }

    public Pane createSearchCard(String title, String author, double time, int songID, List<Integer> playlist) throws FileNotFoundException {
        Pane pane = new Pane();
        pane.setStyle("-fx-border-style: hidden hidden solid hidden;" + "-fx-border-width: 1;" + "-fx-border-color: black;");
        pane.setMinSize(895, 47);
        pane.setMaxSize(895, 47);
        pane.setPrefWidth(200);
        pane.setPrefHeight(200);

        Label authorLabel = new Label(author);
        authorLabel.setStyle("-fx-font-size: 16px;");
        authorLabel.setMinSize(160, 40);
        authorLabel.setMaxSize(160, 40);
        authorLabel.setLayoutX(35);
        authorLabel.setLayoutY(4);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px;");
        titleLabel.setMinSize(275, 40);
        titleLabel.setMaxSize(275, 40);
        titleLabel.setLayoutX(212);
        titleLabel.setLayoutY(4);

        Label timeLabel = new Label(StringUtils.doubleToTimeDuration(time));
        timeLabel.setStyle("-fx-font-size: 16px;");
        timeLabel.setMinSize(83, 40);
        timeLabel.setMaxSize(83, 40);
        timeLabel.setLayoutX(431);
        timeLabel.setLayoutY(4);

        Button playButton = new Button();
        playButton.setCursor(Cursor.HAND);
        playButton.setStyle("-fx-background-color: #F5F8FF;" + "-fx-background-radius: 100%;");
        playButton.setLayoutX(554);
        playButton.setLayoutY(4);
        playButton.setMinSize(40, 40);
        playButton.setMaxSize(40, 40);
        playButton.setOnMousePressed((MouseEvent event) -> {
            try {
                offMusicPlayer.stopPlaying();
                musicPlayer.setSong(songID, playlist, false);
                new AddToRecentPlay(host, port, userId, songID).start();
            } catch (IOException ex) {
                Logger.getLogger(SearchViewController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        ImageView imgView = new ImageView();
        imgView.setImage(new Image(new FileInputStream(new File("src/icons/play-icon.png"))));
        imgView.setFitWidth(28);
        imgView.setFitHeight(28);
        playButton.setGraphic(imgView);

        Button addToPlaylistButton = new Button();
        addToPlaylistButton.setCursor(Cursor.HAND);
        addToPlaylistButton.setStyle("-fx-background-color: #F5F8FF;" + "-fx-background-radius: 100%;");
        addToPlaylistButton.setLayoutX(695);
        addToPlaylistButton.setLayoutY(4);
        addToPlaylistButton.setMinSize(40, 40);
        addToPlaylistButton.setMaxSize(40, 40);
        addToPlaylistButton.setOnMousePressed((MouseEvent event) -> {
            new AddToPlaylist(songID).start();
        });
        ImageView imgView2 = new ImageView();
        imgView2.setImage(new Image(new FileInputStream(new File("src/icons/plus.png"))));
        imgView2.setFitWidth(28);
        imgView2.setFitHeight(28);
        addToPlaylistButton.setGraphic(imgView2);

        Button downloadMusic = new Button();
        downloadMusic.setCursor(Cursor.HAND);
        downloadMusic.setStyle("-fx-background-color: #F5F8FF;" + "-fx-background-radius: 100%;");
        downloadMusic.setLayoutX(825);
        downloadMusic.setLayoutY(4);
        downloadMusic.setMinSize(40, 40);
        downloadMusic.setMaxSize(40, 40);
        downloadMusic.setOnMousePressed((MouseEvent event) -> {
            new DownloadMusic(songID).start();
        });

        ImageView imgView3 = new ImageView();
        imgView3.setImage(new Image(new FileInputStream(new File("src/icons/download.png"))));
        imgView3.setFitWidth(28);
        imgView3.setFitHeight(28);
        downloadMusic.setGraphic(imgView3);

        pane.getChildren().addAll(authorLabel, titleLabel, timeLabel, playButton, addToPlaylistButton, downloadMusic);
        return pane;
    }

    class GetSearchedSong extends Thread {

        @Override
        public void run() {
            try {
                clientSocket = new Socket(host, port);
                try (DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
                        DataInputStream dis = new DataInputStream(clientSocket.getInputStream());) {

                    dos.writeBytes("search");
                    dos.writeByte('\n');
                    dos.writeBytes(searchField.getText());
                    dos.writeByte('\n');

                    int numOfSong = dis.readInt();
                    System.out.println("search number: " + numOfSong);
                    if (numOfSong > 0) {
                        listSongSearched = new ArrayList<>();
                        searchedSongid = new ArrayList<>();
                        for (int i = 0; i < numOfSong; i++) {
                            int songId = dis.readInt();
                            String name = dis.readUTF();
                            String author = dis.readUTF();
                            Double time = dis.readDouble();
                            listSongSearched.add(new Song(songId, name, author, time));
                            searchedSongid.add(songId);
                        }

                        Platform.runLater(() -> {
                            listSongSearched.forEach(e -> {
                                try {
                                    searchFieldVbox.getChildren().add(createSearchCard(e.getName(), e.getArtist(), e.getTime(), e.getSongId(), searchedSongid));
                                } catch (FileNotFoundException ex) {
                                    Logger.getLogger(SearchViewController.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            });
                        });
                    }
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

    class AddToPlaylist extends Thread {

        private final int songId;

        public AddToPlaylist(int songId) {
            this.songId = songId;
        }

        @Override
        public void run() {
            try {
                addPlaylistSocket = new Socket(host, port);
                try (DataOutputStream dos = new DataOutputStream(addPlaylistSocket.getOutputStream());) {
                    dos.writeBytes("addToPlaylist");
                    dos.writeByte('\n');
                    dos.writeInt(userId);
                    dos.writeInt(songId);

                }
            } catch (IOException ex) {
                System.out.println("Error socket");
                Logger.getLogger(SearchViewController.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    addPlaylistSocket.close();
                } catch (IOException ex) {
                    Logger.getLogger(SearchViewController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }

    class DownloadMusic extends Thread {

        private final int songId;

        public DownloadMusic(int songId) {
            this.songId = songId;
        }

        @Override
        public void run() {
            try {
                String folder = "download";

                downloadSocket = new Socket(host, port);
                DataOutputStream dos = new DataOutputStream(downloadSocket.getOutputStream());

                System.out.println("sending");

                dos.writeBytes("downloadmusic");
                dos.writeByte('\n');
                dos.writeInt(userId);
                dos.writeInt(songId);

                //download file
                DataInputStream inputStream = new DataInputStream(new BufferedInputStream(downloadSocket.getInputStream()));

                int bufferSize = 8192;
                byte[] buf = new byte[bufferSize];

                String fileDir = folder + "\\" + inputStream.readUTF();
                try (DataOutputStream fileOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileDir)))) {
                    System.out.println("Start receiving files!" + "\n");

                    while (true) {
                        int read = 0;
                        if (inputStream != null) {
                            read = inputStream.read(buf);
                        }
                        if (read == -1) {
                            break;
                        }
                        fileOut.write(buf, 0, read);
                    }
                    System.out.println("Receive completed, file saved as" + fileDir + "\n");
                }
            } catch (IOException ex) {
                System.out.println("Error");
                Logger.getLogger(SearchViewController.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    downloadSocket.close();
                } catch (IOException ex) {
                    Logger.getLogger(SearchViewController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
