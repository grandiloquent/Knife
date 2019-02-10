package euphoria.psycho.knife;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import euphoria.psycho.common.BitmapUtils;
import euphoria.psycho.common.C;
import euphoria.psycho.common.ContextUtils;
import euphoria.psycho.common.IconUtils;
import euphoria.psycho.common.Log;
import euphoria.psycho.common.NetUtils;
import euphoria.psycho.common.StorageUtils;
import euphoria.psycho.common.ThreadUtils;
import euphoria.psycho.common.base.BaseActivity;
import euphoria.psycho.common.base.Job;
import euphoria.psycho.common.base.Job.Listener;
import euphoria.psycho.common.pool.BytesBufferPool;
import euphoria.psycho.common.widget.selection.SelectableListLayout;
import euphoria.psycho.common.widget.selection.SelectableListToolbar;
import euphoria.psycho.common.widget.selection.SelectionDelegate;
import euphoria.psycho.knife.DocumentUtils.Consumer;
import euphoria.psycho.knife.bottomsheet.BottomSheet;
import euphoria.psycho.knife.bottomsheet.BottomSheet.OnClickListener;
import euphoria.psycho.knife.video.VideoFragment;

import static euphoria.psycho.knife.DocumentUtils.calculateDirectory;
import static euphoria.psycho.knife.DocumentUtils.getDocumentInfos;

public class DirectoryFragment extends Fragment implements SelectionDelegate.SelectionObserver<DocumentInfo>,
        DocumentActionDelegate,
        Toolbar.OnMenuItemClickListener,
        SelectableListToolbar.SearchDelegate {
    private DocumentsAdapter mAdapter;
    private SelectableListLayout mContainer;
    private File mDirectory;
    private int mLastVisiblePosition;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    //    private OnGlobalLayoutListener mGlobalLayoutListener = new OnGlobalLayoutListener() {
//        @Override
//        public void onGlobalLayout() {
//            int scrollY = ContextUtils.getAppSharedPreferences().getInt(C.KEY_SCROLL_Y, RecyclerView.NO_POSITION);
//
//            Log.e("TAG/", "onGlobalLayout: ");
//
//            scrollToPosition(scrollY);
//            mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
//        }
//    };
    private SelectionDelegate mSelectionDelegate;
    private int mSortBy = C.SORT_BY_UNSPECIFIED;
    private DirectoryToolbar mToolbar;

    private boolean clearSelections() {
        if (mSelectionDelegate.getSelectedItems().size() > 0) {
            mSelectionDelegate.clearSelection();
            return true;
        }
        return false;
    }


    private void initializeBottomSheet() {

        BottomSheet.instance(getActivity()).setOnClickListener(new OnClickListener() {
            @Override
            public void onClicked(Pair<Integer, String> item) {
                switch (item.first) {
                    case R.drawable.ic_action_storage:
                        mDirectory = Environment.getExternalStorageDirectory();
                        break;
                    case R.drawable.ic_action_sd_card:
                        mDirectory = new File(StorageUtils.getSDCardPath());
                        break;
                    case R.drawable.ic_action_file_download:
                        mDirectory = new File(Environment.getExternalStorageDirectory(), "Download");
                        break;
                    case R.drawable.ic_action_photo:
                        mDirectory = new File(Environment.getExternalStorageDirectory(), "DCIM");
                        break;
                }
                updateRecyclerView(false);
            }
        });
    }

    private void loadPreferences() {
        SharedPreferences preferences = ContextUtils.getAppSharedPreferences();

        String directory = null;

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(C.EXTRA_DIRECTORY))
                directory = bundle.getString(C.EXTRA_DIRECTORY);
            if (bundle.containsKey(C.EXTRA_SORT_BY))
                mSortBy = bundle.getInt(C.EXTRA_SORT_BY);
        }
        if (directory == null)
            directory = preferences.getString(C.KEY_DIRECTORY, null);
        if (directory == null) {
            mDirectory = Environment.getExternalStorageDirectory();
        } else {
            mDirectory = new File(directory);
        }
        if (mSortBy == C.SORT_BY_UNSPECIFIED)
            mSortBy = preferences.getInt(C.KEY_SORT_BY, C.SORT_BY_NAME);

    }

    private boolean onBackPressed() {

        if (mSelectionDelegate.getSelectedItems().size() > 0) {
            mSelectionDelegate.clearSelection();
            return true;
        }

        File parent = mDirectory.getParentFile();
        if (parent != null) {
            mDirectory = parent;
            updateRecyclerView(true);
            return true;
        }

        return false;

    }

    private void savePreferences() {
        SharedPreferences preferences = ContextUtils.getAppSharedPreferences();
        updateLastVisiblePosition();
        preferences.edit().putInt(C.KEY_SCROLL_Y, mLastVisiblePosition)
                .putString(C.KEY_DIRECTORY, mDirectory.getAbsolutePath())
                .putInt(C.KEY_SORT_BY, mSortBy).apply();
    }

    private void scrollToPosition(int position) {
        LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if (layoutManager != null) {
            int count = layoutManager.getChildCount();
            Log.e("TAG/", "scrollToPosition: " + position + " " + count);
            //  && position < count

            if (position != RecyclerView.NO_POSITION) {


                layoutManager.scrollToPosition(position);
            }
        }
    }

    private void showBottomSheet() {
        BottomSheet.instance(getActivity()).showDialog();
    }

    private void sortBy(int sortBy) {
        mToolbar.hideOverflowMenu();
        mSortBy = sortBy;
        updateRecyclerView(false);
    }


    private void updateLastVisiblePosition() {

        mLastVisiblePosition = mLayoutManager.findLastVisibleItemPosition();
    }

    private void updateRecyclerView(boolean isScrollTo) {
        ThreadUtils.postOnBackgroundThread(() -> {
            List<DocumentInfo> infos = getDocumentInfos(mDirectory, mSortBy);
            ThreadUtils.postOnUiThread(() -> {
                mAdapter.switchDataSet(infos);
                mToolbar.setTitle(mDirectory.getName());
                if (isScrollTo)
                    mLayoutManager.scrollToPosition(mLastVisiblePosition);
            });
        });
    }

    public static void show(FragmentManager manager) {
        show(manager, null);
    }

    public static void show(FragmentManager manager, String startDirectory) {
        FragmentTransaction transaction = manager.beginTransaction();

        DirectoryFragment fragment = new DirectoryFragment();


        if (startDirectory != null) {
            Bundle bundle = new Bundle();
            bundle.putString(C.EXTRA_DIRECTORY, startDirectory);
            fragment.setArguments(bundle);
        }
        //transaction.setCustomAnimations(R.animator.dir_frozen, R.animator.dir_up);
        transaction.replace(R.id.container, fragment);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            transaction.commitNowAllowingStateLoss();
        } else {
            transaction.commit();
        }
    }

    @Override
    public void delete(DocumentInfo documentInfo) {

        DocumentUtils.buildDeleteDialog(getContext(), aBoolean -> {
            if (aBoolean) mAdapter.removeItem(documentInfo);
        }, documentInfo);

    }

    @Override
    public void getProperties(DocumentInfo documentInfo) {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((BaseActivity) Objects.requireNonNull(getActivity())).setOnBackPressedListener(this::onBackPressed);
        initializeBottomSheet();
    }

    @Override
    public void onClicked(DocumentInfo documentInfo) {
        switch (documentInfo.getType()) {
            case C.TYPE_VIDEO:
                VideoFragment.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), documentInfo.getPath());
                break;
            case C.TYPE_DIRECTORY:
                updateLastVisiblePosition();
                mDirectory = new File(documentInfo.getPath());
                updateRecyclerView(false);
                break;
            default:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(documentInfo.getPath())),
                        NetUtils.getMimeType(documentInfo.getFileName()));

                ComponentName foundActivity = intent.resolveActivity(getContext().getPackageManager());
                if (foundActivity != null) {
                    startActivity(intent);
                } else {
                    startActivity(Intent.createChooser(intent, "打开"));
                }
                break;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.fragment_directory, container, false);
    }

    @Override
    public void onEndSearch() {

    }

    @Override
    public boolean onMenuItemClick(MenuItem item)

    {
        switch (item.getItemId()) {
            case R.id.close_menu_id:
                mToolbar.hideOverflowMenu();

                showBottomSheet();
                return true;
            case R.id.search_menu_id:
                mToolbar.hideOverflowMenu();

                mToolbar.showSearchView();
                mContainer.onStartSearch();
                return true;
            case R.id.selection_mode_delete_menu_id:
                mToolbar.hideOverflowMenu();

                List<DocumentInfo> documentInfos = mSelectionDelegate.getSelectedItemsAsList();
                DocumentUtils.buildDeleteDialog(getActivity(), new DocumentUtils.Consumer<Boolean>() {

                    @Override
                    public void accept(Boolean aBoolean) {
                        clearSelections();
                        updateRecyclerView(false);
                    }
                }, documentInfos.toArray(new DocumentInfo[0]));

                break;
            case R.id.action_sort_by_date:

                sortBy(C.SORT_BY_DATE_MODIFIED);
                break;
            case R.id.action_sort_by_name:
                mToolbar.hideOverflowMenu();

                sortBy(C.SORT_BY_NAME);
                break;
            case R.id.action_sort_by_size:
                sortBy(C.SORT_BY_SIZE);
                break;
            case R.id.action_select_same_type:
                DocumentUtils.selectSameTypes(mSelectionDelegate, mAdapter);
                break;
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        savePreferences();
    }

    @Override
    public void onSearchTextChanged(String query) {

    }

    @Override
    public void onSelectionStateChange(List<DocumentInfo> selectedItems) {

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mContainer = view.findViewById(R.id.container);


        mContainer = view.findViewById(R.id.container);
        mContainer.initializeEmptyView(
                VectorDrawableCompat.create(
                        getActivity().getResources(), R.drawable.downloads_big, getActivity().getTheme()),
                R.string.download_manager_ui_empty, R.string.directory_no_results);

        mSelectionDelegate = new SelectionDelegate();
        mSelectionDelegate.addObserver(this);
        mAdapter = new DocumentsAdapter(this, mSelectionDelegate);
        mRecyclerView = mContainer.initializeRecyclerView(mAdapter);
        mRecyclerView.getItemAnimator().setChangeDuration(0);
        mLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        mToolbar = (DirectoryToolbar) mContainer.initializeToolbar(
                R.layout.directory_toolbar, mSelectionDelegate,
                R.string.app_name, null,
                R.id.normal_menu_group, R.id.selection_mode_menu_group,
                this,
                true,
                true);
        mToolbar.getMenu().setGroupVisible(R.id.normal_menu_group, true);
        mToolbar.initializeSearchView(this, R.string.directory_search, R.id.search_menu_id);

        loadPreferences();
        updateRecyclerView(true);

    }

    @Override
    public void rename(DocumentInfo documentInfo) {
        DocumentUtils.buildRenameDialog(getContext(),
                documentInfo.getFileName(),
                charSequence -> {
                    if (charSequence == null) return;
                    String newFileName = charSequence.toString();
                    File src = new File(documentInfo.getPath());
                    boolean renameResult = StorageUtils.renameFile(getContext(),
                            src,
                            new File(src.getParentFile(), newFileName));
                    if (renameResult) updateRecyclerView(false);
                });
    }

    @Override
    public void share(DocumentInfo documentInfo) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setDataAndType(Uri.fromFile(new File(documentInfo.getPath())), NetUtils.getMimeType(documentInfo.getFileName()));
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_link_title)));
    }

    @Override
    public void trimVideo(DocumentInfo documentInfo) {

    }

    @Override
    public void updateItem(DocumentInfo documentInfo) {

    }
}
