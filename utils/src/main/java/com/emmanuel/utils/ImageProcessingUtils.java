package com.emmanuel.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import com.emmanuel.utils.models.Size;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class ImageProcessingUtils {

    /**
     * Resizes an image.
     * @param bitmap The image to resize.
     * @param newWidth The new width of the image.
     * @param newHeight The new height of the image.
     * @return The scaled bitmap image.
     */
    public static Bitmap setImageResolution(Bitmap bitmap, int newWidth, int newHeight) {
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
    }


    /**
     * Saves a bitmap object to disk.
     * @param filePath the path to save the image.
     * @param bitmap The image to resize.
     * @param format The compression format to save the file.
     * @param quality Indicates the compression leve of the image.
     * @apiNote PNG is a lossless format, the compression factor (100) is ignored.
     */
    public static void saveBitmap(String filePath, Bitmap bitmap, Bitmap.CompressFormat format, int quality) throws FileNotFoundException {
        FileOutputStream outputStream = new FileOutputStream(filePath);
        bitmap.compress(format, quality, outputStream); // bmp is your Bitmap instance
    }

    public static int getScaledImageWidth(double scaleFactor, int height) {
        return (int) (scaleFactor * (double) height);
    }

    /**
     * Computes the new dimension of an image maintaining the aspect ratio.
     * @param maxDimension The max dimension (width or height) of the image to scale.
     * @param bitmap The image.
     * @return The new width and height of the image.
     */
    public static Size getScaledDimension(Bitmap bitmap, int maxDimension) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth;
        int newHeight;
        double aspectRatio = (double)width / (double)height;
        if (width > height) {
            newWidth = maxDimension;
            newHeight = (int)(maxDimension / aspectRatio);
        }else {
            newHeight = maxDimension;
            newWidth = (int)(maxDimension * aspectRatio);
        }
        return new Size(newWidth, newHeight);
    }

    /**
     * Resizes a bitmap object maintaining the aspect ratio.
     * @param maxDimension The max dimension (width or height) of the image to scale.
     * @param bitmap The image to resize.
     * @return The resized image.
     */
    public static Bitmap getScaledBitmap(Bitmap bitmap, int maxDimension) {
        Size dimen = getScaledDimension(bitmap, maxDimension);
        return Bitmap.createScaledBitmap(bitmap, dimen.getWidth(), dimen.getHeight(), false);
    }

    /**
     * Gets the orientation of an image.
     * @param imagePath The path of the image.
     * @return The rotation degrees of the image.
     */
    public static int getImageOrientation(String imagePath) throws IOException {
        ExifInterface exif = new ExifInterface(imagePath);
        int rotate = 0;
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
        }
        return rotate;
    }

    /**
     * Rotates an image.
     * @param bitmap The image to rotate.
     * @return The rotated image.
     */
    public static Bitmap rotateImage(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        matrix.postRotate(orientation);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return bitmap;
    }

    /**
     * Generates a thumbnail from a video.
     * @param path the path to the Video
     * @return a thumbnail of the video or null if retrieving the thumbnail failed.
     */
    public static Bitmap getVideoThumbnail(String path) {
        Bitmap bitmap = null;

        FFmpegMediaMetadataRetriever fmmr = new FFmpegMediaMetadataRetriever();

        try {
            fmmr.setDataSource(path);

            final byte[] data = fmmr.getEmbeddedPicture();

            if (data != null) {
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            }

            if (bitmap == null) {
                bitmap = fmmr.getFrameAtTime();
            }
        } catch (Exception ignored) {
        } finally {
            fmmr.release();
        }
        return bitmap;
    }

    /**
     * Converts bitmap object to bytes.
     * @param bmp The bitmap to convert.
     * @param format The compression format to save the file.
     * @param quality Indicates the compression leve of the image.
     * @return Bytes representing the integer.
     */
    public static byte[] convertBitmapToBytes(Bitmap bmp, Bitmap.CompressFormat format, int quality) throws IOException {
        try (ByteArrayOutputStream bmpStream = new ByteArrayOutputStream()) {
            bmp.compress(format, quality, bmpStream);
            return bmpStream.toByteArray();
        }
    }
}
