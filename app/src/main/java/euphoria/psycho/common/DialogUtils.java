package euphoria.psycho.common;

import android.app.Dialog;
import android.content.Context;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import euphoria.psycho.knife.R;

public class DialogUtils {

    public static AlertDialog.Builder createSingleLineDialogBuilder(Context context,
                                                                    String editTextStr,
                                                                    DialogListener<CharSequence> listener) {

        EditText editText = new EditText(context);
        editText.setText(editTextStr);


        return new AlertDialog.Builder(context)
                .setView(editText)
                .setPositiveButton(android.R.string.ok, ((dialog, which) -> {
                    if (listener != null) {
                        listener.ok(editText.getText());
                    }
                }))
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    if (listener != null) listener.ignored();
                });
    }

    public static void showKeyboard(Dialog dialog) {
        dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public interface DialogListener<T> {

        void ok(T t);

        void ignored();
    }
}
