package euphoria.psycho.knife.service;

import android.app.Notification;
import android.app.Notification.Builder;
import android.content.Context;

import euphoria.psycho.knife.R;

public class DeleteJob extends Job {


    public DeleteJob(Context context, String id, Listener listener) {
        super(context, id, listener);
    }

    @Override
    Builder createProgressBuilder() {
        return super.createProgressBuilder(
                mContext.getString(R.string.delete_notification_title),
                R.drawable.ic_menu_delete,
                mContext.getString(android.R.string.cancel),
                R.drawable.ic_cab_cancel);
    }

    @Override
    void finish() {

    }

    @Override
    Notification getProgressNotification() {
        return null;
    }

    @Override
    Notification getSetupNotification() {
        return null;
    }

    @Override
    void start() {

    }
}
