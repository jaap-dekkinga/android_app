package com.dekidea.tuneurl.util;

import com.dekidea.tuneurl.R;

public interface Constants {

    public static final String SETTING_API_URL = "api_url";

    //public static final String TUNEURL_API_BASE_URL = "http://34.208.97.117";
    public static final String TUNEURL_API_BASE_URL = "http://ec2-54-213-252-225.us-west-2.compute.amazonaws.com";
    //public static final String TUNEURL_API_URL = "http://34.208.97.117/api/match";

    public static final String SEARCH_FINGERPRINT_URL = "https://pnz3vadc52.execute-api.us-east-2.amazonaws.com/dev/search-fingerprint";

    public static final String POLL_API_URL = "http://pollapiwebservice.us-east-2.elasticbeanstalk.com/api/pollapi";

    public static final String INTERESTS_API_URL = "https://65neejq3c9.execute-api.us-east-2.amazonaws.com/interests";

    public static final String SHARED_PREFERENCES = "com.appcountry.soundalert.SHARED_PREFERENCES";

    public static final String VERSION_CODE = "VERSION_CODE";

    public static final String ACTION = "action";
    public static final int ACTION_STOP_SERVICE = 0;
    public static final int ACTION_START_SERVICE = 1;
    public static final int ACTION_STOP_LISTENING = 2;
    public static final int ACTION_START_LISTENING = 3;
    public static final int ACTION_GET_DATA_FIRST_RUN = 4;
    public static final int ACTION_GET_DATA_SECOND_RUN = 5;
    public static final int ACTION_AUTORUN_START = 6;
    public static final int ACTION_AUTORUN_STOP = 7;
    public static final int ACTION_DECODE_AUDIO_LABEL = 8;
    public static final int ACTION_DO_ACTION = 9;
    public static final int ACTION_ADD_RECORD_OF_INTEREST = 10;
    public static final int ACTION_SEARCH_FINGERPRINT = 11;

    public static final String MESSAGE_APP_STATUS_CHANGED = "com.appcountry.soundalert.APP_STATUS_CHANGED";
    public static final String MESSAGE = "message";
    public static final int MESSAGE_APP_STATUS_STARTED = 0;
    public static final int MESSAGE_APP_STATUS_STOPPED = 1;

    public static final String TIME = "time";

    public static final int NOTIFICATION_ID = 1521;
    public static final int CAPTURING_NOTIFICATION_ID = 1522;

    public static final String AUDIO_RECORDER_FOLDER = "TuneURL/SoundCache";
    public static final String RECORDED_TRIGGER_FILE = "recorded_trigger.wav";
    public static final String RECORDED_TRIGGER_TEMP_FILE = "recorded_trigger_temp.raw";
    public static final String RECORDED_TEMP_FILE = "recorded_temp.raw";
    public static final String[] REFERENCE_FILES = new String[]{"trigger_audio_1.wav"};
    public static final int[] REFERENCE_RAW_RESOURCES = new int[]{R.raw.trigger_audio_1};

    public static final int MIC_POLL_INTERVAL = 100;
    public static final int DEFAULT_SOUND_THRESHOLD = 82;

    public static final String SETTING_SOUND_THRESHOLD = "sound_threshold";
    public static final String SETTING_RUNNING_STATE= "running_state";

    public static final int SETTING_RUNNING_STATE_STOPPED = 0;
    public static final int SETTING_RUNNING_STATE_STARTED = 1;

    public static final String SETTING_LISTENING_STATE= "listening_state";

    public static final int SETTING_LISTENING_STATE_STOPPED = 0;
    public static final int SETTING_LISTENING_STATE_STARTED = 1;

    public static final int SWIPE_LEFT = 0;
    public static final int SWIPE_RIGHT = 1;

    public static final String SETTING_STORE_NEWS_ITEMS = "store_news_items";
    public static final int SETTING_STORE_NEWS_ITEMS_1DAY = 0;
    public static final int SETTING_STORE_NEWS_ITEMS_1WEEK = 1;

    public static final long ONE_DAY_IN_MILLIS =  86400000L;

    public static final String SETTING_TIMEZONE = "timezone";
    public static final int DEFAULT_TIMEZONE = 3;
    public static final String SETTING_AUTORUN_MODE = "autorun";

    public static final int SETTING_AUTORUN_MODE_DISABLED = 0;
    public static final int SETTING_AUTORUN_MODE_ENABLED = 1;

    public static final String SETTING_AUTORUN_START_HOUR = "autorun_start_hour";
    public static final int DEFAULT_AUTORUN_START_HOUR = 6;
    public static final String SETTING_AUTORUN_START_MINUTE = "autorun_start_minute";
    public static final int DEFAULT_AUTORUN_START_MINUTE = 30;
    public static final String SETTING_AUTORUN_END_HOUR = "autorun_end_hour";
    public static final int DEFAULT_AUTORUN_END_HOUR = 9;
    public static final String SETTING_AUTORUN_END_MINUTE = "autorun_end_minute";
    public static final int DEFAULT_AUTORUN_END_MINUTE = 30;

    public static final String SETTING_IGNORE_BATTERY_OPTIMIZATIONS = "ignore_battery_optimizations";
    public static final int SETTING_IGNORE_BATTERY_OPTIMIZATIONS_DISABLED = 0;
    public static final int SETTING_IGNORE_BATTERY_OPTIMIZATIONS_ENABLED = 1;

    public static final String SETTING_DISPLAY_WIDTH = "display_width";
    public static final int DEFAULT_DISPLAY_WIDTH = 720;

    public static final String IMAGE_FOLDER = "TuneURL/Images";
    public static final String IMAGE_CACHE_FOLDER = "TuneURL/ImageCache";

    public static final int REDUCED_SAMPLE_SIZE_FULL_SMALL = 1;
    public static final int REDUCED_SAMPLE_SIZE_FULL_MEDIUM = 2;
    public static final int REDUCED_SAMPLE_SIZE_FULL_LARGE = 4;

    public static final long MAX_PIC_SIZE_SMALL = 0;
    public static final long MAX_PIC_SIZE_MEDIUM = 2304000L;
    public static final long MAX_PIC_SIZE_LARGE = 5992704L;

    public static final int AUDIO_FILE_NUM_CHANNELS = 1;
    public static final int AUDIO_FILE_MP3_SAMPLE_RATE = 44100;
    public static final int AUDIO_FILE_BITRATE = 128;
    public static final int AUDIO_FILE_MODE = 1;
    public static final int AUDIO_FILE_QUALITY = 0;

    public static final int AUDIO_FILE_CREATED = 0;
    public static final int AUDIO_FILE_SENT = 1;

    public static final long SWIPE_WAIT_TIME = 10000L;

    //public static final String AUDIO_FILE_ID = "audio_file_id";
    //public static final String AUDIO_FILE_NAME = "audio_file_name";
    public static final String AUDIO_FILE_PATH = "audio_file_name";
    public static final String USER_RESPONSE = "user_response";
    public static final String USER_RESPONSE_NO = "no";
    public static final String USER_RESPONSE_YES = "yes";
    public static final String FINGERPRINT = "fingerprint";


    public static final String ACTION_SAVE_PAGE = "save_page";
    public static final String ACTION_OPEN_PAGE = "open_page";
    public static final String ACTION_PHONE = "phone";
    public static final String ACTION_POLL = "poll";
    public static final String ACTION_COUPON = "coupon";
    public static final String ACTION_MAP = "map";


    public static final int TYPE_SAVED_PAGE = 1;
    public static final int TYPE_COUPON_NEW = 2;
    public static final int TYPE_COUPON_USED = 3;


    public static final String PHONE_NUMBER = "PHONE_NUMBER";

    public static final int AUTORUN_START_JOB_ID = 0;
    public static final int AUTORUN_STOP_JOB_ID = 1;

    public static final String AUTORUN_STARTED = "autorun_started";

    public static final String INTEREST_ACTION_HEARD = "heard";
    public static final String INTEREST_ACTION_INTERESTED = "interested";
    public static final String INTEREST_ACTION_ACTED = "acted";
    public static final String INTEREST_ACTION_SHARED = "shared";

    public static final String TUNEURL_ID = "TuneURL_ID";
    public static final String INTEREST_ACTION = "Interest_action";
    public static final String DATE = "date";

    public static final String HEADSET_EVENT = "headset_event";
    public static final int HEADSET_UNPLUGGED = 0;
    public static final int HEADSET_PLUGGED = 1;

    public static final String AUDIO_SOURCE = "audio_source";

    public static final int FINGERPRINT_MATCHING_THRESHOLD = 3;
}
