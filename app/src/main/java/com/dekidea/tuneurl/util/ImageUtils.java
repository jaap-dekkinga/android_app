package com.dekidea.tuneurl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageUtils implements Constants {
	
	public static boolean resizeImage(Context context, String input_path, String output_path, int display_width) {
		
		boolean success = false;
		
		Bitmap new_image = null;
		Bitmap resized_image = null;		
		
		File file = null;
		FileInputStream fis = null;
		
		try {			
			
			file = new File(input_path);
			fis = new FileInputStream(file);
				
			if(fis != null){				
				
				BitmapFactory.Options options = new BitmapFactory.Options();
				
				options.inSampleSize = REDUCED_SAMPLE_SIZE_FULL_LARGE;
				
				Bitmap original_image = BitmapFactory.decodeStream(fis, null, options);	
				
				int pic_width = original_image.getWidth();
				int pic_height = original_image.getHeight();
				
				if(pic_width >= pic_height){
					
					int new_image_height = (int)((double)display_width / 5d);
					int new_image_width = (int)(((double)((double)pic_width / (double)pic_height)) * new_image_height);
					
					resized_image = Bitmap.createScaledBitmap(original_image, new_image_width, new_image_height, true);
					
					new_image = cropImageToSquare(resized_image, new_image_height);
				}
				else{
					
					int new_image_width = (int)((double)display_width / 5d);
					int new_image_height = (int)(((double)((double)pic_height / (double)pic_width)) * new_image_width);
					
					resized_image = Bitmap.createScaledBitmap(original_image, new_image_width, new_image_height, true);
					
					new_image = cropImageToSquare(resized_image, new_image_width);
				}			
			}
		} 
		catch (NullPointerException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		}
		finally{
			
			if(fis != null){
				
				try {
					
					fis.close();
				} 
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}   
		
		fis = null;
	        	    
		if(new_image != null){
			
			success = writeBitmap(new_image, output_path);
			
			if(file != null){
				
				file.delete();
			}
			
			new_image.recycle();
		}
		
		if(resized_image != null){
			
			resized_image.recycle();
		}
		
		return success;
	}
	
	
	public static Bitmap cropImageToSquare(Bitmap bm, int width){
		
		Bitmap cropped = null;
		
		if(bm != null){
			
			boolean needs_resizing = false;
			
			int abs_width = bm.getWidth();
			int abs_height = bm.getHeight();
			
			int margin_width = 0;
			int margin_height = 0;
			
			if(abs_width > width){
				 
				margin_width = (int)Math.round((abs_width - width) / 2);
				
				abs_width = width;
				
				needs_resizing = true;
			}
			
			if(abs_height > width){
				
				margin_height = (int)Math.round((abs_height - width) / 2);
				
				abs_height = width;
				
				needs_resizing = true;
			}		
				
			if(needs_resizing){
				
				cropped = Bitmap.createBitmap(bm, margin_width, margin_height, abs_width, abs_height);
				
				bm.recycle();
				
				bm = null;
			}
			else{
				
				cropped = bm;
			}
		}
		
		return cropped;
	}
		
	
	public static boolean writeBitmap(Bitmap bitmap, String file_path){
		
		boolean success = false;
		
		try {
			
			File file = new File(file_path);
			FileOutputStream fOut;
			
			fOut = new FileOutputStream(file);			
			
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
			
			fOut.flush();
			
			fOut.close();
			
			success = true;
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		}
		
		return success;
	}
}
