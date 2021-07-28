package com.dekidea.tuneurl.util;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.dekidea.tuneurl.R;
import com.musicg.wave.Wave;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Vector;

public class FileUtils implements Constants {
    
    private static Wave[] referenceBenchmarkWaves;
    
    public static Wave[] getReferenceBenchmarkWaves(Context context){
        
        return referenceBenchmarkWaves;
    }
    
    public static void initializeReferenceBenchmarkWaves(Context context){
        
        if(referenceBenchmarkWaves == null){
            
            try{                

                String[] reference_files = FileUtils.getReferenceFilePaths(context);

                referenceBenchmarkWaves = new Wave[reference_files.length];
                
                for(int i=0; i<reference_files.length; i++){

                    referenceBenchmarkWaves[i] = new Wave(reference_files[i]);
                }
            }
            catch (Exception e){
                
                e.printStackTrace();
            }
        }
    }


    public static String[] getReferenceFilePaths(Context context){

        String[] paths = null;

        try {

            paths = new String[REFERENCE_FILES.length];

            for(int i=0; i<REFERENCE_FILES.length; i++) {

                String filepath = context.getFilesDir().getPath();
                File file = new File(filepath, AUDIO_RECORDER_FOLDER);

                if (!file.exists()) {

                    file.mkdirs();
                }

                paths[i] = file.getAbsolutePath() + "/" + REFERENCE_FILES[i];
            }
        }
        catch (Exception e){

            e.printStackTrace();
        }

        return paths;
    }


    public static String getTriggerFilePath(Context context){

        String filepath = context.getFilesDir().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if(!file.exists()){

            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + RECORDED_TRIGGER_FILE);
    }


    public static String getTriggerTempFilePath(Context context){

        String filepath = context.getFilesDir().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if(!file.exists()){

            file.mkdirs();
        }

        File tempFile = new File(filepath,RECORDED_TRIGGER_TEMP_FILE);

        if(tempFile.exists()){

            tempFile.delete();
        }

        return (file.getAbsolutePath() + "/" + RECORDED_TRIGGER_TEMP_FILE);
    }


    public static String getEncodedFilePath(Context context){

        String filepath = context.getFilesDir().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if(!file.exists()){

            file.mkdirs();
        }

        Calendar c = Calendar.getInstance();
        long time = c.getTimeInMillis();

        return (file.getAbsolutePath() + "/" + String.valueOf(time) + ".mp3");
    }


    public static String getAudioLabelTempFilePath(Context context){

        String filepath = context.getFilesDir().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if(!file.exists()){

            file.mkdirs();
        }

        String file_name = String.valueOf(TimeUtils.getCurrentTimeInMillis()) + ".raw";

        return (file.getAbsolutePath() + "/" + file_name);
    }


    public static void deleteFile(String file_path){

        try {

            File file = new File(file_path);

            if (file != null && file.exists()) {

                file.delete();
            }
        }
        catch(Exception e){

            e.printStackTrace();
        }
    }


    public static void installResources(Context context){

        try {

            int last_version_code = Settings.fetchIntSetting(context, VERSION_CODE, 0);

            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            int version_code = pInfo.versionCode;

            if(version_code > last_version_code){

                installReferenceWavFiles(context);

                Settings.updateIntSetting(context, VERSION_CODE, version_code);
            }
            else{

                installReferenceWavFiles(context);
            }
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }


    public static boolean installReferenceWavFiles(Context context){

        boolean success = true;

        for(int i=0; i<REFERENCE_RAW_RESOURCES.length;i++){

            success = success & installReferenceWavFile(context, REFERENCE_RAW_RESOURCES[i], REFERENCE_FILES[i]);
        }

        if(success){

            initializeReferenceBenchmarkWaves(context);
        }

        return success;
    }


    public static boolean installReferenceWavFile(Context context, int raw_resource, String file_name){

        boolean success = false;

        InputStream input_stream = null;

        try {

            input_stream = context.getApplicationContext().getResources().openRawResource(raw_resource);

            String output_folder_path = context.getFilesDir().getPath() + "/" + AUDIO_RECORDER_FOLDER;
            File output_folder = new File(output_folder_path);
            if (!output_folder.exists()) {

                output_folder.mkdirs();
            }

            String output_file_path = output_folder_path + "/" + file_name;

            success = writeFile(input_stream, output_file_path);
        }
        catch (Exception e) {

            e.printStackTrace();

            success = false;
        }
        finally {

            try {

                input_stream.close();
            }
            catch (IOException e) {

                e.printStackTrace();
            }
        }

        return success;
    }


    private static boolean writeFile(InputStream input_stream, String output_file_path){

        try {

            File out_file = new File(output_file_path);
            if (!out_file.exists()) {

                OutputStream output_stream = new FileOutputStream(output_file_path);

                byte[] buffer = new byte[1024];
                int length;

                while ((length = input_stream.read(buffer)) != -1) {

                    output_stream.write(buffer, 0, length);
                }

                output_stream.flush();
                output_stream.close();

                return true;
            }
        }
        catch(Exception e){

            e.printStackTrace();
        }

        return false;
    }


    private static boolean updateFile(InputStream input_stream, String output_file_path){

        try {

            File out_file = new File(output_file_path);
            if (out_file.exists()) {

                out_file.delete();
            }
        }
        catch(Exception e){

            e.printStackTrace();
        }

        try {

            File out_file = new File(output_file_path);
            if (!out_file.exists()) {

                OutputStream output_stream = new FileOutputStream(output_file_path);

                byte[] buffer = new byte[1024];
                int length;

                while ((length = input_stream.read(buffer)) > 0) {

                    output_stream.write(buffer, 0, length);
                }

                output_stream.flush();
                output_stream.close();

                System.out.println("FileUtils.updateFile()");

                return true;
            }
        }
        catch(Exception e){

            e.printStackTrace();
        }

        return false;
    }


    public static void deleteCacheSoundFiles(Context context){

        try{

            Vector<String> reference_files = new Vector<String>();

            for(int a=0; a<REFERENCE_FILES.length; a++){

                reference_files.add(REFERENCE_FILES[a]);
            }

            String outFolderName = context.getFilesDir().getPath() + "/" + AUDIO_RECORDER_FOLDER;
            File out_folder = new File(outFolderName);

            if(out_folder != null && out_folder.exists()){

                File[] files = out_folder.listFiles();

                if(files != null){

                    for(int i=0; i<files.length; i++){

                        File file = files[i];

                        String file_name = file.getName();

                        if(!reference_files.contains(file_name)){

                            try{

                                file.delete();
                            }
                            catch(Exception e){

                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e){

            e.printStackTrace();
        }
    }


    public static void deleteRecordedTempFile(Context context){

        try {

            String filepath = context.getFilesDir().getPath() + "/" + AUDIO_RECORDER_FOLDER + "/" + RECORDED_TEMP_FILE;
            File tempFile = new File(filepath);

            if (tempFile.exists()) {

                tempFile.delete();
            }
        }
        catch (Exception e){

            e.printStackTrace();
        }
    }
}
