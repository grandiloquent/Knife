package euphoria.psycho.knife.delegate;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.widget.Toolbar;
import euphoria.common.Contexts;
import euphoria.common.Files;
import euphoria.common.Strings;
import euphoria.psycho.common.C;
import euphoria.psycho.common.Log;
import euphoria.psycho.helpers.FileHelpers;
import euphoria.psycho.knife.DirectoryFragment;
import euphoria.psycho.knife.DocumentInfo;
import euphoria.psycho.knife.DocumentUtils;
import euphoria.psycho.knife.OperationManager;
import euphoria.psycho.knife.R;
import euphoria.psycho.knife.util.StorageUtils;
import euphoria.common.Threads;
import euphoria.psycho.notes.common.Utils;
import euphoria.psycho.share.util.ThreadUtils;

public class MenuDelegate implements Toolbar.OnMenuItemClickListener {
    private final DirectoryFragment mFragment;

    public MenuDelegate(DirectoryFragment fragment) {
        mFragment = fragment;

    }

    private static final String TAG = "TAG/" + MenuDelegate.class.getSimpleName();

    private void actionDeleteBy(File directory, Context context) {
        Path start = Paths.get(directory.getAbsolutePath());

        Stream<Path> stream = null;
        try {
            stream = java.nio.file.Files.walk(start, Integer.MAX_VALUE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (stream == null) return;
        List<String> collect = stream
                .filter(path -> path.toFile().isFile())
                .map(p -> p.toAbsolutePath().toString())
                .sorted(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return o1.length() - o2.length();
                    }
                })
                .collect(Collectors.toList());
        List<String> keepList = new ArrayList<>();
        List<String> deleteList = new ArrayList<>();

        for (String p : collect) {
            String fileName = Strings.substringAfterLast(p, '/');
//            boolean found = false;
//            for (String k : keepList) {
//                if (fileName.equals(Strings.substringAfterLast(k, '/'))) {
//                    deleteList.add(p);
//                    found = true;
//                    break;
//                }
//            }
            // if (!found) keepList.add(p);
            if (keepList.stream().anyMatch(x -> Strings.substringAfterLast(x, "/").equals(fileName))) {
                deleteList.add(p);
            } else {
                keepList.add(p);
            }

        }
        deleteList.forEach(p -> {
            new File(p).delete();

            Log.e(TAG, "Debug: actionDeleteBy, " + p);

        });


//        List<String> files = Files.getFilesRecursively(directory);
//        List<String> findFiles = new ArrayList<>();
//
//        Collections.sort(files, (o1, o2) ->
//
//                Integer.compare(o1.length(), o2.length())
//        );
//        while (files.size() > 0) {
//            String path = files.remove(0);
//            String fileName = Files.getFileName(path);
//            for (String f : files) {
//                if (Files.getFileName(f).equals(fileName)) {
//                    findFiles.add(f);
//                }
//            }
//
//        }
//        ContentResolver contentResolver = context.getContentResolver();
//        String treeUri = StorageUtils.getTreeUri(context);
//        for (String f : findFiles) {
//            if (!new File(f).delete()) {
//                StorageUtils.deleteFile(contentResolver, new File(f), treeUri);
//            }
//        }
//
//        if (VERSION.SDK_INT >= VERSION_CODES.N) {
//            String text = Files.countFileNames(directory);
//            Contexts.setText(text);
//        }

//        File pattern = new File(Environment.getExternalStorageDirectory(), "目录.txt");
//        if (pattern.isFile()) {
//            List<String> patterns = euphoria.psycho.knife.util.FileUtils.readAllLines(pattern);
//            File[] files = directory.listFiles(new FileFilter() {
//                @Override
//                public boolean accept(File pathname) {
//                    if (pathname.isFile() && patterns.indexOf(StringUtils.substringBeforeLast(pathname.getName(), ".")) != -1)
//                        return true;
//                    return false;
//                }
//            });
//            ContentResolver contentResolver = context.getContentResolver();
//            String treeUri = StorageUtils.getTreeUri(context);
//
//            for (File f :
//                    files) {
//                if (!f.delete()) {
//                    StorageUtils.deleteFile(contentResolver, f, treeUri);
//                }
//
//            }
//        }
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

    private void moveFiles(Context context, File directory) {


        new AlertDialog.Builder(context)
                .setMessage("根据文件扩展名移动文件")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {

                    DocumentUtils.moveFilesByExtension(directory.getAbsolutePath(),
                            "Extensions");
                    mFragment.updateRecyclerView(false);
                }).show();

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

    private void showBottomSheet() {
        mFragment.getBottomSheet().showDialog();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.close_menu_id:
                showBottomSheet();
                return true;

            case R.id.selection_mode_delete_menu_id:

                List<DocumentInfo> documentInfos = mFragment.getSelectionDelegate().getSelectedItemsAsList();
                DocumentUtils.buildDeleteDialog(mFragment.getActivity(), aBoolean -> {
                    mFragment.clearSelections();
                    mFragment.updateRecyclerView(false);
                }, documentInfos.toArray(new DocumentInfo[0]));

                break;
            case R.id.selection_mode_copy_menu_id:

                OperationManager.instance().setSource(mFragment.getSelectionDelegate().getSelectedItemsAsList(), true);
                mFragment.getSelectionDelegate().clearSelection();

                break;
            case R.id.search_menu_id:

                mFragment.getToolbar().showSearchView();
                mFragment.getSelectableListLayout().onStartSearch();
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
            case R.id.action_delete_by:
                actionDeleteBy(mFragment.getDirectory(), mFragment.getContext());
                break;
            case R.id.action_refresh:
                mFragment.updateRecyclerView(false);
                break;
            case R.id.action_move_images:
                try {
                    if (StorageUtils.isSDCardFile(mFragment.getDirectory())) {
                        FileHelpers.moveFilesByKeywords(mFragment.getDirectory().getAbsolutePath());
                        mFragment.updateRecyclerView(false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                Utils.moveFiles(mFragment.getContext(), mFragment.getDirectory());
//                mFragment.updateRecyclerView(false);
                break;
            case R.id.action_calculate_directory:
                calculateDirectories(mFragment.getContext(), mFragment.getDirectory());
                break;
            case R.id.action_rename_by_regex:

                renameByRegex(mFragment.getContext(), mFragment.getDirectory());
                break;
            case R.id.action_move_files:
                moveFiles(mFragment.getContext(), mFragment.getDirectory());
                break;

        }
        return true;
    }

}
