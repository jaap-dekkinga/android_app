package com.dekidea.tuneurl.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Url;


public interface Webservice {

    @Multipart
    @POST("/api/match")
    Call<APIResult> matchSoundFile(@Part MultipartBody.Part file);

    @POST
    Call<JsonObject> postFingerprint(@Url String url, @Query("fingerprint") String fingerprint);

    @POST
    Call<JsonObject> postPollAnswer(@Url String url, @Body PollData pollData);

    @POST
    Call<JsonObject> addRecordOfInterest(@Url String url, @Body JsonArray records);

    @POST
    Call<JsonElement> searchFingerprint(@Url String url, @Body JsonObject fingerprint);
}
