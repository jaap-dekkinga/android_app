package com.dekidea.tuneurl.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.dekidea.tuneurl.activity.AlertActivity;
import com.dekidea.tuneurl.util.Constants;

public class SurfaceViewObject extends SurfaceView implements SurfaceHolder.Callback, Constants {
	
	private AlertActivity mActivity;
	private float mX;
	   
	public SurfaceViewObject(Context context, AttributeSet attrs) {
		
		super(context, attrs);
		
		setFocusable(true);
	}
	 
	 
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
				 
		
	}

	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub  
		
	}

	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
		
	}
	
	
    @SuppressLint("ClickableViewAccessibility")
	public boolean onTouchEvent(MotionEvent me) {
    	
    	int action = me.getAction(); 
    	
    	float x = me.getX();
    	
    	if (action == MotionEvent.ACTION_DOWN) {
    		
    		mX = x;
        }
    	else if(action == MotionEvent.ACTION_MOVE){
    		
    		float swipe = x - mX;
    		
    		if(Math.abs(swipe) > mActivity.mAnimationTreshold){
    			
    			if(swipe > 0){
    				
    				mActivity.performAnimation(SWIPE_RIGHT);
    			}
    			else if(swipe < 0){
    				
    				mActivity.performAnimation(SWIPE_LEFT);
    			}
    		}
    	}
    	else if(action == MotionEvent.ACTION_UP){
    		
    		float swipe = x - mX;
    		
    		if(Math.abs(swipe) > mActivity.mSwipeTreshold){
    			
    			if(swipe > 0){

					System.out.println("SurfaceViewObject.onTouchEvent: SWIPE_RIGHT");
    				
    				mActivity.registerSwipe(SWIPE_RIGHT);
    			}
    			else if(swipe < 0){
    				
    				mActivity.registerSwipe(SWIPE_LEFT);

					System.out.println("SurfaceViewObject.onTouchEvent: SWIPE_LEFT");
    			}
    		}
    		else{
    			
    			mActivity.resetAnimation();
    		}
    	}    	
    	
    	
        return true;
    }
    
    
    public void setActivity(AlertActivity activity){
    	
    	mActivity = activity;
    }
}
