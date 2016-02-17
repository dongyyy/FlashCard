package kr.co.bit.osf.flashcard.common;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public static void showImageFileInImageView(String imageFilePath, ImageView imageView) {
        String tagPrefix = "thumbnail : ";
        // http://developer.android.com/intl/ko/training/camera/photobasics.html
        /* Get the size of the ImageView */
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();
        Log.i(TAG, tagPrefix + "Get the size of the ImageView");
        Log.i(TAG, tagPrefix + "targetWidth : " + targetW);
        Log.i(TAG, tagPrefix + "targetHeight : " + targetH);

        /* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFilePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        Log.i(TAG, tagPrefix + "Get the size of the image");
        Log.i(TAG, tagPrefix + photoW);
        Log.i(TAG, tagPrefix + photoH);

		/* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        }
        Log.i(TAG, tagPrefix + "scaleFactor : " + scaleFactor);

		/* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        Log.i(TAG, tagPrefix + "Set bitmap options to scale the image decode target");

		/* Decode the JPEG file into a Bitmap */
        Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath, bmOptions);
        Log.i(TAG, tagPrefix + "Decode the JPEG file into a Bitmap");

		/* Associate the Bitmap to the ImageView */
        imageView.setImageBitmap(bitmap);
        Log.i(TAG, tagPrefix + "Associate the Bitmap to the ImageView");
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
