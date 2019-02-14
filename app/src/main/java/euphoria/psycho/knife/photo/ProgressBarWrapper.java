package euphoria.psycho.knife.photo;

import android.view.View;
import android.widget.ProgressBar;

/**
 * This class wraps around two progress bars and is solely designed to fix
 * a bug in the framework (b/6928449) that prevents a progress bar from
 * gracefully switching back and forth between indeterminate and determinate
 * modes.
 */
public class ProgressBarWrapper {
    private final ProgressBar mDeterminate;
    private final ProgressBar mIndeterminate;
    private boolean mIsIndeterminate;

    public ProgressBarWrapper(ProgressBar determinate,
            ProgressBar indeterminate, boolean isIndeterminate) {
        mDeterminate = determinate;
        mIndeterminate = indeterminate;
        setIndeterminate(isIndeterminate);
    }

    public void setIndeterminate(boolean isIndeterminate) {
        mIsIndeterminate = isIndeterminate;

        setVisibility(mIsIndeterminate);
    }

    public void setVisibility(int visibility) {
        if (visibility == View.INVISIBLE || visibility == View.GONE) {
            mIndeterminate.setVisibility(visibility);
            mDeterminate.setVisibility(visibility);
        } else {
            setVisibility(mIsIndeterminate);
        }
    }

    private void setVisibility(boolean isIndeterminate) {
        mIndeterminate.setVisibility(isIndeterminate ? View.VISIBLE : View.GONE);
        mDeterminate.setVisibility(isIndeterminate ? View.GONE : View.VISIBLE);
    }

    public void setMax(int max) {
        mDeterminate.setMax(max);
    }

    public void setProgress(int progress) {
        mDeterminate.setProgress(progress);
    }
}
