/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotify.MusicPlayer;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javazoom.jl.decoder.JavaLayerException;
import spotify.controller.homeViewController;

/**
 *
 * @author ADMIN
 */
public class MusicPlayer {

    private Socket clientSocket;
    private final String host;
    private final int port;
    private final homeViewController homeController;

    private int songId = -1;
    boolean playing = false;
    boolean openPlayThread = false;
    List<AudioInputStream> list = new ArrayList<>();
    AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
    SourceDataLine sourceLine;
    int buffer = 4 * 1024;

    boolean loop = false;
    boolean shuffle = false;
    int currentSongIndex = -1;
    double duration = 0;
    int songLength = 0;
    List<Integer> playList = new ArrayList<>();

    GetSong getSong = null;
    PlayThread playThread = null;
    GetAlbumCover albumCoverThread = null;
    AutoPlay autoPlay = null;
    TimeThread timeThread = null;

    long lastTime = 0;
    long time = 0;
    boolean skipTime = false;

    public MusicPlayer(String host, int port, homeViewController homeController) {
        this.host = host;
        this.port = port;
        this.homeController = homeController;
    }

    public void setSong(int Songid, List<Integer> playlist, boolean auto) throws IOException {
        this.playList = playlist;
        this.currentSongIndex = playlist.indexOf(Songid);
        this.duration = 0;
        homeController.setPlayOnline();
        homeController.setProgessBar(0.0);
        homeController.setDuration(duration);
        System.out.println("play list size: " + playlist.size() + " Current index: " + currentSongIndex);
        if (!auto) {
            if (autoPlay != null) {
                autoPlay.stop();
                autoPlay = null;
            }
        }
        if (getSong != null) {
            if (clientSocket != null) {
                clientSocket.close();
            }
            playThread.stop();
            getSong.stop();
            albumCoverThread.stop();
            timeThread.stop();
            if (sourceLine != null) {
                System.out.println("reset");
                sourceLine.close();
                sourceLine = null;
            }
            list.clear();
            playing = false;
            openPlayThread = false;
        }
        this.songId = Songid;
        getSong = new GetSong();
        getSong.start();
        albumCoverThread = new GetAlbumCover(songId);
        albumCoverThread.start();
    }

    public void stopPlaying() throws IOException {
        if (autoPlay != null) {
            autoPlay.stop();
            autoPlay = null;
        }
        if (getSong != null) {
            if (clientSocket != null) {
                clientSocket.close();
            }
            getSong.stop();
            if (playThread != null) {
                playThread.stop();
            }
            if (albumCoverThread != null) {
                albumCoverThread.stop();
            }
            if (timeThread != null) {
                timeThread.stop();
            }
            if (sourceLine != null) {
                System.out.println("reset");
                sourceLine.close();
                sourceLine = null;
            }
            list.clear();
            playing = false;
            openPlayThread = false;
        }
    }

    public void nextSong() throws IOException {
        if (playList.size() > 0) {
            if (currentSongIndex == playList.size() - 1) {
                currentSongIndex = 0;
            } else {
                currentSongIndex += 1;
            }
            setSong(playList.get(currentSongIndex), playList, false);
        }
    }

    public void preSong() throws IOException {
        if (playList.size() > 0) {
            if (currentSongIndex == 0) {
                currentSongIndex = playList.size() - 1;
            } else {
                currentSongIndex -= 1;
            }
            setSong(playList.get(currentSongIndex), playList, false);
        }
    }

    public boolean getPlayingState() {
        return playing;
    }

    public void setLoop(boolean loopStatus) {
        this.loop = loopStatus;
        System.out.println("loop: " + loop + " shuffle " + shuffle);
    }

    public void setShuffle(boolean shuffleStatus) {
        this.shuffle = shuffleStatus;
        System.out.println("loop: " + loop + " shuffle " + shuffle);
    }

    public void setPause() {
        if (playing) {
            System.out.println("pause");
            playing = false;
            time = System.nanoTime();
            sourceLine.stop();
            skipTime = true;
        }
    }

    public void setPlay() throws JavaLayerException, IOException, LineUnavailableException {
        if (!playing && list.size() > 0) {
            System.out.println("start");
            playing = true;
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
            sourceLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceLine.open(decodedFormat);
            sourceLine.start();
        }
    }

    public void AutoSetNextSong() {
        if (shuffle) {
            int random = (int) (Math.random() * (playList.size() - 1));
            currentSongIndex = random;
            try {
                setSong(playList.get(currentSongIndex), playList, true);

            } catch (IOException ex) {
                Logger.getLogger(MusicPlayer.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        } else if (loop) {
            if (currentSongIndex == playList.size() - 1) {
                currentSongIndex = 0;
            } else {
                currentSongIndex += 1;
            }
            try {
                setSong(playList.get(currentSongIndex), playList, true);

            } catch (IOException ex) {
                Logger.getLogger(MusicPlayer.class
                        .getName()).log(Level.SEVERE, null, ex);

            }
        }
    }

    class GetSong extends Thread {

        @Override
        public void run() {
            if (songId != -1 && !playing) {
                try {
                    clientSocket = new Socket(host, port);
                    DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
                    DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                    dos.writeBytes("play");
                    dos.writeByte('\n');
                    dos.writeInt(songId);

                    String songPath = in.readLine();
                    if (songPath != null) {
                        String name = in.readUTF();
                        String author = in.readUTF();
                        Double time = in.readDouble();
                        songLength = (int) Math.floor(time);
                        homeController.setLabel(name, author, time);

                        int counter = 0;

                        while (true) {
                            int size = in.readInt();
                            byte[] data = new byte[size];
                            in.readFully(data, 0, size);
                            ByteArrayInputStream bais = new ByteArrayInputStream(data);
                            AudioInputStream is = new AudioInputStream(bais, decodedFormat, size / decodedFormat.getFrameSize());
                            AudioInputStream dis = AudioSystem.getAudioInputStream(decodedFormat, is);
                            list.add(dis);
//                            System.out.println(counter++ + " " + size);
                            if (size < buffer) {
                                in.close();
                                is.close();
                                dis.close();
                                break;
                            }

                            if (!playing && !openPlayThread) {
                                playing = true;
                                openPlayThread = true;
                                playThread = new PlayThread();
                                playThread.start();

                                timeThread = new TimeThread();
                                timeThread.start();
                            }
                        }
                    } else {
                        System.out.println("Can't find song");
                    }

                    clientSocket.close();
                } catch (IOException | LineUnavailableException ex) {
                    Logger.getLogger(MusicPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    class PlayThread extends Thread {

        int count = 0;
        int count2 = 0;

        public PlayThread() throws LineUnavailableException {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
            sourceLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceLine.open(decodedFormat);
            sourceLine.start();
        }

        @Override
        public void run() {
            int index = 0;
            int nBytesRead;
            byte[] data;

            while (index < list.size()) {
                if (playing) {
                    nBytesRead = 0;
                    while (nBytesRead != -1) {
                        try {
                            data = new byte[list.get(index).available()];
                            nBytesRead = list.get(index).read(data, 0, data.length);
                            if (nBytesRead != -1) {
//                            System.out.println("counter: " + count++ + " size: " + nBytesRead);
                                sourceLine.write(data, 0, nBytesRead);
                            }
                        } catch (IOException ex) {
                        }
                    }
                    index++;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                }
            }

            // Song ended
            System.out.println(autoPlay + " " + shuffle + " " + loop);
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
            AutoSetNextSong();
        }
    }

    class GetAlbumCover extends Thread {

        private Socket imageSocket;
        private int songId;
        private DataInputStream imgRes;
        private DataOutputStream imgReq;
        private ByteArrayInputStream imgStream;

        public GetAlbumCover(int songId) {
            this.songId = songId;
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

                imageSocket.close();
                imgRes.close();
                imgReq.close();

                homeController.setAlbumCover(imgStream);
            } catch (IOException ex) {
                Logger.getLogger(MusicPlayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
