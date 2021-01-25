/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotify.controller;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import spotify.Bean.Song;
import spotify.MusicPlayer.MusicPlayer;
import spotify.MusicPlayer.OfflineMusicPlayer;
import spotify.thread.AddToRecentPlay;

/**
 *
 * @author ADMIN
 */
public class HomeContentViewController {

    private Socket suggestSocket;
    private Socket recentSocket;
    private List<Song> listSuggestion = null;
    private List<Song> listRecentPlay = null;
    private List<Integer> listSuggestSongId;
    private List<Integer> listRecentSongId;
    private int userId;
    private homeViewController homeController;
    private String host;
    private int port;
    private MusicPlayer musicPlayer;
    OfflineMusicPlayer offMusicPlayer;

    @FXML
    GridPane suggestGridPane;

    @FXML
    GridPane RecentplayGridPane;

    public void initialize(String host, int port, int userId, homeViewController controller, MusicPlayer musicPlayer, OfflineMusicPlayer offMusicPlayer) throws IOException {
        System.out.println("new Init");
        suggestSocket = new Socket(host, port);
        recentSocket = new Socket(host, port);
        this.offMusicPlayer = offMusicPlayer;
        this.host = host;
        this.port = port;
        this.homeController = controller;
        this.userId = userId;
        this.musicPlayer = musicPlayer;
        new GetSuggestion().start();
        new GetRecentplay().start();
    }

    private Pane createCardContent(String title, String author, int SongID, List<Integer> playlist) throws FileNotFoundException {
        Pane pane = new Pane();
        pane.setCursor(Cursor.HAND);
        pane.setEffect(new DropShadow());
        pane.setStyle("-fx-background-color: #fff;" + "-fx-background-radius: 20px;");
        pane.setMinSize(190, 98);
        pane.setMaxSize(200, 98);
        pane.setPrefWidth(200);
        pane.setPrefHeight(200);
        ImageView imgView = new ImageView();
        imgView.setLayoutX(14);
        imgView.setLayoutY(22);
        imgView.setFitWidth(54);
        imgView.setFitHeight(54);
        new GetAlbumCover(SongID, imgView).start();

        String displayTitle;
        if (title.length() > 12) {
            displayTitle = title.substring(0, 12) + "..";
        } else {
            displayTitle = title;
        }
        Label titleLabel = new Label(displayTitle);
        titleLabel.setStyle("-fx-font-weight: bold;" + "-fx-font-size: 15px;");
        titleLabel.setLayoutX(81);
        titleLabel.setLayoutY(21);

        String displayAuthor;
        if (author.length() > 12) {
            displayAuthor = author.substring(0, 12) + "..";
        } else {
            displayAuthor = author;
        }
        Label authorLabel = new Label(displayAuthor);
        authorLabel.setLayoutX(81);
        authorLabel.setLayoutY(50);
        authorLabel.setStyle("-fx-font-size: 15px");

        pane.getChildren().addAll(imgView, titleLabel, authorLabel);

        pane.setOnMousePressed((MouseEvent event) -> {
            try {
                offMusicPlayer.stopPlaying();
                musicPlayer.setSong(SongID, playlist, false);
                new AddToRecentPlay(host, port, userId, SongID).start();
//            System.out.println(SongID);
            } catch (IOException ex) {
                Logger.getLogger(HomeContentViewController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        return pane;
    }

    class GetRecentplay extends Thread {

        @Override
        public void run() {
            try {
                try (DataOutputStream dos = new DataOutputStream(recentSocket.getOutputStream());
                        DataInputStream dis = new DataInputStream(recentSocket.getInputStream());) {
                    dos.writeBytes("getRecentplay");
                    dos.writeByte('\n');
                    dos.writeInt(userId);

                    int numOfSong = dis.readInt();
                    System.out.println("numofsong: " + numOfSong);
                    if (numOfSong > 0) {
                        listRecentPlay = new ArrayList<>();
                        listRecentSongId = new ArrayList<>();
                        for (int i = 0; i < numOfSong; i++) {
                            int songId = dis.readInt();
                            String name = dis.readUTF();
                            String author = dis.readUTF();
                            Double time = dis.readDouble();
                            listRecentPlay.add(new Song(songId, name, author, time));
                            listRecentSongId.add(songId);
                        }

                        RecentplayGridPane.setAlignment(Pos.CENTER);
                        int rowIndex = 0;
                        int columnIndex = 0;
                        int minSize = Math.min(listRecentPlay.size(), 6);

                        for (int i = 0; i < minSize; i++) {
                            System.out.println(listRecentPlay.get(i).getName() + " " + listRecentPlay.get(i).getArtist() + " " + listRecentPlay.get(i).getTime());
                            Platform.runLater(new RunnableImpl(i, columnIndex, rowIndex));
                            columnIndex++;
                            if (columnIndex > 2) {
                                columnIndex = 0;
                                rowIndex += 1;
                            }
                        }
                    }

                    recentSocket.close();
                }
            } catch (IOException ex) {
                System.out.println("Error when get recent play list in client");
                Logger.getLogger(HomeContentViewController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        private class RunnableImpl implements Runnable {

            private final int i;
            private final int columnIndex;
            private final int rowIndex;

            public RunnableImpl(int i, int columnIndex, int rowIndex) {
                this.i = i;
                this.columnIndex = columnIndex;
                this.rowIndex = rowIndex;
            }

            @Override
            public void run() {
                try {
                    RecentplayGridPane.add(createCardContent(listRecentPlay.get(i).getName(), listRecentPlay.get(i).getArtist(), listRecentPlay.get(i).getSongId(), listRecentSongId), columnIndex, rowIndex);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(HomeContentViewController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    class GetSuggestion extends Thread {

        @Override
        public void run() {
            try {
                try (DataOutputStream dos = new DataOutputStream(suggestSocket.getOutputStream());
                        DataInputStream dis = new DataInputStream(suggestSocket.getInputStream());) {
                    dos.writeBytes("getSuggest");
                    dos.writeByte('\n');

                    int numOfSong = dis.readInt();
                    System.out.println("suggest song:" + numOfSong);
                    listSuggestion = new ArrayList<>();
                    listSuggestSongId = new ArrayList<>();
                    for (int i = 0; i < numOfSong; i++) {
                        int songId = dis.readInt();
//                        String name = dis.readLine();
//                        String author = dis.readLine();
                        String name = dis.readUTF();
                        String author = dis.readUTF();
                        Double time = dis.readDouble();
                        listSuggestion.add(new Song(songId, name, author, time));
                        listSuggestSongId.add(songId);
                    }

                    suggestGridPane.setAlignment(Pos.CENTER);
                    int rowIndex = 0;
                    int columnIndex = 0;
                    int minSize = Math.min(listSuggestion.size(), 6);

                    for (int i = 0; i < minSize; i++) {
                        Platform.runLater(new RunnableImpl(i, columnIndex, rowIndex));
                        columnIndex++;
                        if (columnIndex > 2) {
                            columnIndex = 0;
                            rowIndex += 1;
                        }
                    }

                    suggestSocket.close();
                }
            } catch (IOException ex) {
                System.out.println("Error when get suggestion list in client");
                Logger.getLogger(HomeContentViewController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        private class RunnableImpl implements Runnable {

            private final int i;
            private final int columnIndex;
            private final int rowIndex;

            public RunnableImpl(int i, int columnIndex, int rowIndex) {
                this.i = i;
                this.columnIndex = columnIndex;
                this.rowIndex = rowIndex;
            }

            @Override
            public void run() {
                try {
                    suggestGridPane.add(createCardContent(listSuggestion.get(i).getName(), listSuggestion.get(i).getArtist(), listSuggestion.get(i).getSongId(), listSuggestSongId), columnIndex, rowIndex);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(HomeContentViewController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    class GetAlbumCover extends Thread {

        private Socket imageSocket;
        private final int songId;
        private DataInputStream imgRes;
        private DataOutputStream imgReq;
        private ImageView imgView;
        private ByteArrayInputStream imgStream;

        public GetAlbumCover(int songId, ImageView imgView) {
            this.songId = songId;
            this.imgView = imgView;
        }

        @Override
        public void run() {
            try {
                imageSocket = new Socket(host, port);
                imgRes = new DataInputStream(imageSocket.getInputStream());
                imgReq = new DataOutputStream(imageSocket.getOutputStream());

                imgReq.writeBytes("getAlbumCover");
                imgReq.writeByte('\n');

                imgReq.writeInt(songId);

                int size = imgRes.readInt();
                byte[] buffer = new byte[size];
                imgRes.readFully(buffer);
                imgStream = new ByteArrayInputStream(buffer);

                imgView.setImage(new Image(imgStream));

                imageSocket.close();
                imgRes.close();
                imgReq.close();
            } catch (IOException ex) {
                Logger.getLogger(MusicPlayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
