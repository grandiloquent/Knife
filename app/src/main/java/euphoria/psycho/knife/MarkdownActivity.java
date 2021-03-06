package euphoria.psycho.knife;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.InputStream;

import androidx.annotation.Nullable;
import euphoria.common.Files;
import euphoria.common.Views;

public class MarkdownActivity extends Activity {


    //WebView mWebView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScrollView scrollView = new ScrollView(this);
        int pad = Views.dp2px(this, 12);
        scrollView.setPadding(pad, pad, pad, pad);
        TextView textView = new TextView(this);
        scrollView.addView(textView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        textView.setTextIsSelectable(true);
        String filePath = getIntent().getStringExtra("file_path");
        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
            textView.setText(Files.readToEnd(is, "UTF-8"));
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        setContentView(scrollView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//        getWindow().setFlags(
//                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
//                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
//        mWebView = new WebView(this);
//        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
//        if (Build.VERSION.SDK_INT >= 19) {
//            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//        }
//        else {
//            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//        }
//        setContentView(mWebView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//
//
//        String filePath = getIntent().getStringExtra("file_path");
//        try {
//            InputStream is = new FileInputStream(filePath);
//            String htm = Files.readToEnd(is, "UTF-8");
//            if (htm == null) return;
//            List<Extension> extensions = Arrays.asList(TablesExtension.create());
//            Parser parser = Parser.builder()
//                    .extensions(extensions)
//                    .build();
//            Node document = parser.parse(htm);
//            HtmlRenderer renderer = HtmlRenderer.builder()
//                    .extensions(extensions)
//                    .build();
//            String data = renderer.render(document);
//            mWebView.loadData(data, "text/html", "UTF-8");
//        } catch (Exception e) {
//
//        }


    }
}
