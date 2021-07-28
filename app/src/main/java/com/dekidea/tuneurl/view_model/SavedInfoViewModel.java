package com.dekidea.tuneurl.view_model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.dekidea.tuneurl.db.entity.SavedInfo;
import com.dekidea.tuneurl.repository.Repository;

import java.util.List;

import javax.inject.Inject;

public class SavedInfoViewModel extends ViewModel {

    private LiveData<List<SavedInfo>> items;
    private Repository repo;

    @Inject
    public SavedInfoViewModel(Repository repo) {

        this.repo = repo;
    }

    // ----

    public void init() {

        if (this.items == null){

            items = repo.getNewsItems();
        }
    }

    public LiveData<List<SavedInfo>> getNewsItems() {

        return this.items;
    }

    public void deleteNewsItems(long date_absolute) {

        repo.deleteNewsItems(date_absolute);
    }

    public void updateSavedInfo(int type, long id) {

        repo.updateSavedInfo(type, id);
    }
}
