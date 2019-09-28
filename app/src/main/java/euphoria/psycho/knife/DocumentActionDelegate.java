package euphoria.psycho.knife;

import euphoria.psycho.common.base.BaseActionDelegate;
import euphoria.psycho.knife.cache.ThumbnailProvider;
import euphoria.psycho.knife.delegate.ListMenuDelegate;
import euphoria.psycho.knife.util.ThumbnailUtils;

public interface DocumentActionDelegate extends BaseActionDelegate<DocumentInfo> {

    void changePdfName(DocumentInfo item);

    void copyContent(DocumentInfo documentInfo);

    void copyFileName(DocumentInfo documentInfo);

    void delete(DocumentInfo documentInfo);

    void deleteLessFiles(DocumentInfo item);

    void extractPdfBookmark(DocumentInfo item);

    void extractVideoSrc(DocumentInfo documentInfo);


    ListMenuDelegate getListMenuDelegate();

    void getProperties(DocumentInfo documentInfo);

    ThumbnailUtils.ThumbnailProvider getThumbnailProvider();

    void html2Markdown1(DocumentInfo documentInfo);

    void onClicked(DocumentInfo documentInfo);

    void rename(DocumentInfo documentInfo);

    void setPdfName(DocumentInfo item);

    void splitPdfByTitleAndPageNumber(DocumentInfo item);

    void splitPdfByTitleList(DocumentInfo item);

    void srt2Txt(DocumentInfo documentInfo);

    void trimVideo(DocumentInfo documentInfo);

    void extractPdfToImage(DocumentInfo documentInfo);

    void extractPdfImages(DocumentInfo documentInfo);


    void unzip(DocumentInfo documentInfo);

    void updateItem(DocumentInfo documentInfo);
}
