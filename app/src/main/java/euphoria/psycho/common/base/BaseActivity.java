package euphoria.psycho.common.base;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSION = 1;

    protected abstract void initialize();

    protected abstract String[] needsPermissions();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] needsPermissions = needsPermissions();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && needsPermissions != null && needsPermissions.length > 0) {
            List<String> permissions = new ArrayList<>();
            for (String permission : needsPermissions) {

                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(permission);
                }
            }
            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[0]), REQUEST_CODE_PERMISSION);
                return;
            }
        }
        initialize();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, permissions[i], Toast.LENGTH_LONG).show();
                return;
            }
        }
        initialize();
    }

    private OnBackPressedListener mOnBackPressedListener;

    @Override
    public void onBackPressed() {
        if (mOnBackPressedListener != null && mOnBackPressedListener.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        mOnBackPressedListener = onBackPressedListener;
    }

    public interface OnBackPressedListener {

        /**
         * Callback, which is called if the Back Button is pressed.
         * Fragments that extend MainFragment can/should override this Method.
         *
         * @return true if the App can be closed, false otherwise
         */
        boolean onBackPressed();
    }
}
