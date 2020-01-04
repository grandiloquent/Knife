package euphoria.psycho.knife.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;


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
                    if (listener != null) listener.cancel();
                });
    }

    public static void showKeyboard(Dialog dialog) {
        dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public interface DialogListener<T> {

        void ok(T t);

        void cancel();
    }
}
