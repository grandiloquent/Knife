package euphoria.psycho.knife;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.Nullable;
import euphoria.common.Files;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class MarkdownActivity extends Activity {


    WebView mWebView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWebView = new WebView(this);
        mWebView.getSettings().setJavaScriptEnabled(true);

        setContentView(mWebView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));


        String filePath = getIntent().getStringExtra("file_path");
        try {
            InputStream is = new FileInputStream(filePath);
            String htm = Files.readToEnd(is, "UTF-8");
            List<Extension> extensions = Arrays.asList(TablesExtension.create());
            Parser parser = Parser.builder()
                    .extensions(extensions)
                    .build();
            Node document = parser.parse(htm);
            HtmlRenderer renderer = HtmlRenderer.builder()
                    .extensions(extensions)
                    .build();
            mWebView.loadData(renderer.render(document), "text/html", "UTF-8");
        } catch (Exception e) {

        }


    }
}
