/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotify.Bean;

import java.io.Serializable;

/**
 *
 * @author ADMIN
 */
public class Song implements Serializable {

    private static final long serialVersionUID = 1L;
    private int songId;
    private String artist;
    private String name;
    private double time;
    private String imgurl;
    private String songPath;

    public Song(int songId, String artist, String name, double time) {
        this.songId = songId;
        this.artist = artist;
        this.name = name;
        this.time = time;
    }

    public Song(int songId, String artist, String name, double time, String imgurl) {
        this.songId = songId;
        this.artist = artist;
        this.name = name;
        this.time = time;
        this.imgurl = imgurl;
    }

    public Song(String artist, String name, double time, String songPath) {
        this.artist = artist;
        this.name = name;
        this.time = time;
        this.songPath = songPath;
    }

    public String getSongPath() {
        return songPath;
    }

    public void setSongPath(String songPath) {
        this.songPath = songPath;
    }


    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

}
