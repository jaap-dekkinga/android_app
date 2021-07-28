package com.dekidea.tuneurl.fragment;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dekidea.tuneurl.R;
import com.dekidea.tuneurl.adapter.SavedInfoAdapter;
import com.dekidea.tuneurl.db.entity.SavedInfo;
import com.dekidea.tuneurl.service.APIService;
import com.dekidea.tuneurl.util.Constants;
import com.dekidea.tuneurl.view_model.SavedInfoViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjection;
import dagger.android.support.HasSupportFragmentInjector;

public class SavedInfoFragment extends Fragment implements Constants {

    private Context mContext;

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    private SavedInfoViewModel viewModel;

    private ListView mSavedInfoList;

    private long mDeleteNewsItemsThreshold = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContext = this.getContext();

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_saved_info_page, container, false);

        configureDagger();

        mSavedInfoList = (ListView)rootView.findViewById(R.id.saved_info_list);

        mSavedInfoList.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub

                try {

                    SavedInfo item = (SavedInfo) mSavedInfoList.getAdapter().getItem(position);

                    if (item != null) {

                        int type = item.getType();
                        String date = item.getDate();

                        if(type == TYPE_COUPON_NEW){

                            showCouponUseDialog(item);
                        }
                        else {

                            addRecordOfInterest(item.getSong_id(), INTEREST_ACTION_ACTED, date);

                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.getUrl())));
                        }
                    }
                }
                catch(Exception e){

                    e.printStackTrace();
                }
            }});

        configureViewModel();

        return rootView;
    }


    private void configureDagger(){

        AndroidSupportInjection.inject(this);
    }


    private void configureViewModel(){

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SavedInfoViewModel.class);

        viewModel.init();

        if(mDeleteNewsItemsThreshold > 0){

            viewModel.deleteNewsItems(mDeleteNewsItemsThreshold);
        }

        viewModel.getNewsItems().observe(this, items -> updateUI(items));
    }


    private void updateUI(final List<SavedInfo> items){

        SavedInfoAdapter adapter = new SavedInfoAdapter(mContext, R.layout.adapter_saved_info, items);

        mSavedInfoList.setAdapter(adapter);
    }


    private void showCouponUseDialog(final SavedInfo item){

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        //builder.setTitle(R.string.coupon_dialog_title);
        builder.setMessage(R.string.coupon_dialog_message);


        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                viewModel.updateSavedInfo(TYPE_COUPON_USED, item.getSong_id());

                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.getUrl())));
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {


            }
        });


        AlertDialog alert = builder.create();

        alert.show();
    }


    private void addRecordOfInterest(long song_ID, String Interest_action, String date){

        Intent i = new Intent(mContext, APIService.class);

        i.putExtra(ACTION, ACTION_ADD_RECORD_OF_INTEREST);
        i.putExtra(TUNEURL_ID, song_ID);
        i.putExtra(INTEREST_ACTION, Interest_action);
        i.putExtra(DATE, date);

        mContext.startService(i);
    }
}
