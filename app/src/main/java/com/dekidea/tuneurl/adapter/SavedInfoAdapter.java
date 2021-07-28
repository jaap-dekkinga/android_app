package com.dekidea.tuneurl.adapter;

import java.util.List;

import com.dekidea.tuneurl.R;
import com.dekidea.tuneurl.db.entity.SavedInfo;
import com.dekidea.tuneurl.service.APIService;
import com.dekidea.tuneurl.util.Constants;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import static com.dekidea.tuneurl.util.Constants.TYPE_COUPON_NEW;
import static com.dekidea.tuneurl.util.Constants.TYPE_COUPON_USED;

public class SavedInfoAdapter extends ArrayAdapter<SavedInfo> implements Constants {

	List<SavedInfo> objects;
	
	int resourceViewId;
	
	Context mContext;
	
	public SavedInfoAdapter(Context context, int textViewResourceId, List<SavedInfo> objects) {
		
		super(context, textViewResourceId, objects);
		
		this.objects = objects;
		
		this.resourceViewId = textViewResourceId;
		
		mContext = context;
	}

	public View getView(int position, View convertView, ViewGroup parent){

		View v = convertView;
		
		if (v == null) {
			
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
			v = inflater.inflate(resourceViewId, null);
		}

		final SavedInfo saved_info = objects.get(position);

		if (saved_info != null) {

            ImageView type_image = (ImageView)v.findViewById(R.id.type_image);
			TextView text_title = (TextView) v.findViewById(R.id.news_title);
			TextView text_date = (TextView) v.findViewById(R.id.news_date);
			ImageView share_image = (ImageView)v.findViewById(R.id.share_image);
            TextView coupon_status = (TextView) v.findViewById(R.id.coupon_status);

            if(type_image != null){

                int type = saved_info.getType();

                if(type == TYPE_COUPON_NEW ) {

                    type_image.setImageResource(R.drawable.ic_coupon);

                    coupon_status.setText("");
                }
                else if(type == TYPE_COUPON_USED){

                    type_image.setImageResource(R.drawable.ic_coupon);

                    coupon_status.setText(R.string.used);
                }
                else{

                    coupon_status.setText("");
                }
            }
			
			if(text_title != null){
				
				text_title.setText(saved_info.getTitle());
			}
			
			if(text_date != null){
				
				text_date.setText(saved_info.getDate());
			}
			
			if(share_image != null){
				
				share_image.setOnLongClickListener(new OnLongClickListener(){

					@Override
					public boolean onLongClick(View v) {

						addRecordOfInterest(mContext, saved_info, INTEREST_ACTION_SHARED);

						shareData(mContext, saved_info);
						
						return true;
					}});
			}
		}

		return v;
	}


	private void shareData(Context context, SavedInfo saved_info){

		Intent i = new Intent();

		i.setAction(Intent.ACTION_SEND);
		i.setType("text/plain");

		i.putExtra(Intent.EXTRA_SUBJECT,  saved_info.getTitle());
		i.putExtra(Intent.EXTRA_TEXT, saved_info.getUrl());

		context.startActivity(i);
	}


	private void addRecordOfInterest(Context context, SavedInfo saved_info, String Interest_action){

		Intent i = new Intent(mContext, APIService.class);

		i.putExtra(ACTION, ACTION_ADD_RECORD_OF_INTEREST);
		i.putExtra(TUNEURL_ID, saved_info.getSong_id());
		i.putExtra(INTEREST_ACTION, Interest_action);
		i.putExtra(DATE, saved_info.getDate());

		context.startService(i);
	}
}