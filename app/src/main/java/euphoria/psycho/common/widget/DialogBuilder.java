package euphoria.psycho.common.widget;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;


public class DialogBuilder {
    protected boolean mCancelable = true;
    public Context mContext;
    protected View mCustomView;
    protected String mMessage;
    protected DialogInterface.OnClickListener mNegativeButtonListener;
    protected String mNegativeButtonText;
    protected DialogInterface.OnClickListener mPositiveButtonListener;
    protected String mPositiveButtonText;
    protected String mTitle;
    protected boolean mindeterminate;

    public DialogBuilder(Context context) {
        mContext = context;
    }

    public Dialog create() {
        if (mindeterminate) {
            return createProgressDialog();
        }
        mPositiveButtonText = TextUtils.isEmpty(mPositiveButtonText)
                ? mContext.getString(android.R.string.ok) : mPositiveButtonText;
        mNegativeButtonText = TextUtils.isEmpty(mNegativeButtonText)
                ? mContext.getString(android.R.string.cancel) : mNegativeButtonText;

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(mMessage).setCancelable(mCancelable);
        builder.setPositiveButton(mPositiveButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (null != mPositiveButtonListener) {
                    mPositiveButtonListener.onClick(dialog, which);
                }
            }
        });
        builder.setNegativeButton(mNegativeButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (null != mNegativeButtonListener) {
                    mNegativeButtonListener.onClick(dialog, which);
                }
            }
        });
        if (!TextUtils.isEmpty(mTitle)) {
            builder.setTitle(mTitle);
        }
        if (null != mCustomView) {
            builder.setView(mCustomView);

        }
        Dialog dialog = builder.create();
        return dialog;
    }

    public Dialog createProgressDialog() {
        MaterialProgressDialog progressDialog = new MaterialProgressDialog(mContext);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(mCancelable);
        progressDialog.setMessage(mMessage);
        return progressDialog;
    }

    public DialogBuilder setCancelable(boolean cancelable) {
        this.mCancelable = cancelable;
        return this;
    }

    public void setIndeterminate(boolean indeterminate) {
        mindeterminate = indeterminate;
    }

    public DialogBuilder setMessage(String message) {
        this.mMessage = message;
        return this;
    }

    public DialogBuilder setMessage(int message) {
        this.mMessage = mContext.getString(message);
        return this;
    }

    public DialogBuilder setNegativeButton(int text, DialogInterface.OnClickListener onClickListener) {
        setNegativeButtonText(mContext.getString(text));
        setNegativeButtonListener(onClickListener);
        return this;
    }

    public DialogBuilder setNegativeButton(String text, DialogInterface.OnClickListener onClickListener) {
        setNegativeButtonText(text);
        setNegativeButtonListener(onClickListener);
        return this;
    }

    public DialogBuilder setNegativeButtonListener(DialogInterface.OnClickListener onClickListener) {
        this.mNegativeButtonListener = onClickListener;
        return this;
    }

    public DialogBuilder setNegativeButtonText(String text) {
        this.mNegativeButtonText = text;
        return this;
    }

    public DialogBuilder setPositiveButton(int text, DialogInterface.OnClickListener onClickListener) {
        setPositiveButtonText(mContext.getString(text));
        setPositiveButtonListener(onClickListener);
        return this;
    }

    public DialogBuilder setPositiveButton(String text, DialogInterface.OnClickListener onClickListener) {
        setPositiveButtonText(text);
        setPositiveButtonListener(onClickListener);
        return this;
    }

    public DialogBuilder setPositiveButtonListener(DialogInterface.OnClickListener onClickListener) {
        this.mPositiveButtonListener = onClickListener;
        return this;
    }

    public DialogBuilder setPositiveButtonText(String text) {
        this.mPositiveButtonText = text;
        return this;
    }

    public DialogBuilder setTitle(String title) {
        this.mTitle = title;
        return this;
    }

    public DialogBuilder setTitle(int title) {
        this.mTitle = mContext.getString(title);
        return this;
    }

    public DialogBuilder setView(View view) {
        this.mCustomView = view;
        return this;
    }

    public void show() {
        Dialog dialog = create();
        dialog.show();
        tintButtons(dialog);
    }

    public void showDialog() {
        show();
    }

    private static Button getButton(Dialog dialog, int which) {
        return ((AlertDialog) dialog).getButton(which);
    }

    public static void tintButtons(Dialog dialog) {
        TintUtils.tintButton(getButton(dialog, DialogInterface.BUTTON_POSITIVE));
        TintUtils.tintButton(getButton(dialog, DialogInterface.BUTTON_NEGATIVE));
        TintUtils.tintButton(getButton(dialog, DialogInterface.BUTTON_NEUTRAL));
    }
}
