package euphoria.common;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Bitmaps {

    public static void saveAsJpg(Bitmap bitmap, File outputFile) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        bitmap.compress(CompressFormat.JPEG, 100, fileOutputStream);
        fileOutputStream.close();

    }
}
