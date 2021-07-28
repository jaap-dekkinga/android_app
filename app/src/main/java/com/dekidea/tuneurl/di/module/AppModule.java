package com.dekidea.tuneurl.di.module;

import android.app.Application;
import android.arch.persistence.room.Room;

import com.dekidea.tuneurl.api.Webservice;
import com.dekidea.tuneurl.db.MyDatabase;
import com.dekidea.tuneurl.db.dao.MyDao;
import com.dekidea.tuneurl.repository.Repository;
import com.dekidea.tuneurl.util.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


@Module(includes = ViewModelModule.class)
public class AppModule implements Constants {

    // --- DATABASE INJECTION ---

    @Provides
    @Singleton
    MyDatabase provideDatabase(Application application) {
        return Room.databaseBuilder(application,
                MyDatabase.class, "MyDatabase.db")
                .build();
    }

    @Provides
    @Singleton
    MyDao provideDao(MyDatabase database) { return database.dao(); }

    // --- REPOSITORY INJECTION ---

    @Provides
    Executor provideExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @Provides
    @Singleton
    Repository provideRepository(Webservice webservice, MyDao dao, Executor executor) {
        return new Repository(webservice, dao, executor);
    }

    // --- NETWORK INJECTION ---

    @Provides
    Gson provideGson() { return new GsonBuilder().create(); }

    @Provides
    Retrofit provideRetrofit(Gson gson) {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(TUNEURL_API_BASE_URL)
                .build();
        return retrofit;
    }

    @Provides
    @Singleton
    Webservice provideApiWebservice(Retrofit restAdapter) {

        return restAdapter.create(Webservice.class);
    }
}
