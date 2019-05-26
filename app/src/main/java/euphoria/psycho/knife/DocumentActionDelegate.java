package euphoria.psycho.knife;

import euphoria.psycho.common.base.BaseActionDelegate;
import euphoria.psycho.knife.cache.ThumbnailProvider;
import euphoria.psycho.knife.delegate.ListMenuDelegate;

public interface DocumentActionDelegate extends BaseActionDelegate<DocumentInfo> {

    void onClicked(DocumentInfo documentInfo);


    void updateItem(DocumentInfo documentInfo);

    void unzip(DocumentInfo documentInfo);


    void srt2Txt(DocumentInfo documentInfo);

    void copyFileName(DocumentInfo documentInfo);

    void extractVideoSrc(DocumentInfo documentInfo);

    void delete(DocumentInfo documentInfo);

    void trimVideo(DocumentInfo documentInfo);

    void getProperties(DocumentInfo documentInfo);

    void rename(DocumentInfo documentInfo);

    ListMenuDelegate getListMenuDelegate();

    ThumbnailProvider getThumbnailProvider();
}
