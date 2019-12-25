package euphoria.psycho.knife;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import euphoria.common.Files;
import euphoria.common.Strings;
import euphoria.common.Threads;
import euphoria.psycho.common.C;
import euphoria.psycho.common.base.BaseActivity;
import euphoria.psycho.common.widget.MaterialProgressDialog;
import euphoria.psycho.common.widget.selection.SelectableListLayout;
import euphoria.psycho.common.widget.selection.SelectableListToolbar;
import euphoria.psycho.common.widget.selection.SelectionDelegate;
import euphoria.psycho.knife.helpers.FileHelper;
import euphoria.psycho.knife.bottomsheet.BottomSheet;
import euphoria.psycho.knife.delegate.BottomSheetDelegate;
import euphoria.psycho.knife.delegate.ListMenuDelegate;
import euphoria.psycho.knife.delegate.MenuDelegate;
import euphoria.psycho.knife.helpers.Helper;
import euphoria.psycho.knife.util.FileUtils;
import euphoria.psycho.knife.util.ThumbnailUtils.ThumbnailProvider;
import euphoria.psycho.knife.util.ThumbnailUtils.ThumbnailProviderImpl;
import euphoria.psycho.knife.util.VideoUtils;
import euphoria.psycho.knife.util.VideoUtils.OnTrimVideoListener;
import euphoria.psycho.knife.video.VideoActivity;
import euphoria.psycho.share.util.ContextUtils;
import euphoria.psycho.share.util.ThreadUtils;

import static android.content.Context.CLIPBOARD_SERVICE;
import static euphoria.psycho.knife.video.FileItemComparator.SORT_BY_DESCENDING;
import static euphoria.psycho.knife.video.FileItemComparator.SORT_BY_MODIFIED_TIME;
import static euphoria.psycho.knife.video.FileItemComparator.SORT_BY_SIZE;
import static euphoria.psycho.knife.video.VideoActivity.KEY_SORT_BY;
import static euphoria.psycho.knife.video.VideoActivity.KEY_SORT_DIRECTION;

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
    private boolean mSortAscending = false;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {


            String search = (String) msg.obj;
            List<DocumentInfo> infos = null;
            try {
                infos = FileHelper.listDocuments(mDirectory.getAbsolutePath(), mSortBy,
                        mSortAscending,
                        search != null ? (dir, name) -> {
                            if (name.contains(search)) return true;
                            return false;
                        } : null);
            } catch (IOException e) {
                e.printStackTrace();
            }


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

    public BottomSheet getBottomSheet() {
        return mBottomSheet;
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

    private void initializeBottomSheet() {
        if (mBottomSheet == null) {
            mBottomSheet = new BottomSheet(getActivity());
            new BottomSheetDelegate(this);
        }
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

        mSortAscending = preferences.getBoolean(C.KEY_SORT_BY_ASCENDING, false);
    }

    private boolean onBackPressed() {

        if (mSelectionDelegate.getSelectedItems().size() > 0) {
            mSelectionDelegate.clearSelection();
            return true;
        }
        if (mToolbar.isSearching()) {
            mToolbar.hideSearchView();
            updateRecyclerView(true);
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
                .putInt(C.KEY_SORT_BY, mSortBy)
                .putBoolean(C.KEY_SORT_BY_ASCENDING, mSortAscending).apply();
    }

    public void sortBy(int sortBy) {
        mToolbar.hideOverflowMenu();
        mSortBy = sortBy;
        updateRecyclerView(false);
    }


    public void sortByAscending(boolean isAscending) {
        mToolbar.hideOverflowMenu();
        mSortAscending = isAscending;
        updateRecyclerView(false);
    }


    private void updateLastVisiblePosition() {

        mLastVisiblePosition = mLayoutManager.findLastVisibleItemPosition();
    }

    public void updateRecyclerView(boolean isScrollTo) {
        ThreadUtils.postOnBackgroundThread(() -> {
            List<DocumentInfo> infos = null;
            try {
                infos = FileHelper.listDocuments(mDirectory.getAbsolutePath(), mSortBy,
                        mSortAscending,
                        null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<DocumentInfo> finalInfos = infos;
            ThreadUtils.postOnUiThread(() -> {
                mAdapter.switchDataSet(finalInfos);
                mToolbar.setTitle(mDirectory.getName());
                if (isScrollTo)
                    mLayoutManager.scrollToPosition(mLastVisiblePosition);
            });
        });
    }

    public void updateRecyclerView(File dir) {
        ThreadUtils.postOnBackgroundThread(() -> {
            mDirectory = dir;
            List<DocumentInfo> infos = null;
            try {
                infos = FileHelper.listDocuments(mDirectory.getAbsolutePath(), mSortBy,
                        mSortAscending,
                        null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<DocumentInfo> finalInfos = infos;
            ThreadUtils.postOnUiThread(() -> {
                mAdapter.switchDataSet(finalInfos);
                mToolbar.setTitle(mDirectory.getName());

            });
        });
    }

    private static long[] parseTimespan(EditText editText) {
        Pattern pattern = Pattern.compile("(\\d+:){1,}\\d+");

        Matcher matcher = pattern.matcher(editText.getText());
        String[] numbers = new String[2];
        long[] results = new long[2];

        int count = 0;
        while (matcher.find()) {

            numbers[count] = matcher.group();
            count++;
            if (count > 1) {
                break;
            }
        }
        if (count == 1) {
            results[1] = Strings.parseDuration(numbers[0]);
        } else if (count == 2) {
            results[0] = Strings.parseDuration(numbers[0]);
            results[1] = Strings.parseDuration(numbers[1]);
        } else {
            return null;
        }
        return results;
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
        transaction.commitNowAllowingStateLoss();
    }

    @Override
    public void addToArchive(DocumentInfo documentInfo) {
        ProgressDialog dialog = MaterialProgressDialog.show(getContext(),
                "", "正在压缩 " + documentInfo.getFileName());

        Threads.postOnBackgroundThread(() -> {
            if (documentInfo.getType() == C.TYPE_DIRECTORY) {
                DocumentUtils.createZipFromDirectory(documentInfo.getPath(),
                        documentInfo.getPath() + ".zip");
                Threads.postOnUiThread(() -> {
                    updateRecyclerView(false);
                    dialog.dismiss();
                });
            }
        });
    }

    @Override
    public void copyContent(DocumentInfo documentInfo) {
        try {
            File sourceFile = new File(documentInfo.getPath());
            String text = new String(euphoria.common.Files.readFully(sourceFile), "UTF-8");

            ClipboardManager manager = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
            if (manager != null) {
                manager.setPrimaryClip(ClipData.newPlainText(null, text));
            }
//            File dir = new File(sourceFile.getParentFile(), "Copied");
//            if (!dir.isDirectory()) dir.mkdir();
//            sourceFile.renameTo(new File(dir, sourceFile.getName()));
//            updateRecyclerView(false);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void copyFileName(DocumentInfo documentInfo) {
        Helper.setClipboardText(getContext(), documentInfo.getFileName());


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
            mThumbnailProvider = new ThumbnailProviderImpl();
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
                Intent videoIntent = new Intent(this.getContext(), VideoActivity.class);
                videoIntent.setData(Uri.fromFile(new File(documentInfo.getPath())));
                if (mSortBy == C.SORT_BY_SIZE)
                    videoIntent.putExtra(KEY_SORT_BY, SORT_BY_SIZE);
                else if (mSortBy == C.SORT_BY_DATE_MODIFIED) {
                    videoIntent.putExtra(KEY_SORT_BY, SORT_BY_MODIFIED_TIME);
                }
                videoIntent.putExtra(KEY_SORT_DIRECTION, SORT_BY_DESCENDING);

                startActivity(videoIntent);
                break;
            case C.TYPE_DIRECTORY:
                updateLastVisiblePosition();
                mDirectory = new File(documentInfo.getPath());
                updateRecyclerView(false);
                break;
            case C.TYPE_AUDIO:
                Intent musicService = new Intent(Intent.ACTION_VIEW);
                musicService.setDataAndType(Uri.fromFile(new File(documentInfo.getPath())), "audio/*");


                Objects.requireNonNull(this.getActivity()).startActivity(Intent.createChooser(musicService, "音乐"));

//                Intent musicService = new Intent(this.getActivity(), MusicService.class);
//                musicService.setAction(MusicService.ACTION_PLAY);
//                musicService.putExtra(MusicService.EXTRA_PATH, documentInfo.getPath());
//
//                this.getActivity().startService(musicService);
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.fragment_directory, container, false);
    }

    @Override
    public void onEndSearch() {
        updateRecyclerView(false);
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

    @Override
    public void rename(DocumentInfo documentInfo) {
        DocumentUtils.buildRenameDialog(getContext(),
                documentInfo.getFileName(),
                charSequence -> {
                    if (charSequence == null) return;
                    String newFileName = charSequence.toString();
                    File src = new File(documentInfo.getPath());
                    File target = new File(src.getParentFile(), newFileName);

                    if (!target.exists()) {
                        src.renameTo(target);
                        updateRecyclerView(false);
                    }
                });
    }

    @Override
    public void trimVideo(DocumentInfo documentInfo) {

        final EditText editText = new EditText(getContext());
        new Builder(this.getContext())
                .setView(editText)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
//                        VideoClip videoClip = new VideoClip();
//                        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2) {
//                            videoClip.clipVideo(documentInfo.getPath(),
//                                    new File(Environment.getExternalStorageDirectory(), documentInfo.getPath()).getAbsolutePath(),
//                                    0, 0);
//                        }

                    long[] numbers = parseTimespan(editText);
                    if (numbers != null) {

                        try {
                            StringBuilder formatBuilder = new StringBuilder();
                            Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());

                            File sourceFile = new File(documentInfo.getPath());

                            String fileName = Files.getFileNameWithoutExtension(documentInfo.getFileName());
                            String ext = Files.getExtension(documentInfo.getFileName());
                            if (ext.length() > 0) ext = ext.substring(1);

                            final File destinationFile = FileUtils.buildUniqueFileWithExtension(
                                    sourceFile.getParentFile(),
                                    String.format("%s_%s_%s", fileName,
                                            Util.getStringForTime(formatBuilder, formatter, numbers[0]).replaceAll(":", "-"),
                                            Util.getStringForTime(formatBuilder, formatter, numbers[1]).replaceAll(":", "-")
                                    ),
                                    ext
                            );

                            VideoUtils.startTrim(sourceFile,
                                    destinationFile,
                                    numbers[0], numbers[1], new OnTrimVideoListener() {
                                        @Override
                                        public void cancelAction() {

                                        }

                                        @Override
                                        public void getResult(Uri uri) {
                                            euphoria.common.Contexts.triggerMediaScanner(getContext(), destinationFile);
                                            updateRecyclerView(false);
                                        }

                                        @Override
                                        public void onError(String message) {

                                        }

                                        @Override
                                        public void onTrimStarted() {

                                        }
                                    });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getContext(), "0:0", Toast.LENGTH_LONG).show();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    public void unzip(DocumentInfo documentInfo) {
        if (Pattern.compile("\\.(?:zip|epub)$").matcher(documentInfo.getFileName()).find()) {
            File dir = new File(
                    Strings.substringBeforeLast(documentInfo.getPath(),
                            '.'));
            if (!dir.isDirectory()) dir.mkdirs();
            DocumentUtils.extractToDirectory(documentInfo.getPath(),
                    dir.getAbsolutePath()
            );
            new File(documentInfo.getPath()).delete();
            updateRecyclerView(false);
            return;
        }
        ThreadUtils.postOnBackgroundThread(() -> {
            UnZipJob job = new UnZipJob(Throwable::printStackTrace);
            job.unzip(documentInfo.getPath());
            ThreadUtils.postOnUiThread(() -> updateRecyclerView(true));
        });
    }
}
