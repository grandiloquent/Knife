package euphoria.psycho.knife;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import euphoria.psycho.common.C;
import euphoria.psycho.common.FileUtils;
import euphoria.psycho.common.Log;
import euphoria.psycho.common.base.BaseActivity;
import euphoria.psycho.common.widget.selection.SelectableListLayout;
import euphoria.psycho.common.widget.selection.SelectableListToolbar;
import euphoria.psycho.common.widget.selection.SelectionDelegate;
import euphoria.psycho.knife.UnZipJob.UnZipListener;
import euphoria.psycho.knife.bottomsheet.BottomSheet;
import euphoria.psycho.knife.cache.ThumbnailProvider;
import euphoria.psycho.knife.cache.ThumbnailProviderImpl;
import euphoria.psycho.knife.delegate.BottomSheetDelegate;
import euphoria.psycho.knife.delegate.ListMenuDelegate;
import euphoria.psycho.knife.delegate.MenuDelegate;
import euphoria.psycho.knife.video.VideoFragment;
import euphoria.psycho.share.util.ContextUtils;
import euphoria.psycho.share.util.ThreadUtils;

import static euphoria.psycho.knife.DocumentUtils.getDocumentInfos;

public class DirectoryFragment extends Fragment implements SelectionDelegate.SelectionObserver<DocumentInfo>,
        DocumentActionDelegate,

        SelectableListToolbar.SearchDelegate {
    private static final int MSG_SEARCH = 0;
    private DocumentsAdapter mAdapter;
    private SelectableListLayout mContainer;
    private File mDirectory;
    private int mLastVisiblePosition;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private BottomSheet mBottomSheet;

    public BottomSheet getBottomSheet() {
        return mBottomSheet;
    }

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
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {


            String search = (String) msg.obj;
            List<DocumentInfo> infos = getDocumentInfos(mDirectory, mSortBy, search != null ? (dir, name) -> {
                if (name.contains(search)) return true;
                return false;
            } : null);


            mAdapter.switchDataSet(infos);

        }
    };
    private ListMenuDelegate mListMenuDelegate;
    private DirectoryToolbar mToolbar;
    ThumbnailProvider mThumbnailProvider;
    private Bookmark mBookmark;

    public boolean clearSelections() {
        if (mSelectionDelegate.getSelectedItems().size() > 0) {
            mSelectionDelegate.clearSelection();
            return true;
        }
        return false;
    }

    public Bookmark getBookmark() {
        if (mBookmark == null) {
            mBookmark = new Bookmark(getContext());
        }
        return mBookmark;
    }

    public File getDirectory() {
        return mDirectory;
    }

    public DocumentsAdapter getDocumentsAdapter() {
        return mAdapter;
    }

    public SelectableListLayout getSelectableListLayout() {
        return mContainer;
    }

    public SelectionDelegate getSelectionDelegate() {
        return mSelectionDelegate;
    }

    public DirectoryToolbar getToolbar() {
        return mToolbar;
    }

    private void initializeToolbar() {
        MenuDelegate menuDelegate = new MenuDelegate(this);

        mToolbar = (DirectoryToolbar) mContainer.initializeToolbar(
                R.layout.directory_toolbar, mSelectionDelegate,
                R.string.app_name, null,
                R.id.normal_menu_group, R.id.selection_mode_menu_group,
                menuDelegate,
                true,
                true);
        mToolbar.getMenu().setGroupVisible(R.id.normal_menu_group, true);
        mToolbar.initializeSearchView(this, R.string.directory_search, R.id.search_menu_id);

        mToolbar.getSearchEditText().setOnKeyListener(this::onSearch);
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
        if (mToolbar.isSearching()) {
            mToolbar.hideSearchView();
        }
        File parent = mDirectory.getParentFile();
        if (parent != null) {
            mDirectory = parent;
            updateRecyclerView(true);
            return true;
        }

        return false;

    }



    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {


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


        }
        return true;
    }

    public boolean onSearch(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
            ThreadUtils.postOnBackgroundThread(() -> {
                String value = mToolbar.getSearchEditText().getText().toString();
                Pattern pattern = Pattern.compile(value);
                Collection<File> collection = euphoria.psycho.share.util.FileUtils.getFiles(mDirectory, pathname -> {
                    if (pathname.isDirectory() || pattern.matcher(pathname.getName()).find()) {
                        return true;
                    }
                    return false;
                }, true);
                List<DocumentInfo> documentInfos = new ArrayList<>();
                File[] files = collection.toArray(new File[0]);
                euphoria.psycho.share.util.FileUtils.sortByName(files, true);
                for (File file : files) {

                    documentInfos.add(DocumentUtils.buildDocumentInfo(file));


                }
                ThreadUtils.postOnUiThread(() -> {
                    mAdapter.switchDataSet(documentInfos);
                });
            });

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

    private void sortBy(int sortBy) {
        mToolbar.hideOverflowMenu();
        mSortBy = sortBy;
        updateRecyclerView(false);
    }

    private void updateLastVisiblePosition() {

        mLastVisiblePosition = mLayoutManager.findLastVisibleItemPosition();
    }

    public void updateRecyclerView(boolean isScrollTo) {
        ThreadUtils.postOnBackgroundThread(() -> {
            List<DocumentInfo> infos = getDocumentInfos(mDirectory, mSortBy, null);
            ThreadUtils.postOnUiThread(() -> {
                mAdapter.switchDataSet(infos);
                mToolbar.setTitle(mDirectory.getName());
                if (isScrollTo)
                    mLayoutManager.scrollToPosition(mLastVisiblePosition);
            });
        });
    }

    public void updateRecyclerView(File dir) {
        ThreadUtils.postOnBackgroundThread(() -> {
            mDirectory = dir;
            List<DocumentInfo> infos = getDocumentInfos(mDirectory, mSortBy, null);
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

        DocumentUtils.buildDeleteDialog(getContext(), aBoolean -> {
            if (aBoolean) mAdapter.removeItem(documentInfo);
        }, documentInfo);

    }

    @Override
    public ListMenuDelegate getListMenuDelegate() {

        if (mListMenuDelegate == null) {
            mListMenuDelegate = new ListMenuDelegate(this);
        }
        return mListMenuDelegate;
    }

    @Override
    public void getProperties(DocumentInfo documentInfo) {
        DocumentUtils.showDocumentProperties(getContext(), documentInfo);
    }

    @Override
    public ThumbnailProvider getThumbnailProvider() {
        if (mThumbnailProvider == null) {
            mThumbnailProvider = new ThumbnailProviderImpl(((App) ContextUtils.getApplicationContext()).getReferencePool());
        }
        return mThumbnailProvider;
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
                VideoFragment.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), documentInfo.getPath(), mSortBy, 0);
                break;
            case C.TYPE_DIRECTORY:
                updateLastVisiblePosition();
                mDirectory = new File(documentInfo.getPath());
                updateRecyclerView(false);
                break;
            default:
                DocumentUtils.openContent(getActivity(), documentInfo, 0);
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.fromFile(new File(documentInfo.getPath())),
//                        NetUtils.getMimeType(documentInfo.getFileName()));
//
//                if (documentInfo.getFileName().toLowerCase().endsWith(".apk")) {
//                    startActivity(Intent.createChooser(intent, "打开"));
//                    return;
//                }
//                ComponentName foundActivity = intent.resolveActivity(getContext().getPackageManager());
//                if (foundActivity != null) {
//                    startActivity(intent);
//                } else {
//                    startActivity(Intent.createChooser(intent, "打开"));
//                }
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
    public void onPause() {
        super.onPause();
        savePreferences();
    }

    @Override
    public void onSearchTextChanged(String query) {

        mHandler.removeMessages(MSG_SEARCH);
        mHandler.obtainMessage(MSG_SEARCH, query).sendToTarget();
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
                        getActivity().getResources(),
                        R.drawable.downloads_big,
                        getActivity().getTheme()),
                R.string.directory_ui_empty, R.string.directory_no_results);

        mSelectionDelegate = new SelectionDelegate();
        mSelectionDelegate.addObserver(this);
        mAdapter = new DocumentsAdapter(this, mSelectionDelegate);
        mRecyclerView = mContainer.initializeRecyclerView(mAdapter);
        mRecyclerView.getItemAnimator().setChangeDuration(0);
        mLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();

        //

        initializeToolbar();
        initializeBottomSheet();

        loadPreferences();
        updateRecyclerView(true);

        OperationManager.instance().initialize(this, needRefreshView -> {
            updateRecyclerView(false);
            OperationManager.instance().hideActionButtons();
        });
    }

    private void initializeBottomSheet() {
        if (mBottomSheet == null) {
            mBottomSheet = new BottomSheet(getActivity());
            new BottomSheetDelegate(this);
        }
    }

    @Override
    public void rename(DocumentInfo documentInfo) {
        DocumentUtils.buildRenameDialog(getContext(),
                documentInfo.getFileName(),
                charSequence -> {
                    if (charSequence == null) return;
                    String newFileName = charSequence.toString();
                    File src = new File(documentInfo.getPath());
                    boolean renameResult = FileUtils.renameFile(getContext(),
                            src,
                            new File(src.getParentFile(), newFileName));
                    if (renameResult) updateRecyclerView(false);
                });
    }

    @Override
    public void trimVideo(DocumentInfo documentInfo) {

    }

    @Override
    public void unzip(DocumentInfo documentInfo) {
        ThreadUtils.postOnBackgroundThread(() -> {
            UnZipJob job = new UnZipJob(new UnZipListener() {
                @Override
                public void onError(Exception exception) {

                    exception.printStackTrace();
                    Log.e("TAG/DirectoryFragment", "onError: " + exception);

                }
            });
            job.unzip(documentInfo.getPath());
        });
    }

    @Override
    public void updateItem(DocumentInfo documentInfo) {

    }
}
