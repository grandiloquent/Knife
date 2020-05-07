package euphoria.server;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

import androidx.annotation.Nullable;
import euphoria.common.Files;
import euphoria.common.Https;
import euphoria.psycho.knife.DocumentUtils;

public class ServerService extends Service {
    private void checkStaticFiles() {


        String[] files = new String[]{
                "app.css",
                "app.js",
                "ic_action_folder.png",
                "ic_action_insert_drive_file.png",
        };

        Arrays.stream(files).forEach(f -> {
            String fileName = Files.getExternalStoragePath("FileServer/" + f);
            if (Files.isFile(fileName)) {
                if (!fileName.endsWith(".css") && !fileName.endsWith(".js")) {
                    return;
                }
                try {
                    String assetMd5 = Files.getMD5Checksum(getAssets().open("static/" + f));

                    if (Files.getMD5Checksum(fileName).equals(assetMd5)) {
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                Files.createDirectoryIfNotExists(Files.getExternalStoragePath("FileServer"));
                Files.copyAssetFile(this, "static/" + f, fileName);
            } catch (IOException e) {
              }
        });


    }

    @Override
    public void onCreate() {
        super.onCreate();
        checkStaticFiles();
        String host = Https.getDeviceIP(getApplicationContext());
        Thread thread=new Thread(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            DocumentUtils.startServer(host, 1235, Environment.getExternalStorageDirectory().getAbsolutePath());
        });
        thread.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
