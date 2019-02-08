package euphoria.psycho.knife;

import android.content.SharedPreferences;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import euphoria.psycho.common.C;
import euphoria.psycho.common.ContextUtils;
import euphoria.psycho.common.StorageUtils;
import euphoria.psycho.common.ThreadUtils;
import euphoria.psycho.common.base.BaseActivity;
import euphoria.psycho.common.base.Job;
import euphoria.psycho.common.base.Job.Listener;
import euphoria.psycho.common.widget.selection.SelectableListLayout;
import euphoria.psycho.common.widget.selection.SelectableListToolbar;
import euphoria.psycho.common.widget.selection.SelectionDelegate;
import euphoria.psycho.knife.bottomsheet.BottomSheet;
import euphoria.psycho.knife.bottomsheet.BottomSheet.OnClickListener;
import euphoria.psycho.knife.video.VideoFragment;

import static euphoria.psycho.knife.DocumentUtils.getDocumentInfos;

public class DirectoryFragment extends Fragment implements SelectionDelegate.SelectionObserver<DocumentInfo>,
        DocumentActionDelegate,
        Toolbar.OnMenuItemClickListener,
        SelectableListToolbar.SearchDelegate {
    private DocumentsAdapter mAdapter;
    private SelectableListLayout mContainer;
    private File mDirectory;
    private RecyclerView mRecyclerView;
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

    private void deleteFiles(DocumentInfo... documentInfos) {
        List<String> source = new ArrayList<>();
        for (DocumentInfo documentInfo : documentInfos) {
            source.add(documentInfo.getPath());
        }

        new Thread(new DeleteFileJob(getContext(), new Listener() {
            @Override
            public void onFinished(Job job) {
                ThreadUtils.postOnUiThread(() -> {
                    clearSelections();
                    updateRecyclerView();
                });
            }

            @Override
            public void onStart(Job job) {

            }
        }, source, new Handler(Looper.getMainLooper()))).start();

//        Intent intent = new Intent(getContext(), FileOperationService.class);
//
//        intent.putExtra(FileOperationService.EXTRA_JOB_ID, Long.toString(Utils.crc64Long(documentInfos[0].getPath())));
//        intent.putExtra(FileOperationService.EXTRA_OPERATION,
//                new FileOperation.Builder()
//                        .withDestination(FileUtils.getDirectoryName(documentInfos[0].getPath()))
//                        .withOpType(FileOperationService.OPERATION_DELETE)
//                        .withSource(source)
//                        .build());
//        getActivity().startService(intent);
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
            updateRecyclerView();
            return true;
        }

        return false;

    }

    private void showBottomSheet() {
        BottomSheet.instance(getActivity()).showDialog();
    }

    private void updateRecyclerView() {
        ThreadUtils.postOnBackgroundThread(() -> {
            List<DocumentInfo> infos = getDocumentInfos(mDirectory, mSortBy);
            ThreadUtils.postOnUiThread(() -> {
                mAdapter.switchDataSet(infos);
                mToolbar.setTitle(mDirectory.getName());
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
        deleteFiles(documentInfo);

//        new MaterialAlertDialogBuilder(getContext())
//                .setTitle(R.string.dialog_delete_title)
//                .setMessage(getString(R.string.dialog_delete_message, documentInfo.getFileName()))
//                .setPositiveButton(android.R.string.ok, ((dialog, which) -> {
//                    deleteFiles(documentInfo);
//                }))
//                .show();
    }

    @Override
    public void getProperties(DocumentInfo documentInfo) {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((BaseActivity) Objects.requireNonNull(getActivity())).setOnBackPressedListener(this::onBackPressed);

    }

    @Override
    public void onClicked(DocumentInfo documentInfo) {
        switch (documentInfo.getType()) {
            case C.TYPE_VIDEO:
                VideoFragment.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), documentInfo.getPath());
                break;
            case C.TYPE_DIRECTORY:
                mDirectory = new File(documentInfo.getPath());
                updateRecyclerView();
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
        mToolbar.hideOverflowMenu();
        switch (item.getItemId()) {
            case R.id.close_menu_id:
                //getActivity().finish();
                showBottomSheet();
                return true;
            case R.id.search_menu_id:
                mToolbar.showSearchView();
                mContainer.onStartSearch();
                return true;
            case R.id.selection_mode_delete_menu_id:

                List<DocumentInfo> documentInfos = mSelectionDelegate.getSelectedItemsAsList();
                deleteFiles(documentInfos.toArray(new DocumentInfo[0]));

                break;
        }
        return false;
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
        mToolbar = (DirectoryToolbar) mContainer.initializeToolbar(
                R.layout.directory_toolbar, mSelectionDelegate,
                R.string.app_name, null,
                R.id.normal_menu_group, R.id.selection_mode_menu_group,
                this,
                true,
                true);
        mToolbar.getMenu().setGroupVisible(R.id.normal_menu_group, true);
        mToolbar.initializeSearchView(this, R.string.directory_search, R.id.search_menu_id);

        BottomSheet.instance(getContext()).setOnClickListener(new OnClickListener() {
            @Override
            public void onClicked(Pair<Integer, String> item) {
                switch (item.first) {
                    case R.drawable.ic_root_internal:
                        mDirectory = Environment.getExternalStorageDirectory();
                        break;
                    case R.drawable.ic_root_sdcard:
                        mDirectory = new File(StorageUtils.getSDCardPath());
                        break;
                    case R.drawable.ic_action_file_download:
                        mDirectory = new File(Environment.getExternalStorageDirectory(), "Download");
                        break;
                }
                updateRecyclerView();
            }
        });
        loadPreferences();
        updateRecyclerView();
    }

    @Override
    public void share(DocumentInfo documentInfo) {

    }

    @Override
    public void trimVideo(DocumentInfo documentInfo) {

    }

    @Override
    public void updateItem(DocumentInfo documentInfo) {

    }
}
