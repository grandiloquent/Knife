package euphoria.psycho.share.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;

import euphoria.psycho.share.R;


public class DialogUtils {


    public static AlertDialog.Builder createFolderDialog(Context context,

                                                         DialogListener<CharSequence> listener) {

        EditText editText = new EditText(context);
        editText.setMaxLines(1);
        editText.setHint(context.getString(R.string.hint_new_folder_hint));
        editText.requestFocus();

        return new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_title_new_folder)
                .setView(editText)
                .setPositiveButton(android.R.string.ok, ((dialog, which) -> {
                    dialog.dismiss();
                    if (listener != null) {
                        listener.ok(editText.getText());
                    }
                }))
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                    if (listener != null) listener.cancel();
                });
    }

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
