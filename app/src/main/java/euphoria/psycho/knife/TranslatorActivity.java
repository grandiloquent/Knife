package euphoria.psycho.knife;

import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class TranslatorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MediaScannerConnection.scanFile(this, new String[]{new File(Environment.getExternalStorageDirectory(), "Pictures").getAbsolutePath()}, null,
                (path, uri) -> Log.e("TAG/", uri.toString()));
    }
}
