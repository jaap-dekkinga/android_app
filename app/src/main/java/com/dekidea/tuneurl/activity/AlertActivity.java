package com.dekidea.tuneurl.activity;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dekidea.tuneurl.R;
import com.dekidea.tuneurl.service.APIService;
import com.dekidea.tuneurl.util.Constants;
import com.dekidea.tuneurl.util.Settings;
import com.dekidea.tuneurl.view.SurfaceViewObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class AlertActivity extends Activity implements Constants, RecognitionListener {
	
	private Context mContext;

	private SurfaceViewObject mSwipePad;
	
	private ImageView mSaveLogo;
	private ImageView mSaveArrows;
	private TextView mSaveLabel;
	
	private ImageView mIgnoreLogo;
	private ImageView mIgnoreArrows;
	private TextView mIgnoreLabel;

	private RelativeLayout mChoiceLayout;
	private RelativeLayout mOverlayLayout;
	private View mOverlayYes;
	private View mOverlayNo;

	public float mSwipeTreshold;
	public float mAnimationTreshold;

    private int mRunningState;
    private int mVisibility;

    SpeechRecognizer speech;

    private long mTuneURL_ID;
	private String mDate;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		mContext = this;

		mTuneURL_ID = getIntent().getLongExtra(TUNEURL_ID, -1);
		mDate = getIntent().getStringExtra(DATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {

            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(this, null);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);


		setContentView(R.layout.activity_alert_layout);
		
		//Settings.updateIntSetting(mContext,SETTING_LISTENING_STATE, SETTING_LISTENING_STATE_STOPPED);
						
		mSwipePad = (SurfaceViewObject) findViewById(R.id.swipe_pad);
		mSwipePad.setActivity(this);
		
		mSaveLogo = (ImageView) findViewById(R.id.save_logo);
		mSaveArrows = (ImageView) findViewById(R.id.save_arrows);
		mSaveLabel = (TextView) findViewById(R.id.save_label);
		
		
		mIgnoreLogo = (ImageView) findViewById(R.id.ignore_logo);
		mIgnoreArrows = (ImageView) findViewById(R.id.ignore_arrows);
		mIgnoreLabel = (TextView) findViewById(R.id.ignore_label);

		mChoiceLayout = (RelativeLayout) findViewById(R.id.choice_layout);
		mOverlayLayout = (RelativeLayout) findViewById(R.id.result_layout);
		mOverlayYes = (View) findViewById(R.id.overlay_yes);
		mOverlayNo = (View) findViewById(R.id.overlay_no);
		
		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();
		display.getMetrics(dm);
		
		int display_width = dm.widthPixels;
		mSwipeTreshold = ((float)display_width)/3f;
		mAnimationTreshold = ((float)display_width)/30f;

		try {

			speech = SpeechRecognizer.createSpeechRecognizer(this);
			speech.setRecognitionListener(this);

			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");

			speech.startListening(intent);
		}
		catch (Exception e){

			e.printStackTrace();
		}
	}
	
	
	@Override
	public void onResume() {
		
		super.onResume();

		mVisibility = View.VISIBLE;
		
		mRunningState = Settings.fetchIntSetting(mContext, SETTING_RUNNING_STATE, SETTING_RUNNING_STATE_STOPPED);

		scheduleDefaultClose();
	}
	
	
	@Override
	public void onPause() {
		
		super.onPause();

        stopSpeechListener();

		mVisibility = View.INVISIBLE;
		
		mRunningState = Settings.fetchIntSetting(mContext, SETTING_RUNNING_STATE, SETTING_RUNNING_STATE_STOPPED);

		if (mRunningState == SETTING_RUNNING_STATE_STARTED) {
			
			int listening_state = Settings.fetchIntSetting(mContext, SETTING_LISTENING_STATE, SETTING_LISTENING_STATE_STOPPED);
			
			if(listening_state == SETTING_LISTENING_STATE_STOPPED) {

                if (!this.isFinishing()) {

                    Settings.startListening(this);
                }
            }
        }
	}
	
	
	@Override
	public void onBackPressed() {

        stopSpeechListener();
		
		registerSwipe(SWIPE_LEFT);
	}


	public void registerSwipe(int swipe_type){

		if(mRunningState == SETTING_RUNNING_STATE_STARTED && mVisibility == View.VISIBLE){

			String user_response = "";

			if(swipe_type == SWIPE_LEFT){

				user_response = USER_RESPONSE_NO;
			}
			else if(swipe_type == SWIPE_RIGHT){

				user_response = USER_RESPONSE_YES;

				addRecordOfInterest(INTEREST_ACTION_INTERESTED);
			}

			doAction(user_response);

			showResult(swipe_type);

			Timer timer = new Timer(true);
			TimerTask timerTask = new TimerTask() {
				@Override
				public void run() {

					moveTaskToBack(true);

					finish();
				}
			};

			timer.schedule(timerTask, 2000);
		}
	}


	public void addRecordOfInterest(String Interest_action) {

		try {

			Intent i = new Intent(mContext, APIService.class);
			i.putExtra(ACTION, ACTION_ADD_RECORD_OF_INTEREST);
			i.putExtra(INTEREST_ACTION, Interest_action);
			i.putExtra(TUNEURL_ID, mTuneURL_ID);
			i.putExtra(DATE, mDate);
			startService(i);
		}
		catch (Exception e){

			e.printStackTrace();
		}
	}


	private void doAction(String user_response){

		Intent i = new Intent(mContext, APIService.class);
		i.putExtra(ACTION, ACTION_DO_ACTION);
		i.putExtra(USER_RESPONSE, user_response);
		startService(i);
	}


	private void showResult(int swipe_type) {

		fadeOutChoiceView();

		mOverlayLayout.setVisibility(View.VISIBLE);

		if (swipe_type == SWIPE_LEFT) {

			mOverlayYes.setVisibility(View.GONE);
			mOverlayNo.setVisibility(View.VISIBLE);
		}
		else if (swipe_type == SWIPE_RIGHT) {

			mOverlayYes.setVisibility(View.VISIBLE);
			mOverlayNo.setVisibility(View.GONE);
		}
	}


	private void fadeOutChoiceView() {
		Animation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setInterpolator(new AccelerateInterpolator());
		fadeOut.setDuration(1000);

		fadeOut.setAnimationListener(new Animation.AnimationListener()
		{
			public void onAnimationEnd(Animation animation)
			{
				mChoiceLayout.setVisibility(View.GONE);
			}
			public void onAnimationRepeat(Animation animation) {}
			public void onAnimationStart(Animation animation) {}
		});

		mChoiceLayout.startAnimation(fadeOut);
	}

	
	private void scheduleDefaultClose(){
		
		TimerTask timerTask = new TimerTask() {
			
			public void run() {

				runOnUiThread(new Runnable() {
					@Override
					public void run() {

						registerSwipe(SWIPE_LEFT);
					}
				});
            }
        };
        
        Timer timer = new Timer();
        timer.schedule(timerTask, SWIPE_WAIT_TIME);	
	}
	
	
	private float scale_save = 1f;
	private float scale_ignore = 1f;
	private float scale_rate = 0.02f;
	private float max_scale = 1.5f;
	
	private boolean animated = false;
	
	public void performAnimation(int swipe_type){
		
		if(swipe_type == SWIPE_RIGHT){
			
			if(!animated){
				
				animated = true;
			
				mSaveLogo.setVisibility(View.VISIBLE);
				mSaveArrows.setVisibility(View.VISIBLE);
				mSaveLabel.setVisibility(View.VISIBLE);
				
				
				mIgnoreLogo.setVisibility(View.GONE);
				mIgnoreArrows.setVisibility(View.GONE);
				mIgnoreLabel.setVisibility(View.GONE);
			}
			
			if(scale_save < max_scale){
				
				scale_save = scale_save + scale_rate;
				
				mSaveLogo.setScaleX(scale_save);
				mSaveLogo.setScaleY(scale_save);			
			}
		}
		else if(swipe_type == SWIPE_LEFT){
			
			if(!animated){
				
				animated = true;
			
				mSaveLogo.setVisibility(View.GONE);
				mSaveArrows.setVisibility(View.GONE);
				mSaveLabel.setVisibility(View.GONE);
				
				
				mIgnoreLogo.setVisibility(View.VISIBLE);
				mIgnoreArrows.setVisibility(View.VISIBLE);
				mIgnoreLabel.setVisibility(View.VISIBLE);
			}

			if(scale_ignore < max_scale){
				
				scale_ignore = scale_ignore + scale_rate;
				
				mIgnoreLogo.setScaleX(scale_ignore);
				mIgnoreLogo.setScaleY(scale_ignore);			
			}
		}
	}
	
	
	public void resetAnimation(){
		
		animated = false;
		
		scale_save = 1f;
		scale_ignore = 1f;
		
		mSaveLogo.setScaleX(scale_save);
		mSaveLogo.setScaleY(scale_save);
		
		mIgnoreLogo.setScaleX(scale_ignore);
		mIgnoreLogo.setScaleY(scale_ignore);	
		
		mSaveLogo.setVisibility(View.VISIBLE);
		mSaveArrows.setVisibility(View.VISIBLE);
		mSaveLabel.setVisibility(View.VISIBLE);
		
		mIgnoreLogo.setVisibility(View.VISIBLE);
		mIgnoreArrows.setVisibility(View.VISIBLE);
		mIgnoreLabel.setVisibility(View.VISIBLE);
	}


    //UPDATE voice commands

    @Override
    public void onReadyForSpeech(Bundle bundle) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int i) {

    }

    @Override
    public void onResults(Bundle bundle) {

        System.out.println("AlertActivity.onResults()");

	    try {

            if (bundle != null) {

                String result = "";

                        ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null && !matches.isEmpty()) {

                    boolean found = false;

                    for (int i = 0; i < matches.size() && !found; i++) {

                        result = matches.get(i);

                        if(result.equalsIgnoreCase("yes")) {

                            System.out.println("YES");

                            found = true;

                            stopSpeechListener();

                            registerSwipe(SWIPE_RIGHT);
                        }
                        else if(result.equalsIgnoreCase("no")){

                            System.out.println("NO");

                            found = true;

                            stopSpeechListener();

                            registerSwipe(SWIPE_LEFT);
                        }
                    }
                }
            }
        }
	    catch(Exception e){

            System.out.println(e.toString());
        }
    }

    @Override
    public void onPartialResults(Bundle bundle) {

        System.out.println("AlertActivity.onPartialResults()");
    }

    @Override
    public void onEvent(int i, Bundle bundle) {

        System.out.println("AlertActivity.onEvent()");
    }


    private void stopSpeechListener(){

        System.out.println("AlertActivity.stopSpeechListener()");

	    if(speech != null) {

            try {

                speech.stopListening();

                speech.cancel();

                speech.destroy();
            }
            catch (Exception e) {

                System.out.println(e.toString());
            }
        }
    }
}