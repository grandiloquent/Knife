package euphoria.psycho.share.util;

import android.os.Environment;

import java.io.File;

import static android.os.Environment.DIRECTORY_DCIM;

public class SystemUtils {

    public static File getCameraDirectory() {
        return new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM), "Camera");
    }

    public static File getDCIMDirectory() {
        return Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM);
    }
}
