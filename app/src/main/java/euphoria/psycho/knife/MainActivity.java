package euphoria.psycho.knife;

import android.content.Intent;

import androidx.annotation.Nullable;
import euphoria.psycho.common.C;
import euphoria.psycho.common.FileUtils;
import euphoria.psycho.common.base.BaseActivity;
import euphoria.psycho.common.base.BaseFragment;
import euphoria.psycho.knife.download.DownloadFragment;
import euphoria.psycho.knife.video.VideoFragment;
import euphoria.psycho.share.util.ContextUtils;

public class MainActivity extends BaseActivity {

    private static final int REQUEST_CODE_PERMISSION = 1;
    private static final String TAG = "TAG/" + MainActivity.class.getSimpleName();

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent.getData() != null) {

            VideoFragment.show(getSupportFragmentManager(), intent.getData().getPath(), C.SORT_BY_NAME, -1);
        } else {
            DirectoryFragment.show(getSupportFragmentManager());
        }
    }

    private void showVideoFragment() {
        VideoFragment.show(getSupportFragmentManager(), null, C.SORT_BY_NAME, 0);
    }

    private void showDownloadFragment() {
        BaseFragment.show(new DownloadFragment(), getSupportFragmentManager(), R.id.container, null);
    }

    @Override
    protected void initialize() {
        setContentView(R.layout.activity_main);

//        if (DEBUG) {
//            DocumentFile documentFile = StorageUtils.getDocumentFileFromTreeUri(this, StorageUtils.getTreeUri().toString(), new File(StorageUtils.getSDCardPath(), "经典/其他"));
//            Log.e(TAG, "initialize: " + documentFile.isDirectory()
//                    + "\n" + documentFile.getUri().toString());
//        }
        String treeUri = ContextUtils.getAppSharedPreferences().getString(C.KEY_TREE_URI, null);

        if (treeUri == null)
            FileUtils.requestTreeUri(this, REQUEST_CODE_PERMISSION);
        // showVideoFragment();
        // handleIntent();

        handleIntent();
    }

    @Override
    protected String[] needsPermissions() {
        return new String[]{"android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"};
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
