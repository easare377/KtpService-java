package com.emmanuel.utils;

import static android.content.Context.BATTERY_SERVICE;
import static android.content.Context.CLIPBOARD_SERVICE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

//import com.android.volley.VolleyError;
import com.emmanuel.utils.models.Size;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import org.threeten.bp.LocalDate;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import de.undercouch.bson4jackson.BsonFactory;
import wseemann.media.FFmpegMediaMetadataRetriever;

import com.jaredrummler.android.device.DeviceName;

/**
 * Created by Emmanuel on 11/7/2019.
 */

public class Utils {

    /**
     * Converts android dip to pixel format.
     *
     * @param dp      The value in dip.
     * @param context The application context.
     */
    public static int dpToPx(int dp, Context context) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale);
    }

    /**
     * Extracts string after specified character.
     * @param value  The string value to extract from.
     * @param ch The character that the string value will be extracted after.
     */
    public static String getStringAfterLastChar(String value, char ch) {
        return value.substring(value.lastIndexOf(ch) + 1);
    }

    /**
     * Sets the first letter of a string to uppercase.
     * @param value  The string value.
     */
    public static String firstLetterToUpperCase(String value) {
        String s1 = value.substring(0, 1).toUpperCase();
        value = s1 + value.substring(1).toLowerCase();
        return value;
    }

//    public static String parseVolleyError(VolleyError error) {
//        try {
//            String responseBody = new String(error.networkResponse.data, "utf-8");
////            JSONObject data = new JSONObject(responseBody);
////            JSONArray errors = data.getJSONArray("errors");
////            JSONObject jsonMessage = errors.getJSONObject(0);
////            String message = jsonMessage.getString("message");
//            return responseBody;
//        } catch (UnsupportedEncodingException errorr) {
//            return null;
//        }
//    }

    /**
     * Converts an object to json string.
     * @param object The object to serialize.
     * @return Json string of an object.
     */
    public static String serializeObjectToJson(Object object) {
        return new Gson().toJson(object);
    }

    /**
     * Deserializes a json string to an object of specified type.
     * @param typeParameterClass The type of object to deserialize to.
     * @return Json string of an object.
     */
    public static <T> T deserializeObjectFromJson(Class<T> typeParameterClass, String json) {
        return new Gson().fromJson(json, typeParameterClass);
    }


    public static String getDay(LocalDate localDate) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);
        if (localDate.getYear() == LocalDate.now().getYear() && localDate.getDayOfYear() == LocalDate.now().getDayOfYear()) {
            return "Today";
        } else if (localDate.getYear() == yesterday.get(Calendar.YEAR) && localDate.getDayOfYear() == yesterday.get(Calendar.DAY_OF_YEAR)) {
            return "Yesterday";
        } else if (localDate.getYear() == tomorrow.get(Calendar.YEAR) && localDate.getDayOfYear() == tomorrow.get(Calendar.DAY_OF_YEAR)) {
            return "Tomorrow";
        }
        return "";
    }

    /**
     * Return pseudo unique ID
     *
     * @return ID
     */
    public static UUID getUniquePsuedoID() {
        // If all else fails, if the user does have lower than API 9 (lower
        // than Gingerbread), has reset their device or 'Secure.ANDROID_ID'
        // returns 'null', then simply the ID returned will be solely based
        // off their Android device information. This is where the collisions
        // can happen.
        // Thanks http://www.pocketmagic.net/?p=1662!
        // Try not to use DISPLAY, HOST or ID - these items could change.
        // If there are collisions, there will be overlapping data
        String m_szDevIDShort = "35" + (Build.BOARD.length() % 10) + (Build.BRAND.length() % 10) + (Build.CPU_ABI.length() % 10) + (Build.DEVICE.length() % 10) + (Build.MANUFACTURER.length() % 10) + (Build.MODEL.length() % 10) + (Build.PRODUCT.length() % 10);

        // Thanks to @Roman SL!
        // https://stackoverflow.com/a/4789483/950427
        // Only devices with API >= 9 have android.os.Build.SERIAL
        // http://developer.android.com/reference/android/os/Build.html#SERIAL
        // If a user upgrades software or roots their device, there will be a duplicate entry
        String serial = null;
        try {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();

            // Go ahead and return the serial for api => 9
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode());
        } catch (Exception exception) {
            // String needs to be initialized
            serial = "serial"; // some value
        }

        // Thanks @Joe!
        // https://stackoverflow.com/a/2853253/950427
        // Finally, combine the values we have found by using the UUID class to create a unique identifier
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode());
    }

    public static String millisToDateFormat(long millis, String format) {
        // New date object from millis
        Date date = new Date(millis);
        // formattter
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        // Pass date object
        return formatter.format(date);
    }

    public static double calcPercentage(double dividend, double divisor) {
        return (dividend / divisor) * 100.0;
    }

    public static boolean isEvenNumber(int value) {
        return value % 2 == 0;
    }

    /**
     * Converts 16 bits integer to bytes.
     * @param value The number to convert.
     * @param byteOrder Indicates a big endian byte order or little endian byte order.
     * @return Bytes representing the integer.
     */
    public static byte[] uInt16ToBytes(int value, ByteOrder byteOrder) {
        if (value > 65535) {
            throw new IllegalArgumentException("value cannot be greater than 65535!");
        }
        byte[] longBytes;
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(byteOrder);
        bb.putInt(value);
        longBytes = bb.array();
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return Arrays.copyOfRange(longBytes, 2, 4);
        } else {
            return Arrays.copyOfRange(longBytes, 0, 2);
        }
    }

    /**
     * Converts unsigned 32 bits integer to bytes.
     * @param value The number to convert.
     * @param byteOrder Indicates a big endian byte order or little endian byte order.
     * @return Bytes representing the integer.
     */
    public static byte[] uInt32ToBytes(long value, ByteOrder byteOrder) {
        if (value > 4294967295L) {
            throw new IllegalArgumentException("value cannot be greater than 4294967295!");
        }
        byte[] longBytes;
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.order(byteOrder);
        bb.putLong(value);
        longBytes = bb.array();
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return Arrays.copyOfRange(longBytes, 4, 8);
        } else {
            return Arrays.copyOfRange(longBytes, 0, 4);
        }
    }

    /**
     * Converts bytes to unsigned 32 bit integer.
     * @param data The bytes to convert.
     * @param isBigEndian Indicates a big endian byte order or little endian byte order.
     * @return Integer value.
     */
    public static long bytesToUInt32(byte[] data, boolean isBigEndian) {
        return bytesToInt(data, isBigEndian) & 0xffffffffL;
    }

    /**
     * Converts bytes to 32 bit integer.
     * @param data The bytes to convert.
     * @param isBigEndian Indicates a big endian byte order or little endian byte order.
     * @return Integer value.
     */
    public static int bytesToInt(byte[] data, boolean isBigEndian) {
        if (data.length != 4)
            throw new IllegalArgumentException();
        int ch1 = data[0] & 0xFF;
        int ch2 = data[1] & 0xFF;
        int ch3 = data[2] & 0xFF;
        int ch4 = data[3] & 0xFF;
        if (isBigEndian)
            return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4));
        else
            return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1));
    }

    /**
     * Converts bytes to unsigned 16 bit integer.
     * @param data The bytes to convert.
     * @param isBigEndian Indicates a big endian byte order or little endian byte order.
     * @return Integer value.
     */
    public static int bytesToUInt16(byte[] data, boolean isBigEndian) {
        return bytesToInt16(data, isBigEndian) & 0xffff;
    }

    /**
     * Converts bytes to 32 bit integer.
     * @param data The bytes to convert.
     * @param isBigEndian Indicates a big endian byte order or little endian byte order.
     * @return Integer value.
     */
    public static int bytesToInt16(byte[] data, boolean isBigEndian) {
        if (data.length != 2)
            throw new IllegalArgumentException();
        int ch1 = data[0] & 0xFF;
        int ch2 = data[1] & 0xFF;
        if (isBigEndian)
            return ((ch1 << 8) + (ch2));
        else
            return ((ch2 << 8) + (ch1));
    }

    /**
     * Converts 32 bits integer to bytes.
     * @param value The number to convert.
     * @param byteOrder Indicates a big endian byte order or little endian byte order.
     * @return Bytes representing the integer.
     */
    public static byte[] int32ToBytes(long value, ByteOrder byteOrder) {
        byte[] longBytes;
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(byteOrder);
        bb.putLong(value);
        longBytes = bb.array();
        return Arrays.copyOfRange(longBytes, 4, 8);
    }

    /**
     * Gets the ip address of all the network interfaces.
     * @return Ip address of network interfaces.
     */
    public static String[] getCurrentDeviceIpAddress() throws SocketException {
        List<String> ipAddressList = new ArrayList<>();
        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
            NetworkInterface intf = en.nextElement();
            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                InetAddress inetAddress = enumIpAddr.nextElement();
                if (!inetAddress.isLoopbackAddress()) {
                    //ipAddress = inetAddress.getHostAddress().toString();
                    ipAddressList.add(inetAddress.getHostAddress());
                }
            }
        }
        String[] ipAddresses = new String[ipAddressList.size()];
        ipAddresses = ipAddressList.toArray(ipAddresses);
        return ipAddresses;
    }

    /**
     * Indicates if a remote network is reachable.
     * @param localIpAddress The local ip address that the remote device is connected to.
     * @param timeoutMillis Indicates a big endian byte order or little endian byte order.
     * @return Bytes representing the integer.
     */
    public static boolean isIpReachable(String localIpAddress, String remoteIpAddress, int timeoutMillis) throws IOException {
        NetworkInterface ni = NetworkInterface
                .getByInetAddress(InetAddress.getByName(localIpAddress));
        InetAddress pingAddr = InetAddress.getByName(remoteIpAddress);
        return pingAddr.isReachable(ni, 200, timeoutMillis);
    }

    private static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    public static double getBatteryPercentage(Context context) {
        if (Build.VERSION.SDK_INT >= 21) {
            BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, iFilter);
            int level = batteryStatus != null ?
                    batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
            int scale = batteryStatus != null ?
                    batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
            double batteryPct = level / (double) scale;

            return (batteryPct * 100);
        }
    }

    public static byte[] toBson(Object obj) throws IOException {
        //serialize data
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectMapper mapper = new ObjectMapper(new BsonFactory());
        mapper.writeValue(baos, obj);
        return baos.toByteArray();
    }

    public static <T> T fromBson(Class<T> typeParameterClass, byte[] bsonData) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(
                bsonData);
        ObjectMapper mapper = new ObjectMapper(new BsonFactory());
        return mapper.readValue(bais, typeParameterClass);
    }

    public static String getClipboardPrimaryText(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        String clipboardText = "";
        if (clipboard.hasPrimaryClip()) {
            ClipData data = clipboard.getPrimaryClip();
            if (data != null && data.getItemCount() > 0) {
                CharSequence text = data.getItemAt(0).coerceToText(context);
                if (text != null) {
                    clipboardText = text.toString();
                }
            }
        }
        return clipboardText;
    }

    public static byte[] convertIntToBytes(int value, ByteOrder byteOrder) {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.order(byteOrder);
        b.putInt(value);
        return b.array();
    }

    public static String getRandomPin(int charLength) {
        Random rnd = new Random();
        int max = 9;
        int min = 0;
        int no;//rnd.nextInt(max - min) + min;
        StringBuilder generatedPin = new StringBuilder(); //String.valueOf(no);
        for (int i = 0; i < charLength; i++) {
            no = rnd.nextInt(max - min) + min;
            generatedPin.append(no);
        }
        return generatedPin.toString();
    }

    public static String getDeviceName() {
        return DeviceName.getDeviceName();
    }
}

