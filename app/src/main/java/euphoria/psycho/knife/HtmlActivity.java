package euphoria.psycho.knife;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;


import androidx.annotation.Nullable;

public class HtmlActivity extends Activity {
    private WebView mWebView;


    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context;
        mWebView = new WebView(this);
        setContentView(mWebView);
        mWebView.loadUrl(getIntent().getData().toString());

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.scrollTo(0, PreferenceManager.getDefaultSharedPreferences(HtmlActivity.this)
                        .getInt("scrollY", 0));
            }
        });
    }

    @Override
    protected void onPause() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putInt("scrollY", mWebView.getScrollY()).apply();
        super.onPause();
    }
}
