package euphoria.psycho.knife.download;

import android.app.Activity;
import android.text.Selection;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import euphoria.psycho.common.Log;
import euphoria.psycho.common.base.BaseFragment;
import euphoria.psycho.common.widget.selection.SelectableListLayout;
import euphoria.psycho.common.widget.selection.SelectionDelegate;
import euphoria.psycho.common.widget.selection.SelectionDelegate.SelectionObserver;
import euphoria.psycho.knife.R;

public class DownloadFragment extends BaseFragment implements OnMenuItemClickListener, SelectionObserver<DownloadInfo> {

    RecyclerView mRecyclerView;
    SelectableListLayout mContainer;
    DownloadAdapter mAdapter;
    DownloadToolbar mToolbar;
    SelectionDelegate<DownloadInfo> mSelectionDelegate;

    private DownloadManager mDownloadManager;

    public DownloadManager getDownloadManager() {
        return mDownloadManager;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }


    @Override
    public void onSelectionStateChange(List<DownloadInfo> selectedItems) {

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);


        Log.e("TAG/", "onCreateOptionsMenu: ");

    }

    private TextView mEmptyView;

    @Override
    protected void initViews() {
        mSelectionDelegate = new SelectionDelegate<>();
        mSelectionDelegate.addObserver(this);
        mAdapter = new DownloadAdapter(mSelectionDelegate, this);

        mRecyclerView = mContainer.initializeRecyclerView(mAdapter);


        mToolbar = (DownloadToolbar) mContainer.initializeToolbar(
                R.layout.download_toolbar,
                mSelectionDelegate,
                0,
                null,
                R.id.normal_menu_group,
                R.id.selection_mode_menu_group,
                this, true, false
        );
        Activity activity = getActivity();
        mEmptyView = mContainer.initializeEmptyView(
                VectorDrawableCompat.create(
                        activity.getResources(), R.drawable.downloads_big, activity.getTheme()),
                R.string.download_manager_ui_empty, R.string.download_manager_ui_empty);

        mAdapter.switchDatas(DownloadDatabase.instance().queryPendingTask());

    }

    @Override
    protected void bindViews(View view) {

        mContainer = view.findViewById(R.id.container);


    }

    @Override
    protected int provideMenuId() {

        Log.e("TAG/", "provideMenuId: ");

        return 0;
    }


    @Override
    protected int provideLayoutId() {
        return R.layout.fragment_download;
    }
}
