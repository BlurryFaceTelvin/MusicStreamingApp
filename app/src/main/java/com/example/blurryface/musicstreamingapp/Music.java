package com.example.blurryface.musicstreamingapp;

/**
 * Created by BlurryFace on 1/24/2018.
 */

public class Music {
    public String artists;
    public String title;
    public String image;
    public String song;

    public Music(){
    }

    public Music(String artists, String title, String thumbImage) {
        this.artists = artists;
        this.title = title;
        this.image = thumbImage;
    }
    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }
    public String getArtist() {
        return artists;
    }

    public void setArtist(String artist) {
        this.artists = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
