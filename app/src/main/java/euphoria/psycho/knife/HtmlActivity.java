package euphoria.psycho.knife;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
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


import androidx.annotation.Nullable;

public class HtmlActivity extends Activity {
    private WebView mWebView;

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


                            Log.e("TAG/", "Debug: onMenuItemClick, \n" + image);


                            return false;
                        }
                    });
        }
    }
}
