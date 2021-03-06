package euphoria.psycho.knife;

import euphoria.psycho.common.base.BaseActionDelegate;
import euphoria.psycho.knife.delegate.ListMenuDelegate;
import euphoria.psycho.knife.util.ThumbnailUtils;

public interface DocumentActionDelegate extends BaseActionDelegate<DocumentInfo> {


    void copyContent(DocumentInfo documentInfo);

    void copyFileName(DocumentInfo documentInfo);

    void delete(DocumentInfo documentInfo);


    ListMenuDelegate getListMenuDelegate();

    void getProperties(DocumentInfo documentInfo);

    ThumbnailUtils.ThumbnailProvider getThumbnailProvider();


    void onClicked(DocumentInfo documentInfo);

    void rename(DocumentInfo documentInfo);

    void formatFileName(DocumentInfo documentInfo);

    void trimVideo(DocumentInfo documentInfo);

    void addToArchive(DocumentInfo documentInfo);

    void unzip(DocumentInfo documentInfo);


}
