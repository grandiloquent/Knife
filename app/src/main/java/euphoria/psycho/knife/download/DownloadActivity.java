package euphoria.psycho.knife.download;

import android.content.Intent;

import androidx.annotation.Nullable;
import euphoria.psycho.common.C;
import euphoria.psycho.common.FileUtils;
import euphoria.psycho.common.base.BaseActivity;
import euphoria.psycho.common.base.BaseFragment;
import euphoria.psycho.knife.R;
import euphoria.psycho.share.util.ContextUtils;


public class DownloadActivity extends BaseActivity {

    private static final int REQUEST_CODE_PERMISSION = 1;
    private static final String TAG = "TAG/" + euphoria.psycho.knife.MainActivity.class.getSimpleName();


    private void showDownloadFragment() {
        BaseFragment.show(new DownloadFragment(), getSupportFragmentManager(), R.id.container, null);
    }

    @Override
    protected void initialize() {
        setContentView(R.layout.activity_main);

//
        String treeUri = ContextUtils.getAppSharedPreferences().getString(C.KEY_TREE_URI, null);

        if (treeUri == null)
            FileUtils.requestTreeUri(this, REQUEST_CODE_PERMISSION);

        showDownloadFragment();
    }

    @Override
    protected String[] needsPermissions() {
        return new String[]{"android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.WAKE_LOCK",
                "android.permission.INTERNET"};
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PERMISSION && resultCode == RESULT_OK) {
            FileUtils.keepPermission(this, data);
            ContextUtils.getAppSharedPreferences().edit().putString(C.KEY_TREE_URI, data.getDataString()).apply();
        }
    }
}
