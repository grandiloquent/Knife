package euphoria.psycho.share.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import java.io.File;
import java.util.List;

import euphoria.psycho.share.R;


public class IntentUtils {

    private static final String TAG = "TAG/" + IntentUtils.class.getSimpleName();

    public static Intent createViewIntentForUri(
            Uri fileUri, String mimeType, String originalUrl, String referrer) {
        Intent fileIntent = new Intent(Intent.ACTION_VIEW);
        String normalizedMimeType = Intent.normalizeMimeType(mimeType);
        if (TextUtils.isEmpty(normalizedMimeType)) {
            fileIntent.setData(fileUri);
        } else {
            fileIntent.setDataAndType(fileUri, normalizedMimeType);
        }
        fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        fileIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        fileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        setOriginalUrlAndReferralExtraToIntent(fileIntent, originalUrl, referrer);
        return fileIntent;
    }

    public static void setOriginalUrlAndReferralExtraToIntent(
            Intent intent, String originalUrl, String referrer) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) return;
        if (originalUrl != null) {
            intent.putExtra(Intent.EXTRA_ORIGINATING_URI, Uri.parse(originalUrl));
        }
        if (referrer != null) intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse(originalUrl));
    }

    public static void shareFile(Context context, String path) {

        Intent intent = new Intent(Intent.ACTION_SEND);
        Uri uri = Uri.fromFile(new File(path));
        intent.setDataAndType(uri, MimeTypeUtils.getMimeTypeForIntent(path));
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(intent, context.getResources().getText(R.string.share)));
    }

    public static boolean isPackageInstalled(Context context, String packageName) {

        PackageManager packageManager = context.getPackageManager();

        List<ApplicationInfo> applicationInfos = packageManager.getInstalledApplications(0);
        for (ApplicationInfo app : applicationInfos) {
            if (app.packageName.equals(packageName)) return true;
        }
        return false;
    }
}
