package euphoria.psycho.knife;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import euphoria.common.Strings;

public class Utilities {



    public static boolean isNullOrWhiteSpace(String value) {
        if (value == null) return true;

        for (int i = 0; i < value.length(); i++) {
            if (!Character.isWhitespace(value.charAt(i))) return false;
        }

        return true;
    }

    public static String srt2txt(String fileName) throws IOException {
        FileInputStream in = new FileInputStream(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        Pattern numberLine = Pattern.compile("(^[0-9]+$)|(^[0-9]+[^a-zA-Z]*?[0-9]+$)");
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Strings.substringAfterLast(fileName,"/")).append("\r\n\r\n");
        while ((line = reader.readLine()) != null) {
            if (numberLine.matcher(line).matches()) continue;
            stringBuilder.append(line.trim()).append(' ');
        }

        return  stringBuilder.toString().replaceAll( "[\\.]+", ".\r\n\r\n");
    }

    public static void setClipboardText(Context context, String text) {
        if (context == null || TextUtils.isEmpty(text)) return;

        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager == null) return;
        ClipData clipData = ClipData.newPlainText(null, text);
        manager.setPrimaryClip(clipData);
    }
}

