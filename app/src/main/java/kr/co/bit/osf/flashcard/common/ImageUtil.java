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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kr.co.bit.osf.flashcard.db.CardDTO;
import kr.co.bit.osf.flashcard.db.FlashCardDB;

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

    public static List<File> getImageListFromDefaultAlbum(Context context) {
        List<File> imageList = new ArrayList<>();

        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATA
        };

        // content:// style URI for the "primary" external storage volume
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        // Make the query.
        Cursor cur = context.getContentResolver().query(images,
                projection, // Which columns to return
                null,       // Which rows to return (all rows)
                null,       // Selection arguments (none)
                null        // Ordering
        );
        if (cur == null) {
            return imageList;
        }

        if (cur.moveToFirst()) {
            String bucket;
            String data;

            int bucketColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

            int dataColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.DATA);

            do {
                // Get the field values
                bucket = cur.getString(bucketColumn);
                data = cur.getString(dataColumn);

                // Do something with the values.
                if (ALBUM_NAME.equals(bucket)) {
                    imageList.add(new File(data));
                }
            } while (cur.moveToNext());
        }

        return imageList;
    }

    public static void loadCardImageIntoImageView(Context context, CardDTO card, ImageView imageView) {
        if (card.getType() == FlashCardDB.CardEntry.TYPE_USER) {
            // load image from sd card(glide)
            Glide.with(context).load(card.getImagePath()).into(imageView);
        } else {
            // card demo data(glide)
            Glide.with(context).fromResource()
                    .load(card.getImageId()).into(imageView);
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
