package euphoria.psycho.share.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static android.os.Environment.DIRECTORY_DCIM;

public class SystemUtils {

    public static File getCameraDirectory() {
        return new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM), "Camera");
    }

    public static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = {(byte) (0xff & hostAddress),
                (byte) (0xff & (hostAddress >> 8)),
                (byte) (0xff & (hostAddress >> 16)),
                (byte) (0xff & (hostAddress >> 24))};

        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }

    public static String getDeviceIP(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        try {
            @SuppressLint("MissingPermission") WifiInfo wifiInfo = wifiManager.getConnectionInfo();


            InetAddress inetAddress = intToInetAddress(wifiInfo.getIpAddress());

            return inetAddress.getHostAddress();
        } catch (Exception e) {

            Log.e("TAG/SystemUtils", "getDeviceIP: " + e.getMessage());

            return null;
        }
    }

    public static File getDCIMDirectory() {
        return Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM);
    }
}
