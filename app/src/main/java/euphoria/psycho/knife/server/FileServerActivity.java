package euphoria.psycho.knife.server;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.TextView;

import euphoria.psycho.common.base.BaseActivity;
import euphoria.psycho.knife.R;
import euphoria.psycho.knife.server.FileServer.LocalBinder;

public class FileServerActivity extends BaseActivity {

    private LocalBinder mLocalBinder;
    private TextView mTextView;

    @Override
    protected void initialize() {
        setContentView(R.layout.activity_file_server);
        mTextView = findViewById(R.id.url);
        Intent server = new Intent(this, FileServer.class);
        startService(server);
        bindService(server, mServiceConnection, BIND_AUTO_CREATE);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLocalBinder = (LocalBinder) service;
            mTextView.setText("服务器地址: " + mLocalBinder.getServerURL());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLocalBinder = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(mServiceConnection);
    }

    @Override
    protected String[] needsPermissions() {
        return new String[]{
                "android.permission.ACCESS_WIFI_STATE"
        };
    }
}
