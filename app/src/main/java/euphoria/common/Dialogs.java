package euphoria.common;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.EditText;

public class Dialogs {

    public static void showDialog(Context context,
                                  String placeHolder,
                                  Listener listener) {
        EditText editText = new EditText(context);
        editText.setText(placeHolder);
        new AlertDialog.Builder(context)
                .setView(editText)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (listener != null) listener.onPositive(editText.getText());
                })
                .show();

    }

    public interface Listener {
        void onPositive(CharSequence text);
    }
}
