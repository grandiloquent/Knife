package euphoria.psycho.common.base;

import android.app.Application;
import android.os.StrictMode;

import euphoria.psycho.share.util.ContextUtils;

public abstract class BaseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        ContextUtils.initApplicationContext(getApplicationContext());
    }
}
