package euphoria.psycho.knife;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import euphoria.psycho.knife.bottomsheet.BottomSheet;

public class MenuDelegate implements Toolbar.OnMenuItemClickListener {
    private final DirectoryFragment mFragment;
    private BottomSheet mBottomSheet;

    public MenuDelegate(DirectoryFragment fragment) {
        mFragment = fragment;
        initializeBottomSheet();
    }

    private void initializeBottomSheet() {
        if (mBottomSheet == null) {
            mBottomSheet = new BottomSheet(mFragment.getActivity());
            mBottomSheet.setOnClickListener(mFragment::onBottomSheetClicked);
        }
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

        Log.e("TAG/MenuDelegate", "showBookmark: " + bookmarks.size());

        new AlertDialog.Builder(mFragment.getContext())
                .setTitle(R.string.bookmark)
                .setAdapter(new ArrayAdapter<>(mFragment.getContext(),
                        R.layout.dialog_bookmark,
                        R.id.line1, bookmarks), (dialog, which) -> {
                    dialog.dismiss();
                    mFragment.updateRecyclerView(bookmarks.get(which));
                })
                .setNegativeButton(android.R.string.cancel, ((dialog, which) -> dialog.dismiss()))
                .setCancelable(false)
                .show();
    }

    private void showBottomSheet() {
        mBottomSheet.showDialog();
    }

}
