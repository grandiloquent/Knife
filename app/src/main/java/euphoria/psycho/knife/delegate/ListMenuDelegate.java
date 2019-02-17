package euphoria.psycho.knife;

import android.content.Context;

public class ListMenuDelegate {

    private final DirectoryFragment mFragment;

    public ListMenuDelegate(DirectoryFragment fragment) {
        mFragment = fragment;
    }

    public void shareDocumentInfo(DocumentInfo documentInfo) {
        DocumentUtils.shareDocument(mFragment.getContext(), documentInfo);
    }

    public void addToBookmark(DocumentInfo documentInfo) {

        mFragment.getBookmark().insert(documentInfo.getPath());
    }
}
