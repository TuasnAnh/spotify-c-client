/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotify.MusicPlayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javazoom.jl.decoder.JavaLayerException;
import spotify.Bean.Song;
import spotify.controller.homeViewController;

/**
 *
 * @author ADMIN
 */
public class OfflineMusicPlayer {

    private final homeViewController homeController;

    boolean playing = false;
    String songPath;

    AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
    SourceDataLine sourceLine;
    AudioInputStream din = null;
    int buffer = 4 * 1024;

    boolean loop = false;
    boolean shuffle = false;
    int currentSongIndex = -1;
    double duration = 0;
    int songLength = 0;
    List<String> playlist;
    List<Song> listSongInfor;

    PlayOffline playThread = null;
    AutoPlay autoPlay = null;
    TimeThread timeThread = null;

    long lastTime = 0;
    long time = 0;
    boolean skipTime = false;

    public OfflineMusicPlayer(homeViewController homeController) {
        this.homeController = homeController;
    }

    public void setSong(String path, List<String> playlist, List<Song> listSong, boolean auto) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        this.currentSongIndex = playlist.indexOf(path);
        this.duration = 0;
        this.songLength = (int) Math.floor(listSong.get(currentSongIndex).getTime());
        this.listSongInfor = listSong;
        homeController.setPlayOffline();
        homeController.setProgessBar(0.0);
        homeController.setDuration(duration);
        homeController.setLabel(listSong.get(currentSongIndex).getName(), listSong.get(currentSongIndex).getArtist(), listSong.get(currentSongIndex).getTime());
        homeController.setDefaultCover();
        songPath = path;
        this.playlist = playlist;
        File file = new File(songPath);
        AudioInputStream in = AudioSystem.getAudioInputStream(file);
        din = AudioSystem.getAudioInputStream(decodedFormat, in);
        playing = true;

        if (!auto) {
            if (autoPlay != null) {
                autoPlay.stop();
                autoPlay = null;
            }
        }
        if (playThread != null) {
            playThread.stop();
            timeThread.stop();
            if (sourceLine != null) {
                System.out.println("reset");
                sourceLine.close();
                sourceLine = null;
            }
        }

        playThread = new PlayOffline();
        playThread.start();

        timeThread = new TimeThread();
        timeThread.start();
    }

    public void stopPlaying() {
        if (autoPlay != null) {
            autoPlay.stop();
            autoPlay = null;
        }
        if (playThread != null) {
            playThread.stop();
            if (timeThread != null) {
                timeThread.stop();
            }
            if (sourceLine != null) {
                System.out.println("reset");
                sourceLine.close();
                sourceLine = null;
            }
        }
    }

    public boolean getPlayingState() {
        return playing;
    }

    public void setLoop(boolean loopStatus) {
        this.loop = loopStatus;
    }

    public void setShuffle(boolean shuffleStatus) {
        this.shuffle = shuffleStatus;
    }

    public void setPause() {
        if (playing) {
            System.out.println("pause off");
            playing = false;
            time = System.nanoTime();
            sourceLine.stop();
            skipTime = true;
        }
    }

    public void setPlay() throws JavaLayerException, IOException, LineUnavailableException {
        if (!playing) {
            System.out.println("start off");
            playing = true;
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
            sourceLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceLine.open(decodedFormat);
            sourceLine.start();
        }
    }

    public void AutoSetNextSong() throws UnsupportedAudioFileException, LineUnavailableException {
        if (shuffle) {
            int random = (int) (Math.random() * (playlist.size() - 1));
            currentSongIndex = random;
            try {
                setSong(playlist.get(currentSongIndex), playlist, listSongInfor, true);

            } catch (IOException ex) {
                Logger.getLogger(MusicPlayer.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        } else if (loop) {
            if (currentSongIndex == playlist.size() - 1) {
                currentSongIndex = 0;
            } else {
                currentSongIndex += 1;
            }
            try {
                setSong(playlist.get(currentSongIndex), playlist, listSongInfor, true);
            } catch (IOException ex) {
                Logger.getLogger(MusicPlayer.class
                        .getName()).log(Level.SEVERE, null, ex);

            }
        }
    }

    public void nextSong() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        if (playlist.size() > 0) {
            if (currentSongIndex == playlist.size() - 1) {
                currentSongIndex = 0;
            } else {
                currentSongIndex += 1;
            }
            setSong(playlist.get(currentSongIndex), playlist, listSongInfor, false);
        }
    }

    public void preSong() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        if (playlist.size() > 0) {
            if (currentSongIndex == 0) {
                currentSongIndex = playlist.size() - 1;
            } else {
                currentSongIndex -= 1;
            }
            setSong(playlist.get(currentSongIndex), playlist, listSongInfor, false);
        }
    }

    class PlayOffline extends Thread {

        public PlayOffline() throws LineUnavailableException {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
            sourceLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceLine.open(decodedFormat);
            sourceLine.start();
        }

        @Override
        public void run() {
            byte[] data = new byte[4096];
            if (sourceLine != null) {
                // Start
                sourceLine.start();
                int nBytesRead = 0, nBytesWritten = 0;
                while (nBytesRead != -1) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(OfflineMusicPlayer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (playing) {
                        try {
                            nBytesRead = din.read(data, 0, data.length);
                            if (nBytesRead != -1) {
                                nBytesWritten = sourceLine.write(data, 0, nBytesRead);
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(OfflineMusicPlayer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                // Stop
                sourceLine.drain();
                sourceLine.stop();
                sourceLine.close();
                playing = false;

                if (autoPlay != null) {
                    autoPlay.stop();
                    autoPlay = null;
                }
                if (shuffle || loop) {
                    autoPlay = new AutoPlay();
                    autoPlay.start();
                }
            }
        }

    }

    class TimeThread extends Thread {

        @Override
        public void run() {
            Double sum = 0.0;
            lastTime = System.nanoTime();

            while (duration <= songLength) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MusicPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (playing) {
                    if (!skipTime) {
                        time = System.nanoTime();
                    }
                    long elapsedTime = time - lastTime;
                    double deltaTime = (double) elapsedTime / 1_000_000_000;
                    duration += deltaTime;
                    homeController.setDuration(duration);
                    homeController.setProgessBar(duration / Double.valueOf(songLength));
                    lastTime = time;
                    if (!skipTime) {
                        lastTime = time;
                    } else {
                        lastTime = System.nanoTime();
                    }
                    skipTime = false;

                }
            }
        }
    }

    class AutoPlay extends Thread {

        @Override
        public void run() {
            try {
                AutoSetNextSong();
            } catch (UnsupportedAudioFileException | LineUnavailableException ex) {
                Logger.getLogger(OfflineMusicPlayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
