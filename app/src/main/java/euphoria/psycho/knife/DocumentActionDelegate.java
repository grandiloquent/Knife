package euphoria.psycho.knife;

import euphoria.psycho.common.base.BaseActionDelegate;

public interface DocumentActionDelegate extends BaseActionDelegate<DocumentInfo> {

    void onClicked(DocumentInfo documentInfo);

    void updateItem(DocumentInfo documentInfo);

    void share(DocumentInfo documentInfo);

    void delete(DocumentInfo documentInfo);

    void trimVideo(DocumentInfo documentInfo);

    void getProperties(DocumentInfo documentInfo);
}
