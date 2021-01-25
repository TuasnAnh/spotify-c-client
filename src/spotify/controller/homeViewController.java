package spotify.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javazoom.jl.decoder.JavaLayerException;
import spotify.MusicPlayer.MusicPlayer;
import spotify.MusicPlayer.OfflineMusicPlayer;
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
public class homeViewController {

  private String host;
  private int port;
  private int userId;
  private homeViewController homeController;
  private MusicPlayer musicPlayer;
  private OfflineMusicPlayer offMusicPlayer;
  private boolean loop = false;
  private boolean shuffle = false;
  boolean playOnline = true;

  File playImg = new File("src/icons/play-icon.png");
  File pauseImg = new File("src/icons/pause.png");
  File shuffleImg = new File("src/icons/shuffle.png");
  File prevImg = new File("src/icons/skip-left.png");
  File nextImg = new File("src/icons/skip-right.png");
  File loopImg = new File("src/icons/repeat.png");
  File volumn = new File("src/icons/volume.png");

  private final String homePath = "src/spotify/view/HomeContentView.fxml";
  private final String searchPath = "src/spotify/view/ViewSearch.fxml";
  private final String playlistPath = "src/spotify/view/ViewPlaylist.fxml";
  private final String accountPath = "src/spotify/view/ViewAccount.fxml";
  private final String downloadPath = "src/spotify/view/ViewDownload.fxml";

  @FXML
  private Button SearchView, HomeView, PlaylistView, AccountView;

  @FXML
  private BorderPane HomePane;

  @FXML
  private Label songNameBar, authorBar, timeLabelBar, realTimeDuration;

  @FXML
  private Button pauseButton;

  @FXML
  private ImageView albumCover;

  @FXML
  private Button loopButton, shuffleButton, nextButton, preButton, volumeButton;

  @FXML
  private ProgressBar progressBar;

  @FXML
  private Button DownloadView;

  @FXML
  public void changeViewAction(ActionEvent event) throws IOException {
    Parent root;
    URL url;

    if (event.getSource() == HomeView) {
      url = Paths.get(homePath).toUri().toURL();
    } else if (event.getSource() == SearchView) {
      url = Paths.get(searchPath).toUri().toURL();
    } else if (event.getSource() == PlaylistView) {
      url = Paths.get(playlistPath).toUri().toURL();
    } else if (event.getSource() == DownloadView) {
      url = Paths.get(downloadPath).toUri().toURL();
    } else {
      url = Paths.get(accountPath).toUri().toURL();
    }

    FXMLLoader loader = new FXMLLoader(url);
    root = loader.load();
    HomePane.setCenter(root);

    if (event.getSource() == HomeView) {
      HomeContentViewController controller = loader.<HomeContentViewController>getController();
      controller.initialize(host, port, userId, homeController, musicPlayer, offMusicPlayer);
    } else if (event.getSource() == SearchView) {
      SearchViewController controller = loader.<SearchViewController>getController();
      controller.initialize(host, port, userId, homeController, musicPlayer, offMusicPlayer);
    } else if (event.getSource() == PlaylistView) {
      PlaylistViewController controller = loader.<PlaylistViewController>getController();
      controller.initialize(host, port, userId, homeController, musicPlayer, offMusicPlayer);
    } else if (event.getSource() == DownloadView) {
      DownloadViewController controller = loader.<DownloadViewController>getController();
      controller.initialize(host, port, userId, homeController, musicPlayer, offMusicPlayer);
    } else {
      AccountViewController controller = loader.<AccountViewController>getController();
      controller.initialize(host, port, userId, homeController, musicPlayer, offMusicPlayer);
    }

  }

  @FXML
  private void changePlayState() throws JavaLayerException, FileNotFoundException, IOException, LineUnavailableException {
    if (playOnline) {
      boolean playing = musicPlayer.getPlayingState();
      System.out.println("home view: " + playing);
      if (playing) {
        ImageView pimgview = new ImageView(new Image(new FileInputStream(playImg)));
        pimgview.setFitWidth(31);
        pimgview.setFitHeight(31);
        pauseButton.setGraphic(pimgview);
        musicPlayer.setPause();
      } else {
        ImageView pimgview = new ImageView(new Image(new FileInputStream(pauseImg)));
        pimgview.setFitWidth(31);
        pimgview.setFitHeight(31);
        pauseButton.setGraphic(pimgview);
        musicPlayer.setPlay();
      }
    } else {
      boolean playing = offMusicPlayer.getPlayingState();
      System.out.println("home view: " + playing);
      if (playing) {
        ImageView pimgview = new ImageView(new Image(new FileInputStream(playImg)));
        pimgview.setFitWidth(31);
        pimgview.setFitHeight(31);
        pauseButton.setGraphic(pimgview);
        offMusicPlayer.setPause();
      } else {
        ImageView pimgview = new ImageView(new Image(new FileInputStream(pauseImg)));
        pimgview.setFitWidth(31);
        pimgview.setFitHeight(31);
        pauseButton.setGraphic(pimgview);
        offMusicPlayer.setPlay();
      }
    }

  }

  @FXML
  private void setLoop() throws FileNotFoundException {
    ImageView pimgview;
    if (!loop) {
      shuffle = true;
      setShuffle();
      pimgview = new ImageView(new Image(new FileInputStream("src/icons/repeat-active.png")));
    } else {
      pimgview = new ImageView(new Image(new FileInputStream("src/icons/repeat.png")));
    }
    loop = !loop;
    musicPlayer.setLoop(loop);
    offMusicPlayer.setLoop(loop);
    pimgview.setFitWidth(31);
    pimgview.setFitHeight(31);
    loopButton.setGraphic(pimgview);
  }

  @FXML
  private void setShuffle() throws FileNotFoundException {
    ImageView pimgview;
    if (!shuffle) {
      loop = true;
      setLoop();
      pimgview = new ImageView(new Image(new FileInputStream("src/icons/shuffle-active.png")));
    } else {
      pimgview = new ImageView(new Image(new FileInputStream("src/icons/shuffle.png")));
    }
    shuffle = !shuffle;
    musicPlayer.setShuffle(shuffle);
    offMusicPlayer.setShuffle(shuffle);
    pimgview.setFitWidth(31);
    pimgview.setFitHeight(31);
    shuffleButton.setGraphic(pimgview);
  }

  @FXML
  private void setNextSong() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
    if (playOnline) {
      musicPlayer.nextSong();
    } else {
      offMusicPlayer.nextSong();
    }
  }

  @FXML
  private void setPreSong() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
    if (playOnline) {
      musicPlayer.preSong();
    } else {
      offMusicPlayer.preSong();
    }
  }

  public void initialize(String host, int port, int userId, homeViewController controller) throws IOException {
    this.host = host;
    this.port = port;
    this.userId = userId;
    this.homeController = controller;
    musicPlayer = new MusicPlayer(host, port, homeController);
    offMusicPlayer = new OfflineMusicPlayer(homeController);

    ImageView pimgview = new ImageView(new Image(new FileInputStream(pauseImg)));
    pimgview.setFitWidth(31);
    pimgview.setFitHeight(31);
    pauseButton.setGraphic(pimgview);

    ImageView pimgview2 = new ImageView(new Image(new FileInputStream(shuffleImg)));
    pimgview2.setFitWidth(31);
    pimgview2.setFitHeight(31);
    shuffleButton.setGraphic(pimgview2);

    ImageView pimgview3 = new ImageView(new Image(new FileInputStream(prevImg)));
    pimgview3.setFitWidth(31);
    pimgview3.setFitHeight(31);
    preButton.setGraphic(pimgview3);

    ImageView pimgview4 = new ImageView(new Image(new FileInputStream(nextImg)));
    pimgview4.setFitWidth(31);
    pimgview4.setFitHeight(31);
    nextButton.setGraphic(pimgview4);

    ImageView pimgview5 = new ImageView(new Image(new FileInputStream(loopImg)));
    pimgview5.setFitWidth(31);
    pimgview5.setFitHeight(31);
    loopButton.setGraphic(pimgview5);

    ImageView pimgview6 = new ImageView(new Image(new FileInputStream(volumn)));
    pimgview6.setFitWidth(31);
    pimgview6.setFitHeight(31);
    volumeButton.setGraphic(pimgview6);

    setMainContent();
  }

  public void setMainContent() throws MalformedURLException, IOException {
    FXMLLoader loader = new FXMLLoader(Paths.get(homePath).toUri().toURL());
    Parent root = loader.load();
    HomePane.setCenter(root);

    HomeContentViewController controller = loader.<HomeContentViewController>getController();
    controller.initialize(host, port, userId, homeController, musicPlayer, offMusicPlayer);
  }

  public void setLabel(String songName, String author, Double time) {
    Platform.runLater(() -> {
      songNameBar.setText(songName);
      authorBar.setText(author);
      timeLabelBar.setText(StringUtils.doubleToTimeDuration(time));
    });
  }

  public void setAlbumCover(ByteArrayInputStream imgStream) {
    Platform.runLater(() -> {
      albumCover.setImage(new Image(imgStream));
    });
  }

  public void setDuration(double time) {
    Platform.runLater(() -> {
      realTimeDuration.setText(StringUtils.doubleToTimeDuration(time));
    });
  }

  public void setProgessBar(Double progress) {
    Platform.runLater(() -> {
      progressBar.setProgress(progress);
    });
  }

  public void setPlayOnline() {
    this.playOnline = true;
  }

  public void setPlayOffline() {
    this.playOnline = false;
  }

  public void setDefaultCover() {
    Platform.runLater(() -> {
      try {
        albumCover.setImage(new Image(new FileInputStream("src/icons/spotify.jpg")));
      } catch (FileNotFoundException ex) {
        Logger.getLogger(homeViewController.class.getName()).log(Level.SEVERE, null, ex);
      }
    });
  }
}
