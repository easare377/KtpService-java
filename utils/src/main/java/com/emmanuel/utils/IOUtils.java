package com.emmanuel.utils;

import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class IOUtils {
    /**
     * Creates a directory on an external storage device.
     * @param path The path of the directory.
     */
    public static boolean createDir(String path) {
        String fullPath = Environment.getExternalStorageDirectory().getPath() + path;
        File fPath = new File(fullPath);
        if (fPath.exists()) {
            return true;
        }
        return fPath.mkdir();
    }


    /**
     * Extracts the filename from a url.
     * @param uri .
     */
    public static String getFilenameFromUri(String uri) {
        return uri.substring(uri.lastIndexOf('/') + 1);
    }

    /**
     * Sets the first letter of a string to uppercase.
     * @param inputStream  The string value.
     */
    public static String convertStreamToString(InputStream inputStream) throws IOException {
        /*
         * To convert the InputStream to String we use the
         * BufferedReader.readLine() method. We iterate until the BufferedReader
         * return null which means there's no more data to read. Each line will
         * appended to a StringBuilder and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();

        String line; //= null;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
//        try {
//            while ((line = reader.readLine()) != null) {
//                sb.append(line);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                is.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        return sb.toString();
    }


    /**
     * Downloads string resource from a host.
     * @param uri The url containing string resource.
     */
    public static String downloadString(String uri) throws IOException {
        String responseString = null;
        URL url = new URL(uri);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == HttpsURLConnection.HTTP_OK) {
            // Do normal input or output stream reading
            responseString = convertStreamToString(conn.getInputStream());
        }
        return responseString;
    }

    /**
     * Downloads a file resource from a host and writes directly to an output stream and closes the file.
     * @param uri The url containing file resource.
     * @param fileOutputStream The output stream to write the file to.
     * @return a boolean indicating successful download.
     * @apiNote If the file already exists, the file is overwritten.
     */
    public static boolean downloadFile(String uri, FileOutputStream fileOutputStream) throws Exception {
        try {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                // Do normal input or output stream reading
                byte[] buffer = new byte[1024];
                int byteCount;
                InputStream inpstr = conn.getInputStream();
                try {
                    while ((byteCount = inpstr.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, byteCount);
                    }
                    fileOutputStream.flush();
                } finally {
                    fileOutputStream.close();
                    inpstr.close();
                }
            } else if (responseCode == HttpsURLConnection.HTTP_NOT_FOUND) {
                throw new Resources.NotFoundException("The requested resource does not exist");
            } else {
                throw new Exception("An error occurred on the server.");
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Downloads a file resource from a host and writes directly to an output stream and closes the file.
     * @param uri The url containing file resource.
     * @param file The file object to write the file to.
     * @return a boolean indicating successful download.
     * @apiNote If the file already exists, the file is overwritten.
     */
    public static boolean downloadFile(String uri, File file) throws Exception {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        return downloadFile(uri, fileOutputStream);
    }

    /**
     * Downloads a file resource from a host and writes directly to an output stream and closes the file.
     * @param uri The url containing file resource.
     * @param path The path to write the file to.
     * @return a boolean indicating successful download.
     * @apiNote If the file already exists, the file is overwritten.
     */
    public static boolean downloadFile(String uri, String path) throws Exception {
        FileOutputStream fileOutputStream = new FileOutputStream(path);
        return downloadFile(uri, fileOutputStream);
    }

    /**
     * Checks if file already exists.
     * @param path The path of the file.
     * @return a boolean indicating if the file exists.
     */
    public static boolean fileExists(String path) {
        File FPath = new File(path);
        return FPath.exists();
    }

    /**
     * Deletes a file.
     * @param path The path of the file.
     * @return a boolean indicating if the file was successfully deleted.
     */
    public static boolean deleteFile(String path) {
        File fPath = new File(path);
        return fPath.delete();
    }

    public static String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }


    /**
     * Reads an input stream into a byte array.
     * @param inputStream The input stream to read.
     * @param length The length of the bytes to read.
     * @return The number of bytes specified.
     */
    public static byte[] readInputStream(InputStream inputStream, int length) throws IOException {
        byte[] buffer = new byte[length];
        int totalBytesRead = 0;
        while (totalBytesRead != length) {
            int byteRead = inputStream.read(buffer, totalBytesRead, length - totalBytesRead);
            if (byteRead == -1) {
                throw new IOException();
            }
            totalBytesRead += byteRead;
        }
        return buffer;
    }

    public static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize;
        long totalBlocks;
        if (Build.VERSION.SDK_INT >= 18) {
            blockSize = stat.getBlockSizeLong();
            totalBlocks = stat.getBlockCountLong();
        } else {
            blockSize = stat.getBlockSize();
            totalBlocks = stat.getBlockCount();
        }
        return totalBlocks * blockSize;
    }

    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize;
        long availableBlocks;
        if (Build.VERSION.SDK_INT >= 18) {
            blockSize = stat.getBlockSizeLong();
            availableBlocks = stat.getAvailableBlocksLong();
        } else {
            blockSize = stat.getBlockSize();
            availableBlocks = stat.getAvailableBlocks();
        }
        return availableBlocks * blockSize;
    }

    public static long getTotalExternalMemorySize() throws IOException {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize;
            long totalBlocks;
            if (Build.VERSION.SDK_INT >= 18) {
                blockSize = stat.getBlockSizeLong();
                totalBlocks = stat.getBlockCountLong();
            } else {
                blockSize = stat.getBlockSize();
                totalBlocks = stat.getBlockCount();
            }
            return totalBlocks * blockSize;
        } else {
            throw new IOException("External storage not found!");
        }
    }

    public static long getAvailableExternalMemorySize() throws IOException {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize;
            long availableBlocks;
            if (Build.VERSION.SDK_INT >= 18) {
                blockSize = stat.getBlockSizeLong();
                availableBlocks = stat.getAvailableBlocksLong();
            } else {
                blockSize = stat.getBlockSize();
                availableBlocks = stat.getAvailableBlocks();
            }
            return availableBlocks * blockSize;
        } else {
            throw new IOException("External storage not found!");
        }
    }
}
