/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotify.controller;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import spotify.Bean.Song;
import spotify.MusicPlayer.MusicPlayer;
import spotify.MusicPlayer.OfflineMusicPlayer;
import spotify.utils.StringUtils;

/**
 *
 * @author ADMIN
 */
public class DownloadViewController {

    private List<String> listPath;
    private int userId;
    int port;
    String host;
    homeViewController homeController;
    MusicPlayer musicPlayer;
    OfflineMusicPlayer offMusicPlayer;

    @FXML
    private VBox DownloadViewBox;

    public void initialize(String host, int port, int userId, homeViewController homeController, MusicPlayer musicPlayer, OfflineMusicPlayer offMusicPlayer) throws IOException {
        this.userId = userId;
        this.host = host;
        this.port = port;
        this.homeController = homeController;
        this.musicPlayer = musicPlayer;
        this.offMusicPlayer = offMusicPlayer;
        new GetDownloadMusic().start();
    }

    public Pane createDownloadCard(String title, String author, double time, String filePath, List<Song> listSong) throws FileNotFoundException {
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
        authorLabel.setLayoutX(32);
        authorLabel.setLayoutY(4);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px;");
        titleLabel.setMinSize(275, 40);
        titleLabel.setMaxSize(275, 40);
        titleLabel.setLayoutX(203);
        titleLabel.setLayoutY(4);

        Label timeLabel = new Label(StringUtils.doubleToTimeDuration(time));
        timeLabel.setStyle("-fx-font-size: 16px;");
        timeLabel.setMinSize(83, 40);
        timeLabel.setMaxSize(83, 40);
        timeLabel.setLayoutX(563);
        timeLabel.setLayoutY(4);

        Button playButton = new Button();
        playButton.setCursor(Cursor.HAND);
        playButton.setStyle("-fx-background-color: #F5F8FF;" + "-fx-background-radius: 100%;");
        playButton.setLayoutX(773);
        playButton.setLayoutY(4);
        playButton.setMinSize(40, 40);
        playButton.setMaxSize(40, 40);
        playButton.setOnMousePressed((MouseEvent event) -> {
            try {
                musicPlayer.stopPlaying();
                offMusicPlayer.setSong(filePath, listPath, listSong, false);
            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
                Logger.getLogger(DownloadViewController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        ImageView imgView = new ImageView();
        imgView.setImage(new Image(new FileInputStream(new File("src/icons/play-icon.png"))));
        imgView.setFitWidth(28);
        imgView.setFitHeight(30);
        playButton.setGraphic(imgView);

        pane.getChildren().addAll(authorLabel, titleLabel, timeLabel, playButton);
        return pane;
    }

    class GetDownloadMusic extends Thread {

        @Override
        public void run() {
            try {
                String fileDir = "download/";
                File folder = new File(fileDir);
                File[] listOfFiles = folder.listFiles();
                listPath = new ArrayList<>();
                List<Song> listSong = new ArrayList<>();

                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        listPath.add(fileDir + file.getName());
                    }
                }

                for (int i = 0; i < listPath.size(); i++) {

                    String path = listPath.get(i);
                    Mp3File mp3file = new Mp3File(path);
                    System.out.println("Length of this mp3 is: " + mp3file.getLengthInSeconds() + " seconds");
                    if (mp3file.hasId3v2Tag()) {
                        ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                        System.out.println("Artist: " + id3v2Tag.getArtist());
                        System.out.println("Title: " + id3v2Tag.getTitle());
                        Song s = new Song(id3v2Tag.getArtist(), id3v2Tag.getTitle(), mp3file.getLengthInSeconds(), path);
                        listSong.add(s);
                    }
                }

                Platform.runLater(() -> {
                    listSong.forEach(e -> {
                        try {
                            DownloadViewBox.getChildren().add(createDownloadCard(e.getName(), e.getArtist(), e.getTime(), e.getSongPath(), listSong));
                        } catch (Exception ex) {
                            Logger.getLogger(SearchViewController.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    });
                });
            } catch (Exception e) {
                System.out.println("Error");
            }
        }
    }
}
