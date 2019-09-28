package euphoria.psycho.knife;

import android.content.Intent;

import androidx.annotation.Nullable;
import euphoria.common.Documents;
import euphoria.psycho.common.C;
import euphoria.psycho.common.base.BaseActivity;
import euphoria.psycho.common.base.BaseFragment;
import euphoria.psycho.knife.download.DownloadFragment;
import euphoria.psycho.share.util.ContextUtils;

public class MainActivity extends BaseActivity {

    private static final int REQUEST_CODE_PERMISSION = 1;
    private static final String TAG = "TAG/" + MainActivity.class.getSimpleName();

    private void handleIntent() {
        Intent intent = getIntent();

            DirectoryFragment.show(getSupportFragmentManager());
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
       if( euphoria.psycho.knife.util.FileUtils.hasSDCardPath()) {
           String treeUri = ContextUtils.getAppSharedPreferences().getString(C.KEY_TREE_URI, null);

           if (treeUri == null)
               Documents.requestTreeUri(this, REQUEST_CODE_PERMISSION);
       }
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
            Documents.keepPermission(this, data);
            ContextUtils.getAppSharedPreferences().edit().putString(C.KEY_TREE_URI, data.getDataString()).apply();
        }
    }
}
