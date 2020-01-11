package euphoria.server;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import euphoria.common.Https;
import euphoria.psycho.knife.DocumentUtils;

public class ServerActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String host = Https.getDeviceIP(this);


        Log.e("TAG/", "Debug: onCreate, \n" + host);

        DocumentUtils.startServer(host, 1235, Environment.getExternalStorageDirectory().getAbsolutePath());
    }
}
