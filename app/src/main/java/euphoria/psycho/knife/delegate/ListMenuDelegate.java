package euphoria.psycho.knife.delegate;

import euphoria.psycho.knife.DirectoryFragment;
import euphoria.psycho.knife.DocumentInfo;
import euphoria.psycho.knife.DocumentUtils;

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
