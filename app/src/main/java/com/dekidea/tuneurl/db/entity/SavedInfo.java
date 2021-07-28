package com.dekidea.tuneurl.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity
public class SavedInfo {

    @PrimaryKey(autoGenerate = false)
    @SerializedName("song_id")
    @Expose
    private long song_id;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("url")
    @Expose
    private String url;

    @SerializedName("image_url")
    @Expose
    private String image_url;

    @SerializedName("type")
    @Expose
    private int type;

    @SerializedName("date")
    @Expose
    private String date;

    @SerializedName("date_absolute")
    @Expose
    private long date_absolute;

    // --- CONSTRUCTORS ---

    //public SavedInfo() { }

    public SavedInfo(long song_id, String title, String url, String image_url, int type, String date, long date_absolute) {

        this.song_id = song_id;
        this.title = title;
        this.url = url;
        this.image_url = image_url;
        this.type = type;
        this.date = date;
        this.date_absolute = date_absolute;
    }

    // --- GETTER ---

    public long getSong_id() { return this.song_id; }
    public String getTitle() { return this.title; }
    public String getUrl() { return this.url; }
    public String getImage_url() { return this.image_url; }
    public int getType() { return this.type; }
    public String getDate() { return this.date; }
    public long getDate_absolute() { return this.date_absolute; }

    // --- SETTER ---

    public void setSong_id(long song_id) { this.song_id = song_id; }
    public void setTitle(String title) { this.title = title; }
    public void setURL(String url) { this.url = url; }
    public void setImage_url(String image_url) { this.image_url = image_url; }
    public void setType(int type) { this.type = type; }
    public void setDate(String date) { this.date = date; }
    public void setDate_absolute(long date_absolute) { this.date_absolute = date_absolute; }
}
