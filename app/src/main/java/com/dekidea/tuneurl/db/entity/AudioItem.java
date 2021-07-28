package com.dekidea.tuneurl.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity
public class AudioItem {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @SerializedName("id")
    @Expose
    private long id;

    @SerializedName("audio_file_path")
    @Expose
    private String audio_file_path;

    @SerializedName("user_response")
    @Expose
    private String user_response;

    // --- CONSTRUCTORS ---

    //public AudioItem() { }

    public AudioItem(String audio_file_path, String user_response) {

        this.audio_file_path = audio_file_path;
        this.user_response = user_response;
    }

    // --- GETTER ---

    public long getId() { return this.id; }
    public String getAudio_file_path() { return this.audio_file_path; }
    public String getUser_response() { return this.user_response; }

    // --- SETTER ---

    public void setId(long id) { this.id = id; }
    public void setAudio_file_path(String audio_file_path) { this.audio_file_path = audio_file_path; }
    public void setUser_response(String user_response) { this.user_response = user_response; }
}
