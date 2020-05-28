package euphoria.psycho.knife;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;

import androidx.annotation.Nullable;
import euphoria.common.Strings;

public class HtmlActivity extends Activity {
    // 0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ
    static final String AB = "abcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();
    private WebView mWebView;

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void copyFile(FileInputStream fis, FileOutputStream fos) throws IOException {
        FileChannel fcis = fis.getChannel();
        FileChannel fcos = fos.getChannel();
        fcis.transferTo(0, fcis.size(), fcos);
        fis.close();
        fos.close();
    }

    static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWebView = new WebView(this);
        setContentView(mWebView);
        mWebView.loadUrl(getIntent().getData().toString());

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

            }
        });

        registerForContextMenu(mWebView);
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return;
        }
        super.onBackPressed();
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }
        FileInputStream fis = new FileInputStream(sourceFile);
        FileOutputStream fos = new FileOutputStream(destFile);
        copyFile(fis, fos);
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(contextMenu, v, menuInfo);
        final WebView.HitTestResult webViewHitTestResult = mWebView.getHitTestResult();

        if (webViewHitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                webViewHitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {

            contextMenu.setHeaderTitle("保存图片");

            contextMenu.add(0, 1, 0, "Save - Download Image")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {

                            String image = webViewHitTestResult.getExtra();

                            if (image.startsWith("file://")) {


                                File sourceFile = new File(Uri.parse(image).getPath());
                                String randomString = randomString(9);
                                String extension = Strings.substringAfterLast(image, ".");
                                File targetFile = new File(Environment.getExternalStorageDirectory(),
                                        "Servers/images/" + randomString + "." + extension);
                                try {
                                    copyFile(sourceFile, targetFile);
                                } catch (IOException e) {
                                }

                                Contexts.setText(randomString + "." + extension);

                            }


                            return false;
                        }
                    });
        }
    }
}
