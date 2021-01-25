/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotify.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
public class PlaylistViewController {

    private Socket clientSocket;
    private Socket removePlSocket;
    private List<Song> playlist;
    private List<Integer> listSongid;
    private int userId;
    int port;
    String host;
    homeViewController homeController;
    MusicPlayer musicPlayer;
    OfflineMusicPlayer offMusicPlayer;

    @FXML
    private VBox PlaylistViewBox;

    public void initialize(String host, int port, int userId, homeViewController homeController, MusicPlayer musicPlayer, OfflineMusicPlayer offMusicPlayer) throws IOException {
        this.userId = userId;
        this.host = host;
        this.port = port;
        this.homeController = homeController;
        this.musicPlayer = musicPlayer;
        this.offMusicPlayer = offMusicPlayer;
        new GetPlaylist().start();
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
        timeLabel.setLayoutX(526);
        timeLabel.setLayoutY(4);

        Button playButton = new Button();
        playButton.setCursor(Cursor.HAND);
        playButton.setStyle("-fx-background-color: #F5F8FF;" + "-fx-background-radius: 100%;");
        playButton.setLayoutX(666);
        playButton.setLayoutY(4);
        playButton.setMinSize(40, 40);
        playButton.setMaxSize(40, 40);
        playButton.setOnMousePressed((MouseEvent event) -> {
            try {
                offMusicPlayer.stopPlaying();
                musicPlayer.setSong(songID, playlist, false);
                new AddToRecentPlay(host, port, userId, songID).start();
            } catch (IOException ex) {
                Logger.getLogger(PlaylistViewController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        ImageView imgView = new ImageView();
        imgView.setImage(new Image(new FileInputStream(new File("src/icons/play-icon.png"))));
        imgView.setFitWidth(28);
        imgView.setFitHeight(30);
        playButton.setGraphic(imgView);

        Button addToPlaylistButton = new Button();
        addToPlaylistButton.setCursor(Cursor.HAND);
        addToPlaylistButton.setStyle("-fx-background-color: #F5F8FF;" + "-fx-background-radius: 100%;");
        addToPlaylistButton.setLayoutX(794);
        addToPlaylistButton.setLayoutY(4);
        addToPlaylistButton.setMinSize(40, 40);
        addToPlaylistButton.setMaxSize(40, 40);
        addToPlaylistButton.setOnMousePressed((MouseEvent event) -> {
            new RemoveFromPlaylist(songID).start();
        });
        ImageView imgView2 = new ImageView();
        imgView2.setImage(new Image(new FileInputStream(new File("src/icons/x.png"))));
        imgView2.setFitWidth(20);
        imgView2.setFitHeight(20);
        addToPlaylistButton.setGraphic(imgView2);

        pane.getChildren().addAll(authorLabel, titleLabel, timeLabel, playButton, addToPlaylistButton);
        return pane;
    }

    class GetPlaylist extends Thread {

        @Override
        public void run() {
            try {
                clientSocket = new Socket(host, port);
                try (DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
                        DataInputStream dis = new DataInputStream(clientSocket.getInputStream());) {

                    dos.writeBytes("getPlaylist");
                    dos.writeByte('\n');
                    dos.writeInt(userId);

                    int numOfSong = dis.readInt();
                    System.out.println("playlist number: " + numOfSong);
                    if (numOfSong > 0) {
                        playlist = new ArrayList<>();
                        listSongid = new ArrayList<>();
                        for (int i = 0; i < numOfSong; i++) {
                            int songId = dis.readInt();
                            String name = dis.readUTF();
                            String author = dis.readUTF();
                            Double time = dis.readDouble();
                            playlist.add(new Song(songId, name, author, time));
                            listSongid.add(songId);
                        }

                        Platform.runLater(() -> {
                            playlist.forEach(e -> {
                                try {
                                    PlaylistViewBox.getChildren().add(createSearchCard(e.getName(), e.getArtist(), e.getTime(), e.getSongId(), listSongid));
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

    class RemoveFromPlaylist extends Thread {

        private final int songId;

        public RemoveFromPlaylist(int songId) {
            this.songId = songId;
        }

        @Override
        public void run() {
            try {
                removePlSocket = new Socket(host, port);
                try (DataOutputStream dos = new DataOutputStream(removePlSocket.getOutputStream());) {
                    System.out.println("sending");
                    dos.writeBytes("removeFromPlaylist");
                    dos.writeByte('\n');
                    dos.writeInt(userId);
                    dos.writeInt(songId);

                }
            } catch (IOException ex) {
                System.out.println("Error socket");
                Logger.getLogger(SearchViewController.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    removePlSocket.close();
                } catch (IOException ex) {
                    Logger.getLogger(SearchViewController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }
}
