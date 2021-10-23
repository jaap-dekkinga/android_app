package com.dekidea.tuneurl.repository;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;

import com.dekidea.tuneurl.activity.AlertActivity;
import com.dekidea.tuneurl.api.APIData;
import com.dekidea.tuneurl.api.APIResult;
import com.dekidea.tuneurl.api.PollData;
import com.dekidea.tuneurl.api.RecordOfInterest;
import com.dekidea.tuneurl.api.Webservice;
import com.dekidea.tuneurl.db.dao.MyDao;
import com.dekidea.tuneurl.db.entity.AudioItem;
import com.dekidea.tuneurl.db.entity.SavedInfo;
import com.dekidea.tuneurl.util.Constants;
import com.dekidea.tuneurl.util.FileUtils;
import com.dekidea.tuneurl.util.Settings;
import com.dekidea.tuneurl.util.TimeUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@Singleton
public class Repository implements Constants {

    private final Webservice webservice;
    private final MyDao dao;
    private final Executor executor;

    @Inject
    public Repository(Webservice webservice, MyDao dao, Executor executor) {

        this.webservice = webservice;
        this.dao = dao;
        this.executor = executor;
    }

    // ---

    public List<AudioItem> getAudioItems() {

        return dao.loadAudioItems();
    }

    public void deleteNewsItems(final long date_absolute) {

        executor.execute(() -> {

            dao.deleteSavedInfo(date_absolute);
        });
    }

    public LiveData<List<SavedInfo>> getNewsItems() {

        return dao.loadSavedInfo(); // return a LiveData directly from the database.
    }

    public void updateSavedInfo(int type, long id) {

        executor.execute(() -> {

            dao.updateSavedInfo(type, id);
        });
    }


    private void postPollAnswer(String user_response, APIData data) {

        try{

            String poll_name = data.getInfo();
            String timestamp = TimeUtils.getTimestamp();

            PollData pollData = new PollData(poll_name, user_response, timestamp);

            webservice.postPollAnswer(POLL_API_URL, pollData).enqueue(new Callback<JsonObject>() {

                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                    executor.execute(() -> {

                        try {

                            JsonObject result = response.body();

                            System.out.println(result.toString());
                        }
                        catch (Exception e){

                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                    t.printStackTrace();
                }
            });

        }
        catch(Exception e){

            e.printStackTrace();
        }
    }


    private void openPage(Context context, APIData data){

        try{

            String url = data.getInfo();

            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(i);
        }
        catch(Exception e){

            e.printStackTrace();
        }
    }


    private void saveInfo(APIData data){

        try{

            String action = data.getDescription();

            long song_id = data.getId();
            String title = data.getName();
            String url = data.getInfo();
            String image_url = "";
            int type = 0;

            String date = data.getDate();
            long date_absolute = data.getDateAbsulote();

            if(ACTION_SAVE_PAGE.equals(action)){

                type = TYPE_SAVED_PAGE;
            }
            else if(ACTION_COUPON.equals(action)){

                type = TYPE_COUPON_NEW;
            }

            System.out.println("URL:" + url);
            System.out.println("type:" + type);

            SavedInfo item = new SavedInfo(song_id, title, url, image_url, type, date, date_absolute);

            dao.saveSavedInfo(item);
        }
        catch(Exception e){

            e.printStackTrace();
        }
    }


    private void callPhone(Context context, APIData data){

        try {

            String phone_number = data.getInfo();

            System.out.println("Phone:" + phone_number);

            if (phone_number != null) {

                try {

                    Intent i = new Intent(Intent.ACTION_CALL);

                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    String uri = "tel:" + phone_number.trim();

                    i.setData(Uri.parse(uri));

                    if (ActivityCompat.checkSelfPermission(context.getApplicationContext(),
                            Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {

                        Settings.stopListening(context);

                        context.startActivity(i);
                    }
                    else{

                        Settings.startListening(context);
                    }
                }
                catch (Exception e) {

                    e.printStackTrace();
                }
            }
        }
        catch (Exception e){

            e.printStackTrace();
        }
    }


    private void openMap(Context context, APIData data){

        try{

            String address = data.getInfo();

            Intent i = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("google.navigation:q=an+" + address));

            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(i);
        }
        catch(Exception e){

            e.printStackTrace();
        }
    }


    public void searchFingerprint(final Context context, JsonObject fingerprint) {

        System.out.println("Repository.searchFingerprint()");

        executor.execute(() -> {

            webservice.searchFingerprint(SEARCH_FINGERPRINT_URL, fingerprint).enqueue(new Callback<JsonElement>() {

                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                    executor.execute(() -> {

                        try {

                            System.out.println(response.toString());

                            if(response.code() == 200) {

                                JsonElement result = response.body();

                                System.out.println("result = " + result);

                                if(result != null) {

                                    JsonArray result_array = result.getAsJsonArray();

                                    if (result_array != null && result_array.size() > 0) {

                                        JsonObject closest_match = null;

                                        for (int i = 0; i < result_array.size(); i++) {

                                            try {

                                                JsonObject current_match = result_array.get(i).getAsJsonObject();

                                                if (closest_match == null) {

                                                    closest_match = current_match;
                                                }
                                                else {

                                                    int closest_matchPercentage = closest_match.get("matchPercentage").getAsInt();
                                                    int current_matchPercentage = current_match.get("matchPercentage").getAsInt();

                                                    if (closest_matchPercentage < current_matchPercentage) {

                                                        closest_match = current_match;
                                                    }
                                                }
                                            }
                                            catch (Exception e) {

                                                e.printStackTrace();
                                            }
                                        }

                                        if (closest_match != null) {

                                            int matchPercentage = closest_match.get("matchPercentage").getAsInt();

                                            if(matchPercentage >= FINGERPRINT_MATCHING_THRESHOLD) {

                                                long id = closest_match.get("id").getAsLong();
                                                String type = closest_match.get("type").getAsString();

                                                String name = "";
                                                try {

                                                    name = closest_match.get("name").getAsString();
                                                }
                                                catch (Exception e){

                                                    e.printStackTrace();
                                                }

                                                String info = closest_match.get("info").getAsString();

                                                String description = "";
                                                try {

                                                    description = closest_match.get("description").getAsString();
                                                }
                                                catch (Exception e){

                                                    e.printStackTrace();
                                                }

                                                APIData apiData = new APIData(id, name, description, type, info, matchPercentage);

                                                String date = TimeUtils.getCurrentTimeAsFormattedString();
                                                apiData.setDate(date);
                                                apiData.setDateAbsolute(TimeUtils.getCurrentTimeInMillis());

                                                if (id >= 0) {

                                                    Settings.setCurrentAPIData(apiData);

                                                    addRecordOfInterest(context, String.valueOf(id), INTEREST_ACTION_HEARD, date);

                                                    startAlertActivity(context, id, date);
                                                }
                                                else {

                                                    restartListening(context);
                                                }
                                            }
                                            else {

                                                restartListening(context);
                                            }
                                        }
                                        else {

                                            restartListening(context);
                                        }
                                    }
                                    else {

                                        restartListening(context);
                                    }
                                }
                                else {

                                    restartListening(context);
                                }
                            }
                            else{

                                restartListening(context);
                            }
                        }
                        catch (Exception e){

                            e.printStackTrace();

                            restartListening(context);
                        }
                    });
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {

                    t.printStackTrace();

                    restartListening(context);
                }
            });
        });
    }


    private void restartListening(Context context){

        Settings.setCurrentAPIData(null);

        Settings.startListening(context);
    }


    public void addRecordOfInterest(Context context, String TuneURL_ID, String interest_action, String date) {

        try{

            String UserID = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

            RecordOfInterest rof = new RecordOfInterest(UserID, date, TuneURL_ID, interest_action);
            String json_rof = new Gson().toJson(rof);
            JsonObject jsonObject = new JsonParser().parse(json_rof).getAsJsonObject();
            JsonArray json_array = new JsonArray();
            json_array.add(jsonObject);

            webservice.addRecordOfInterest(INTERESTS_API_URL, json_array).enqueue(new Callback<JsonObject>() {

                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                    executor.execute(() -> {

                        try {

                            System.out.println(response.toString());
                        }
                        catch (Exception e){

                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                    t.printStackTrace();
                }
            });

        }
        catch(Exception e){

            e.printStackTrace();
        }
    }


    public void doAction(Context context, String user_response){

        FileUtils.deleteCacheSoundFiles(context);

        APIData data = Settings.getCurrentAPIData();

        if(data != null) {

            String action = data.getType();
            String date = data.getDate();

            System.out.println("Repository.doAction(): " + action);

            if (ACTION_POLL.equals(action)) {

                postPollAnswer(user_response, data);

                Settings.startListening(context);
            }
            else {

                if (USER_RESPONSE_YES.equals(user_response)) {

                    if (ACTION_SAVE_PAGE.equals(action)) {

                        saveInfo(data);

                        Settings.startListening(context);
                    }
                    else if (ACTION_OPEN_PAGE.equals(action)) {

                        addRecordOfInterest(context, String.valueOf(data.getId()), INTEREST_ACTION_ACTED, date);

                        openPage(context, data);

                        Settings.startListening(context);
                    }
                    else if (ACTION_PHONE.equals(action)) {

                        addRecordOfInterest(context, String.valueOf(data.getId()), INTEREST_ACTION_ACTED, date);

                        callPhone(context, data);
                    }
                    else if (ACTION_COUPON.equals(action)) {

                        saveInfo(data);

                        Settings.startListening(context);
                    }
                    else if (ACTION_MAP.equals(action)) {

                        addRecordOfInterest(context, String.valueOf(data.getId()), INTEREST_ACTION_ACTED, date);

                        openMap(context, data);

                        Settings.startListening(context);
                    }
                    else {

                        Settings.startListening(context);
                    }
                }
                else {

                    Settings.startListening(context);
                }
            }
        }
    }

    private void startAlertActivity(Context context, long TuneURL_ID, String date){

        Intent i = new Intent(context, AlertActivity.class);

        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(TUNEURL_ID, TuneURL_ID);
        i.putExtra(DATE, date);

        context.startActivity(i);
    }
}
