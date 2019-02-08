package euphoria.psycho.knife;

import android.Manifest;
import android.content.Intent;
import android.os.Environment;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import euphoria.psycho.common.C;
import euphoria.psycho.common.ContextUtils;
import euphoria.psycho.common.StorageUtils;
import euphoria.psycho.common.base.BaseActivity;

import static android.os.Environment.DIRECTORY_MUSIC;

public class MainActivity extends BaseActivity {

    private static final int REQUEST_CODE_PERMISSION = 1;
    private DrawerLayout mDrawerLayout;

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()) {
            case R.id.action_internal_storage:

                DirectoryFragment.show(getSupportFragmentManager(), Environment.getExternalStorageDirectory().getAbsolutePath());
                break;
            case R.id.action_sdcard:

                DirectoryFragment.show(getSupportFragmentManager(), StorageUtils.getSDCardPath());
                break;
            case R.id.action_download:
                DirectoryFragment.show(getSupportFragmentManager(), new File(Environment.getExternalStorageDirectory(), "Download").getAbsolutePath());
                break;
        }


        return true;
    }

    @Override
    protected void initialize() {
        setContentView(R.layout.activity_main);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        DirectoryFragment.show(getSupportFragmentManager());
        String treeUri = ContextUtils.getAppSharedPreferences().getString(C.KEY_TREE_URI, null);

        if (treeUri == null)
            StorageUtils.requestTreeUri(this, REQUEST_CODE_PERMISSION);
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
            StorageUtils.keepPermission(this, data);
            ContextUtils.getAppSharedPreferences().edit().putString(C.KEY_TREE_URI, data.getDataString()).apply();
        }
    }
}
