package euphoria.psycho.knife.delegate;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.format.Formatter;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.widget.Toolbar;
import euphoria.common.Strings;
import euphoria.common.Threads;
import euphoria.psycho.common.C;
import euphoria.psycho.common.Log;
import euphoria.psycho.knife.DirectoryFragment;
import euphoria.psycho.knife.DocumentInfo;
import euphoria.psycho.knife.DocumentUtils;
import euphoria.psycho.knife.OperationManager;
import euphoria.psycho.knife.R;
import euphoria.psycho.knife.helpers.FileHelper;
import euphoria.psycho.knife.helpers.Helper;
import euphoria.psycho.share.util.ThreadUtils;

public class MenuDelegate implements Toolbar.OnMenuItemClickListener {
    private static final String TAG = "TAG/" + MenuDelegate.class.getSimpleName();
    private final DirectoryFragment mFragment;

    public MenuDelegate(DirectoryFragment fragment) {
        mFragment = fragment;

    }

    private void actionCalculateDirectory() {
        calculateDirectories(mFragment.getContext(), mFragment.getDirectory());
    }

    private void actionCopyDirectoryStructure() {
        try {
            Helper.setClipboardText(mFragment.getContext(),
                    FileHelper.copyDirectoryStructure(mFragment.getDirectory().getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void actionDeleteEmptyDirectories() {
    }

    private void actionMoveFiles() {
    }

    private void actionMoveImages() {
    }

    private void actionRefresh() {
    }

    private void actionRenameByRegex() {
    }

    private void actionSelectAll() {
    }

    private void actionSelectSameType() {
    }

    private void actionSortByAscending() {
    }

    private void actionSortByDate() {
    }

    private void actionSortByDescending() {
    }

    private void actionSortByName() {
    }

    private void actionSortBySize() {
    }

    private void bookmarkMenuId() {
    }

    private void calculateDirectories(Context context, File directory) {
        final File[] dirList = directory.listFiles(pathname -> {
            if (pathname.isDirectory()) return true;
            return false;
        });
        final Collator collator = Collator.getInstance(Locale.CHINA);
        Arrays.sort(dirList, (o1, o2) -> collator.compare(o1.getName(), o2.getName()));
        final List<Pair<String, Long>> fileItems = new ArrayList<>();

        ThreadUtils.postOnBackgroundThread(() -> {
            final StringBuilder sb = new StringBuilder();


            int count = 0;
            for (File dir : dirList) {
                count++;
                long size = DocumentUtils.calculateDirectory(dir.getAbsolutePath());

                Pair<String, Long> fileItem = Pair.create(dir.getName(), size);

                fileItems.add(fileItem);
            }
            Collections.sort(fileItems, (o1, o2) -> o1.second.compareTo(o2.second) * -1);
            sb.setLength(0);
            sb.append("总共计算手机内部储存中 ").append(count).append(" 个目录").append('\n');
            sb.append("从大到小依次排列: \n\n");
            sb.append("目录名").append(" | ").append("大小").append("\n\n");
            for (Pair<String, Long> f : fileItems) {
                sb.append(f.first).append(" | ").append(Formatter.formatFileSize(context, f.second)).append("\n");
            }

            Threads.postOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, sb.toString()));
                    Toast.makeText(context, sb.toString(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void closeMenuId() {
        mFragment.getBottomSheet().showDialog();
    }

    private void deleteEmptyDirectories() {
        try {
            Files.list(Paths.get(mFragment.getDirectory().getPath())).parallel().filter(p -> {
                try {
                    return Files.isDirectory(p)
                            && FileHelper.isEmptyDirectory(p);
                } catch (Exception ignored) {
                    return false;
                }
            }).forEach(new Consumer<Path>() {
                @Override
                public void accept(Path path) {
                    try {
                        Files.delete(path);
                    } catch (Exception ignored) {
                    }
                }
            });
            mFragment.updateRecyclerView(false);

        } catch (Exception ignored) {

        }

    }

    private void moveFiles(Context context, File directory) {


        new AlertDialog.Builder(context)
                .setMessage("根据文件扩展名移动文件")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {

                    DocumentUtils.moveFilesByExtension(directory.getAbsolutePath(),
                            "Extensions");
                    mFragment.updateRecyclerView(false);
                }).show();

    }

    private void normalMenuGroup() {
    }

    private void renameByRegex(Context context, File directory) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_rename_by_regex, null);
        EditText find = view.findViewById(R.id.find);
        EditText replace = view.findViewById(R.id.replace);

        new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String f = find.getText().toString();
                    if (Strings.isNullOrWhiteSpace(f)) return;
                    String r = replace.getText().toString();
                    File[] files = directory.listFiles();
                    for (File file : files) {
                        File dst = new File(directory, file.getName().replaceAll(f, r));
                        file.renameTo(dst);
                    }
                    mFragment.updateRecyclerView(false);
                }).show();

    }

    private void searchMenuId() {
        mFragment.getToolbar().showSearchView();
        mFragment.getSelectableListLayout().onStartSearch();
    }

    private void selectionModeCopyMenuId() {
    }

    private void selectionModeCutMenuId() {
    }

    private void selectionModeDeleteMenuId() {
        List<DocumentInfo> documentInfos = mFragment.getSelectionDelegate().getSelectedItemsAsList();
        DocumentUtils.buildDeleteDialog(mFragment.getActivity(), aBoolean -> {
            mFragment.clearSelections();
            mFragment.updateRecyclerView(false);
        }, documentInfos.toArray(new DocumentInfo[0]));

    }

    private void selectionModeMenuGroup() {
    }

    private void showBookmark() {
        List<String> bookmarks = mFragment.getBookmark().fetchBookmarks();


        new Builder(mFragment.getContext())
                .setTitle(R.string.bookmark)
                .setAdapter(new ArrayAdapter<>(mFragment.getContext(),
                                R.layout.dialog_bookmark,
                                R.id.line1, bookmarks), (dialog, which) -> {
                            dialog.dismiss();
                            File dir = new File(bookmarks.get(which));
                            if (dir.isDirectory()) {
                                mFragment.updateRecyclerView(dir);
                            } else {
                                mFragment.getBookmark().delete(dir.getAbsolutePath());
                                Toast.makeText(mFragment.getContext(), mFragment.getText(R.string.bookmark_no_exists), Toast.LENGTH_SHORT).show();
                            }
                        }
                )
                .setNegativeButton(android.R.string.cancel, ((dialog, which) -> dialog.dismiss()))
                .setCancelable(false)
                .show();
    }


    private void sortMenuId() {
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_copy_directory_structure:
                actionCopyDirectoryStructure();
                return true;
            case R.id.close_menu_id:
                closeMenuId();
                return true;

            case R.id.selection_mode_delete_menu_id:
                selectionModeDeleteMenuId();
                break;
            case R.id.selection_mode_copy_menu_id:

                OperationManager.instance().setSource(mFragment.getSelectionDelegate().getSelectedItemsAsList(), true);
                mFragment.getSelectionDelegate().clearSelection();

                break;
            case R.id.search_menu_id:
                searchMenuId();
                return true;
            case R.id.action_select_same_type:
                DocumentUtils.selectSameTypes(mFragment.getSelectionDelegate(), mFragment.getDocumentsAdapter());
                break;
            case R.id.action_select_all:
                DocumentUtils.selectAll(mFragment.getSelectionDelegate(), mFragment.getDocumentsAdapter());
                break;
            case R.id.selection_mode_cut_menu_id:

                OperationManager.instance().setSource(mFragment.getSelectionDelegate().getSelectedItemsAsList(), false);
                mFragment.getSelectionDelegate().clearSelection();

                break;
            case R.id.bookmark_menu_id:
                showBookmark();
                break;
            case R.id.action_sort_by_date:

                mFragment.sortBy(C.SORT_BY_DATE_MODIFIED);
                break;
            case R.id.action_sort_by_name:

                mFragment.sortBy(C.SORT_BY_NAME);
                break;
            case R.id.action_sort_by_size:
                mFragment.sortBy(C.SORT_BY_SIZE);
                break;
            case R.id.action_sort_by_ascending:
                mFragment.sortByAscending(true);
                break;

            case R.id.action_sort_by_descending:
                mFragment.sortByAscending(false);
                break;


            case R.id.action_calculate_directory:
                actionCalculateDirectory();
                break;
            case R.id.action_rename_by_regex:

                renameByRegex(mFragment.getContext(), mFragment.getDirectory());
                break;
            case R.id.action_delete_empty_directories:
                deleteEmptyDirectories();
                break;
            case R.id.action_move_files:
                moveFiles(mFragment.getContext(), mFragment.getDirectory());
                break;


        }
        return true;
    }

}
