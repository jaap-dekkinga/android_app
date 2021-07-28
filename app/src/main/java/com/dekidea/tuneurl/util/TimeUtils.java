package com.dekidea.tuneurl.util;


import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;

import com.dekidea.tuneurl.R;
import com.dekidea.tuneurl.service.AutorunJobService;
import com.dekidea.tuneurl.service.AutorunService;

import org.joda.time.Instant;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


public class TimeUtils implements Constants {


	public static String pad(int c) {
		
		if (c >= 10){
			
			return String.valueOf(c);
		}			
		else{
			
			return "0" + String.valueOf(c);
		}
	}


    public static String getTimestamp(){

        Instant instant = Instant.now();

        return instant.toString();
    }
	
	
	public static String getCurrentTimeAsString(){
		
		String time = "";
		
		try{
		
			Calendar calendar = Calendar.getInstance();
			
			String month_as_string = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int year = calendar.get(Calendar.YEAR);
			
			int hour = 	calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);		
			
			time = month_as_string + " " + day + ", " + year + " - " + hour + ":" + minute;
		}
		catch(NullPointerException e){
			
			e.printStackTrace();
		}
		catch(IllegalArgumentException e){
			
			e.printStackTrace();
		}
		
		return time;
	}


    public static String getCurrentTimeAsFormattedString(){

        String time = "";

        try{

            Calendar calendar = Calendar.getInstance();

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);


            int hour = 	calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            time = year + "-" + pad(month) + "-" + pad(day) + "T" + pad(hour) + "" + pad(minute);
        }
        catch(NullPointerException e){

            e.printStackTrace();
        }
        catch(IllegalArgumentException e){

            e.printStackTrace();
        }

        return time;
    }


	public static long getCurrentTimeInMillis(){
		
		Calendar calendar = Calendar.getInstance();
		
		return calendar.getTimeInMillis();
	}
	
	
	@SuppressLint("NewApi")
	public static void scheduleAutomaticStart(Context context){
		
		System.out.println("TimeUtils.scheduleAutomaticStart()");

        int current_api_version = android.os.Build.VERSION.SDK_INT;

        try {

            if (current_api_version > 25) {

                long latency = getAutomaticStartDelayInMillis(context);
                PersistableBundle extras = new PersistableBundle();
                extras.putInt(ACTION, ACTION_AUTORUN_START);

                System.out.println("latency = " + latency);

                ComponentName serviceComponent = new ComponentName(context, AutorunJobService.class);
                JobInfo.Builder builder = new JobInfo.Builder(AUTORUN_START_JOB_ID, serviceComponent);
                builder.setMinimumLatency(latency); // wait at least
                builder.setOverrideDeadline(latency + 2000); // maximum delay
                builder.setExtras(extras);

                //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
                //builder.setRequiresDeviceIdle(true); // device should be idle
                //builder.setRequiresCharging(false); // we don't care if the device is charging or not
                JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
                jobScheduler.schedule(builder.build());
            }
            else {

                AlarmManager alarm_manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                Intent intent = new Intent(context, AutorunService.class);
                intent.putExtra(ACTION, ACTION_AUTORUN_START);

                PendingIntent pending_intent = PendingIntent.getService(context, ACTION_AUTORUN_START, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                long start_time_in_millis = getAutomaticStartTimeInMillis(context);

                //alarm_manager.setRepeating(AlarmManager.RTC_WAKEUP, start_time_in_millis, 86400000L, pending_intent);

                if (current_api_version < 19) {

                    alarm_manager.set(AlarmManager.RTC_WAKEUP, start_time_in_millis, pending_intent);
                }
                else if (current_api_version >= 19 && current_api_version < 23) {

                    alarm_manager.setExact(AlarmManager.RTC_WAKEUP, start_time_in_millis, pending_intent);
                }
                else {

                    alarm_manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, start_time_in_millis, pending_intent);
                }
            }

            TimeUtils.scheduleAutomaticStop(context);
        }
        catch (Exception e){

            e.printStackTrace();
        }
	}
	
	
	public static long getAutomaticStartTimeInMillis(Context context){
		
		long start_time_in_millis = -1;
		long current_time_in_millis = -1;
		long end_time_in_millis = -1;
		
		TimeZone timezone = TimeZone.getDefault();
		
		int setting_timezone = Settings.fetchIntSetting(context, SETTING_TIMEZONE, DEFAULT_TIMEZONE);
		
		String[] timezones = context.getResources().getStringArray(R.array.setting_timezone_values);
		
		int start_hour = Settings.fetchIntSetting(context, SETTING_AUTORUN_START_HOUR, DEFAULT_AUTORUN_START_HOUR);
		int start_minute = Settings.fetchIntSetting(context, SETTING_AUTORUN_START_MINUTE, DEFAULT_AUTORUN_START_MINUTE);
		
		int end_hour = Settings.fetchIntSetting(context, SETTING_AUTORUN_END_HOUR, DEFAULT_AUTORUN_END_HOUR);
		int end_minute = Settings.fetchIntSetting(context, SETTING_AUTORUN_END_MINUTE, DEFAULT_AUTORUN_START_MINUTE);
		
		if(setting_timezone < timezones.length - 1){
			
			timezone = TimeZone.getTimeZone(timezones[setting_timezone]);
		}
		
		Calendar start_calendar = Calendar.getInstance(timezone);
		Calendar current_calendar = Calendar.getInstance(timezone);
		Calendar end_calendar = Calendar.getInstance(timezone);
		
		start_calendar.set(Calendar.HOUR_OF_DAY, start_hour);
		start_calendar.set(Calendar.MINUTE, start_minute);
		start_calendar.set(Calendar.SECOND, 0);
		start_calendar.set(Calendar.MILLISECOND, 0);
		
		end_calendar.set(Calendar.HOUR_OF_DAY, end_hour);
		end_calendar.set(Calendar.MINUTE, end_minute);
		end_calendar.set(Calendar.SECOND, 0);
		end_calendar.set(Calendar.MILLISECOND, 0);
		
		start_time_in_millis = start_calendar.getTimeInMillis();
		current_time_in_millis = current_calendar.getTimeInMillis();
		end_time_in_millis = end_calendar.getTimeInMillis();
		
		if(current_time_in_millis > end_time_in_millis){
			
			start_time_in_millis = start_time_in_millis + 86400000L;
		}
		
		return start_time_in_millis;
	}


    public static long getAutomaticStartDelayInMillis(Context context){

        long start_time_in_millis = -1;
        long current_time_in_millis = -1;

        TimeZone timezone = TimeZone.getDefault();

        int setting_timezone = Settings.fetchIntSetting(context, SETTING_TIMEZONE, DEFAULT_TIMEZONE);

        String[] timezones = context.getResources().getStringArray(R.array.setting_timezone_values);

        int start_hour = Settings.fetchIntSetting(context, SETTING_AUTORUN_START_HOUR, DEFAULT_AUTORUN_START_HOUR);
        int start_minute = Settings.fetchIntSetting(context, SETTING_AUTORUN_START_MINUTE, DEFAULT_AUTORUN_START_MINUTE);

        if(setting_timezone < timezones.length - 1){

            timezone = TimeZone.getTimeZone(timezones[setting_timezone]);
        }

        Calendar start_calendar = Calendar.getInstance(timezone);
        Calendar current_calendar = Calendar.getInstance(timezone);

        start_calendar.set(Calendar.HOUR_OF_DAY, start_hour);
        start_calendar.set(Calendar.MINUTE, start_minute);
        start_calendar.set(Calendar.SECOND, 0);
        start_calendar.set(Calendar.MILLISECOND, 0);

        start_time_in_millis = start_calendar.getTimeInMillis();
        current_time_in_millis = current_calendar.getTimeInMillis();

        if(current_time_in_millis > start_time_in_millis){

            start_time_in_millis = start_time_in_millis + 86400000L;
        }

        return start_time_in_millis - current_time_in_millis;
    }
	
	
	@SuppressLint("NewApi")
	public static void scheduleAutomaticStop(Context context){
		
		System.out.println("TimeUtils.scheduleAutomaticStop()");

        int current_api_version = android.os.Build.VERSION.SDK_INT;

        try {

            if (current_api_version > 25) {

                long latency = getAutomaticStopDelayInMillis(context);
                PersistableBundle extras = new PersistableBundle();
                extras.putInt(ACTION, ACTION_AUTORUN_STOP);

                System.out.println("latency = " + latency);

                ComponentName serviceComponent = new ComponentName(context, AutorunJobService.class);
                JobInfo.Builder builder = new JobInfo.Builder(AUTORUN_STOP_JOB_ID, serviceComponent);
                builder.setMinimumLatency(latency); // wait at least
                builder.setOverrideDeadline(latency + 2000); // maximum delay
                builder.setExtras(extras);

                //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
                //builder.setRequiresDeviceIdle(true); // device should be idle
                //builder.setRequiresCharging(false); // we don't care if the device is charging or not
                JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
                jobScheduler.schedule(builder.build());
            }
            else{

                AlarmManager alarm_manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                Intent intent = new Intent(context, AutorunService.class);
                intent.putExtra(ACTION, ACTION_AUTORUN_STOP);

                PendingIntent pending_intent = PendingIntent.getService(context, ACTION_AUTORUN_STOP, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                long stop_time_in_millis = getAutomaticStopTimeInMillis(context);

                //alarm_manager.setRepeating(AlarmManager.RTC_WAKEUP, stop_time_in_millis, 86400000L, pending_intent);

                if (current_api_version < 19) {

                    alarm_manager.set(AlarmManager.RTC_WAKEUP, stop_time_in_millis, pending_intent);
                }
                else if (current_api_version >= 19 && current_api_version < 23) {

                    alarm_manager.setExact(AlarmManager.RTC_WAKEUP, stop_time_in_millis, pending_intent);
                }
                else {

                    alarm_manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, stop_time_in_millis, pending_intent);
                }
            }
        }
        catch (Exception e){

            e.printStackTrace();

        }
    }
	
	
	public static long getAutomaticStopDelayInMillis(Context context){
		
		long end_time_in_millis = -1;
		long current_time_in_millis = -1;
		
		TimeZone timezone = TimeZone.getDefault();
		
		int setting_timezone = Settings.fetchIntSetting(context, SETTING_TIMEZONE, DEFAULT_TIMEZONE);
		
		String[] timezones = context.getResources().getStringArray(R.array.setting_timezone_values);
		
		int end_hour = Settings.fetchIntSetting(context, SETTING_AUTORUN_END_HOUR, DEFAULT_AUTORUN_END_HOUR);
		int end_minute = Settings.fetchIntSetting(context, SETTING_AUTORUN_END_MINUTE, DEFAULT_AUTORUN_END_MINUTE);
		
		if(setting_timezone < timezones.length - 1){
			
			timezone = TimeZone.getTimeZone(timezones[setting_timezone]);
		}
		
		Calendar calendar = Calendar.getInstance(timezone);
		Calendar current_calendar = Calendar.getInstance(timezone);
		
		calendar.set(Calendar.HOUR_OF_DAY, end_hour);
		calendar.set(Calendar.MINUTE, end_minute);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		end_time_in_millis = calendar.getTimeInMillis();
		current_time_in_millis = current_calendar.getTimeInMillis();
		
		if(current_time_in_millis > end_time_in_millis){
		
			end_time_in_millis = end_time_in_millis + 86400000L;
		}

		return end_time_in_millis - current_time_in_millis;
	}


    public static long getAutomaticStopTimeInMillis(Context context){

        long end_time_in_millis = -1;
        long current_time_in_millis = -1;

        TimeZone timezone = TimeZone.getDefault();

        int setting_timezone = Settings.fetchIntSetting(context, SETTING_TIMEZONE, DEFAULT_TIMEZONE);

        String[] timezones = context.getResources().getStringArray(R.array.setting_timezone_values);

        int end_hour = Settings.fetchIntSetting(context, SETTING_AUTORUN_END_HOUR, DEFAULT_AUTORUN_END_HOUR);
        int end_minute = Settings.fetchIntSetting(context, SETTING_AUTORUN_END_MINUTE, DEFAULT_AUTORUN_END_MINUTE);

        if(setting_timezone < timezones.length - 1){

            timezone = TimeZone.getTimeZone(timezones[setting_timezone]);
        }

        Calendar calendar = Calendar.getInstance(timezone);
        Calendar current_calendar = Calendar.getInstance(timezone);

        calendar.set(Calendar.HOUR_OF_DAY, end_hour);
        calendar.set(Calendar.MINUTE, end_minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        end_time_in_millis = calendar.getTimeInMillis();
        current_time_in_millis = current_calendar.getTimeInMillis();

        if(current_time_in_millis > end_time_in_millis){

            end_time_in_millis = end_time_in_millis + 86400000L;
        }
        return end_time_in_millis;
    }


    public static void cancelAutomaticStart(Context context){

        System.out.println("TimeUtils.cancelAutomaticStart()");

        int current_api_version = android.os.Build.VERSION.SDK_INT;

        try {

            if (current_api_version > 25) {

                JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
                jobScheduler.cancel(AUTORUN_START_JOB_ID);
            }
            else {

                AlarmManager alarm_manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                Intent intent = new Intent(context, AutorunService.class);
                intent.putExtra(ACTION, ACTION_AUTORUN_START);

                PendingIntent pending_intent = PendingIntent.getService(context, ACTION_AUTORUN_START, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                alarm_manager.cancel(pending_intent);
            }

            TimeUtils.cancelAutomaticStop(context);
        }
        catch (Exception e){

            e.printStackTrace();

        }
    }
	
	
	public static void cancelAutomaticStop(Context context){
		
		System.out.println("TimeUtils.cancelAutomaticStop()");

        int current_api_version = android.os.Build.VERSION.SDK_INT;

        try {

            if (current_api_version > 25) {

                JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
                jobScheduler.cancel(AUTORUN_STOP_JOB_ID);
            }
            else {
				
                AlarmManager alarm_manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                Intent intent = new Intent(context, AutorunService.class);
                intent.putExtra(ACTION, ACTION_AUTORUN_STOP);

                PendingIntent pending_intent = PendingIntent.getService(context, ACTION_AUTORUN_STOP, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                alarm_manager.cancel(pending_intent);
            }
        }
        catch (Exception e){

            e.printStackTrace();

        }
	}


	public static long getDeleteNewsItemsThreshold(Context context ){

	    long threshold = -1;

	    long current_time_in_millis = getCurrentTimeInMillis();

        int seting_store_news_items = Settings.fetchIntSetting(context, SETTING_STORE_NEWS_ITEMS, SETTING_STORE_NEWS_ITEMS_1DAY);

        if(seting_store_news_items == SETTING_STORE_NEWS_ITEMS_1DAY){

            threshold = current_time_in_millis - ONE_DAY_IN_MILLIS;
        }
        else if(seting_store_news_items == SETTING_STORE_NEWS_ITEMS_1WEEK){

            threshold = current_time_in_millis - 7 * ONE_DAY_IN_MILLIS;
        }

        return threshold;
    }
}
