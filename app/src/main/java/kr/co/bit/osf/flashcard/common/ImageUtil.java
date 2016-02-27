package kr.co.bit.osf.flashcard.common;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.co.bit.osf.flashcard.R;
import kr.co.bit.osf.flashcard.db.CardDTO;
import kr.co.bit.osf.flashcard.db.FlashCardDB;
import kr.co.bit.osf.flashcard.debug.Dlog;

public class ImageUtil {
    private static final String TAG = "ImageUtilLog";

    public static final String ALBUM_NAME = "FlashCard";

    // http://developer.android.com/intl/ko/guide/topics/media/camera.html
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a file Uri for saving an image or video */
    public static Uri getOutputMediaFileUri(int type){
        Log.i(TAG, "getOutputMediaFileUri(" + type + ")");
        return Uri.fromFile(getOutputMediaFile(type));
    }

    public static File getMediaStorageDir() {
        return new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), ALBUM_NAME);
    }

    public static Uri getMediaStorageDirUri(){
        return Uri.fromFile(getMediaStorageDir());
    }

    /** Create a File for saving an image or video */
    public static File getOutputMediaFile(int type){
        Log.i(TAG, "getOutputMediaFile(" + type + ")");
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        //File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
        //        Environment.DIRECTORY_PICTURES), "MyCameraApp");
        File mediaStorageDir = getMediaStorageDir();
        Log.i(TAG, "mediaStorageDir:" + mediaStorageDir);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.i(TAG, "failed to create directory:" + mediaStorageDir);
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    // http://stackoverflow.com/questions/7429228/check-whether-the-sd-card-is-available-or-not-programmatically
    public static boolean isSDPresent() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static void loadCardImageIntoImageView(Context context, CardDTO card, ImageView imageView) {
        Dlog.i(card.toString());
        int imageId = 0;
        try {
            if (card.getType() == FlashCardDB.CardEntry.TYPE_USER) {
                Dlog.i("user type");
                // load image from sd card(glide)
                File imageFile = new File(card.getImagePath());
                if (imageFile.exists()) {
                    Dlog.i("image file exist");
                    Glide.with(context).load(card.getImagePath()).into(imageView);
                } else {
                    Dlog.i("image file not found!");
                    imageId = R.drawable.default_image_empty_image;
                }
            } else {
                Dlog.i("demo type");
                // card demo data(glide)
                imageId = context.getResources().getIdentifier(card.getImageName(),
                        "drawable", context.getPackageName());
                if (imageId > 0) {
                    Glide.with(context).fromResource().load(imageId).into(imageView);
                } else {
                    imageId = R.drawable.default_image_empty_image;
                }
            }
        } catch (Exception e) {
            imageId = R.drawable.default_image_empty_image;
            Dlog.e(e.toString());
        }
        // empty image or not found
        if (imageId == R.drawable.default_image_empty_image) {
            Dlog.i("empty_image");
            Glide.with(context).fromResource().load(imageId).into(imageView);
        }
    }

    public static String getImagePathFromIntentData(Context context, Intent data) {
        Uri selectedImageUri = data.getData();
        String[] projection = { MediaStore.MediaColumns.DATA };
        CursorLoader cursorLoader = new CursorLoader(context, selectedImageUri, projection, null, null, null);
        Cursor cursor =cursorLoader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        String selectedImagePath = cursor.getString(column_index);
        Log.i(TAG, "selectedImagePath=" + selectedImagePath);

        return selectedImagePath;
    }

    // http://stackoverflow.com/questions/26570084/how-to-get-file-name-from-file-path-in-android
    public static String getNameFromPath(String pathName) {
        return pathName.substring(pathName.lastIndexOf("/") + 1);
    }
}
