package euphoria.server;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import androidx.annotation.Nullable;
import euphoria.common.Files;
import euphoria.common.Https;
import euphoria.common.Threads;
import euphoria.psycho.knife.DocumentUtils;

public class ServerActivity extends Activity {
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
                Log.e("TAG/" + ServerActivity.this.getClass().getSimpleName(), "Error: checkStaticFiles, " + e.getMessage() + " " + e.getCause());
            }
        });


    }
    private Handler mHandler;
    private HandlerThread mHandlerThread;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkStaticFiles();
        String host = Https.getDeviceIP(this);

        mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        Log.e("TAG/", "Debug: onCreate, \n" + host);

        mHandler.post(() -> DocumentUtils.startServer(host, 1235, Environment.getExternalStorageDirectory().getAbsolutePath()));
    }

    @Override
    protected void onResume() {
        super.onResume();


    }


}
