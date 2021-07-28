package com.dekidea.tuneurl.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity
public class PollItem {

    @PrimaryKey(autoGenerate = false)
    @SerializedName("song_id")
    @Expose
    private long song_id;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("poll_response")
    @Expose
    private String poll_response;

    @SerializedName("date")
    @Expose
    private String date;

    @SerializedName("date_absolute")
    @Expose
    private long date_absolute;

    // --- CONSTRUCTORS ---

    //public PollItem() { }

    public PollItem(long song_id, String title, String poll_response, String date, long date_absolute) {

        this.song_id = song_id;
        this.title = title;
        this.poll_response = poll_response;
        this.date = date;
        this.date_absolute = date_absolute;
    }

    // --- GETTER ---

    public long getSong_id() { return this.song_id; }
    public String getTitle() { return this.title; }
    public String getPoll_response() { return this.poll_response; }
    public String getDate() { return this.date; }
    public long getDate_absolute() { return this.date_absolute; }

    // --- SETTER ---

    public void setSong_id(long song_id) { this.song_id = song_id; }
    public void setTitle(String title) { this.title = title; }
    public void setPoll_response(String poll_response) { this.poll_response = poll_response; }
    public void setDate(String date) { this.date = date; }
    public void setDate_absolute(long date_absolute) { this.date_absolute = date_absolute; }
}
