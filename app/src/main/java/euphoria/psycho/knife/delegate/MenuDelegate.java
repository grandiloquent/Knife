package euphoria.psycho.knife.delegate;

import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.widget.Toolbar;
import euphoria.psycho.knife.DirectoryFragment;
import euphoria.psycho.knife.DocumentInfo;
import euphoria.psycho.knife.DocumentUtils;
import euphoria.psycho.knife.OperationManager;
import euphoria.psycho.knife.R;

public class MenuDelegate implements Toolbar.OnMenuItemClickListener {
    private final DirectoryFragment mFragment;

    public MenuDelegate(DirectoryFragment fragment) {
        mFragment = fragment;

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
        }
        return true;
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

}
