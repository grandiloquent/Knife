package euphoria.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.Pair;


import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfOutline;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.annot.PdfAnnotation;
import com.itextpdf.kernel.utils.PageRange;
import com.itextpdf.kernel.utils.PdfSplitter;
import com.tom_roush.pdfbox.multipdf.Splitter;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation;
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitDestination;
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import euphoria.common.Bitmaps;
import euphoria.common.Contexts;
import euphoria.common.Dialogs;
import euphoria.common.Dialogs.Listener;
import euphoria.common.Files;
import euphoria.common.Strings;
import euphoria.psycho.knife.R;

import static euphoria.common.Files.getValidFileName;
import static euphoria.common.Strings.lines;
import static euphoria.common.Strings.paddingStartZero;
import static euphoria.common.Strings.substringAfterLast;
import static euphoria.common.Strings.substringBeforeLast;

public class Pdfs {
    private static final String TAG = "TAG/" + Pdfs.class.getSimpleName();
    private static boolean sInitialized = false;

    private void SplitByOutline(PdfDocument pdfDocument, String outlineTitle, String f) throws FileNotFoundException {
        int num = -1;
        int num2 = -1;
        PdfWriter writer = new PdfWriter(f);
        //writer.SetCompressionLevel();
        PdfDocument pdf = new PdfDocument(writer);

        int numberOfPages = pdfDocument.getNumberOfPages();
        for (int i = 1; i <= numberOfPages; i++) {
            PdfPage page = pdfDocument.getPage(i);
            List<PdfOutline> outlines = page.getOutlines(false);
            if (outlines != null) {
                for (PdfOutline current : outlines) {
                    if (current.getTitle().equals(outlineTitle)) {
                        num = pdfDocument.getPageNumber(page);
                        PdfOutline absoluteTreeNextOutline = getAbsoluteTreeNextOutline(current);
                        if (absoluteTreeNextOutline != null) {
                            num2 = pdfDocument.getPageNumber(getPageByOutline(pdfDocument, i, absoluteTreeNextOutline)) - 1;
                        } else {
                            num2 = numberOfPages;
                        }
                        if (num - num2 == 1) {
                            num2 = num;
                            break;
                        }
                        break;
                    }
                }
            }
        }
        if (num == -1 || num2 == -1) {
            return;
        }
        pdfDocument.copyPagesTo(num, num2, pdf);
        pdf.close();
    }

    public static void changeFileNameByMetadata(File file) throws Exception {
        PdfReader pdfReader = new PdfReader(file);
        PdfDocument pdfDocument = new PdfDocument(pdfReader);
        PdfDocumentInfo pdfDocumentInfo = pdfDocument.getDocumentInfo();
        String author = pdfDocumentInfo.getAuthor();
        String title = pdfDocumentInfo.getTitle();


        String fileName = "";
        if (!Strings.isNullOrWhiteSpace(title)) {
            fileName = title;
        } else {
            throw new Exception();
        }
        if (author != null && !author.equals("Unknown")) {
            fileName = fileName + " - " + author;
        }
        if (fileName.length() != 0) {
            fileName += ".pdf";
        }
        if (fileName.length() > 0) {
            File dst = new File(file.getParentFile(), getValidFileName(fileName, ' '));
            if (!dst.isFile())
                file.renameTo(dst);
        }
        pdfDocument.close();
    }


    private static void collectTitles(PdfOutline parent, StringBuilder sb) {

        List<PdfOutline> children = parent.getAllChildren();
        sb.append(parent.getTitle()).append('\n');
        if (children.size() > 0) {
            for (PdfOutline outline : children) {
                collectTitles(outline, sb);
            }
        }
    }

    public static String collectTitles(File pdfFile, boolean isDeep) throws IOException {
        PdfDocument document = new PdfDocument(new PdfReader(pdfFile));

        PdfOutline pdfOutline = document.getOutlines(true);


        StringBuilder sb = new StringBuilder();
        if (isDeep) {
            collectTitles(pdfOutline, sb);
        } else {
            sb.append(pdfOutline.getTitle()).append('\n');
            for (PdfOutline outline : pdfOutline.getAllChildren())
                sb.append(outline.getTitle()).append('\n');
        }
        return sb.toString();

    }

    //    public static void extractImages(String filename)
//            throws IOException, DocumentException {
//
//        File dir = new File(new File(filename).getParentFile(), "images");
//        if (!dir.isDirectory()) dir.mkdir();
//
//        PdfReader reader = new PdfReader(filename);
//
//        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
//        MyImageRenderListener listener = new MyImageRenderListener(dir.getAbsolutePath());
//        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
//            parser.processContent(i, listener);
//        }
//    }
//
//
//    private static class MyImageRenderListener implements RenderListener {
//        private String path;
//
//        public MyImageRenderListener(String path) {
//            this.path = path;
//        }
//
//        @Override
//        public void beginTextBlock() {
//
//        }
//
//        @Override
//        public void renderText(TextRenderInfo renderInfo) {
//
//        }
//
//        @Override
//        public void endTextBlock() {
//
//        }
//
//        @Override
//        public void renderImage(ImageRenderInfo renderInfo) {
//            try {
//                String filename;
//                FileOutputStream os;
//                PdfImageObject image = renderInfo.getImage();
//                image.getBufferedImage();
//
//                if (image == null) return;
////                filename = String.format(path, renderInfo.getRef().getNumber(),
////                        Strings.isNullOrWhiteSpace(image.getFileType()) ? ".jpg" :
////                                image.getFileType()
////                );
//                filename = new File(path,
//                        Strings.isNullOrWhiteSpace(image.getFileType()) ? ".jpg" : image.getFileType()).getAbsolutePath();
//                os = new FileOutputStream(filename);
//                os.write(image.getImageAsBytes());
//                os.flush();
//                os.close();
//            } catch (IOException e) {
//                System.out.println(e.getMessage());
//            }
//        }
//    }

    public static void extractPdfImages(Context context, File file, String prefix, Listener listener) {
        Dialogs.showDialog(context, null, text -> {

            int number = Strings.parseIntSafely(text.toString(), -1);
            if (number == -1 || number - 1 < 0) {
                Contexts.toast(context, "请输入正确的页码");
            } else {
                try {
                    List<Integer> numbers = Strings.parseIntsSafely(text.toString());

                    int start = 0;
                    int end = 0;

                    if (numbers.size() == 0) return;
                    if (numbers.size() > 1) {
                        end = numbers.get(1);
                    }
                    start = numbers.get(0) - 1;

                    extractPdfImages(context, file, prefix, start, end);
                    if (listener != null) listener.onSuccess(null);
                } catch (Exception e) {

                    Log.e(TAG, "Debug: extractPdfImages, " + e);

                }
            }
        });
    }

    private static void extractPdfImages(Context context, File file, String prefix, int start, int end) throws IOException {

    }

    public static void extractPdfToImage(Context context, File file, Listener listener) {
        Dialogs.showDialog(context, null, text -> {

            try {
                File dir = new File(file.getAbsolutePath().replaceFirst("\\.\\w+$", " "));
                if (!dir.isDirectory()) dir.mkdirs();
                splitPdfInRange(file, text.toString(), dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            int number = Strings.parseIntSafely(text.toString(), -1);
//            if (number == -1 || number - 1 < 0) {
//                Contexts.toast(context, "请输入正确的页码");
//            } else {
//                try {
//                    Bitmap bitmap = extractPdfToImage(context, file, number - 1);
//                    File out = new File(file.getParentFile(), String.format(Locale.CHINA, "%s-%d.jpg",
//                            Files.getFileNameWithoutExtension(file.getName()), number));
//
//                    Bitmaps.saveAsJpg(bitmap, out);
//                    bitmap.recycle();
//                    if (listener != null) listener.onSuccess(null);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
        });
    }

    private static Bitmap extractPdfToImage(Context context, File file, int pageNum) throws Exception {
//        ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
//
//        PdfiumCore pdfiumCore = new PdfiumCore(context);
//        PdfDocument pdfDocument = pdfiumCore.newDocument(fd);
//
//        pdfiumCore.openPage(pdfDocument, pageNum);
//
//        int width = pdfiumCore.getPageWidth(pdfDocument, pageNum);
//        int height = pdfiumCore.getPageHeight(pdfDocument, pageNum);
//        // ARGB_8888 - best quality, high memory usage, higher possibility of OutOfMemoryError
//        // RGB_565 - little worse quality, twice less memory usage
//        Bitmap bitmap = Bitmap.createBitmap(width, height,
//                Config.ARGB_8888);
//        pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageNum, 0, 0,
//                width, height);
//        //if you need to render annotations and form fields, you can use
//        //the same method above adding 'true' as last param
//
//
//        pdfiumCore.closeDocument(pdfDocument); // important!
//        return bitmap;

        return null;
    }

    public static void extractText(Context context, File file, Listener listener) {
        Dialogs.showDialog(context, null, text -> {

            int number = Strings.parseIntSafely(text.toString(), -1);
            if (number == -1 || number - 1 < 0) {
                Contexts.toast(context, "请输入正确的页码");
            } else {
                try {
                    List<Integer> numbers = Strings.parseIntsSafely(text.toString());

                    int start = 0;
                    int end = 0;

                    if (numbers.size() == 0) return;
                    if (numbers.size() > 1) {
                        end = numbers.get(1);
                    }
                    start = numbers.get(0);
                    if (start == 0) start = 1;
                    if (end == 0) end = start + 1;

                    String result = extractText(file, start, end);
                    if (listener != null) listener.onSuccess(result);
                } catch (Exception e) {

                    Log.e(TAG, "Debug: extractPdfImages, " + e);

                }
            }
        });
    }

    //    private static Bitmap from1Bit(PDImage pdImage) throws IOException {
//        final PDColorSpace colorSpace = pdImage.getColorSpace();
//        final int width = pdImage.getWidth();
//        final int height = pdImage.getHeight();
//        Bitmap raster = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8);
//        final float[] decode = getDecodeArray(pdImage);
//        ByteBuffer buffer = ByteBuffer.allocate(raster.getRowBytes() * height);
//        raster.copyPixelsToBuffer(buffer);
//        byte[] output = buffer.array();
//
//        // read bit stream
//        InputStream iis = null;
//        try {
//            // create stream
//            iis = pdImage.createInputStream();
//            final boolean isIndexed =
//                    false; // TODO: PdfBox-Android colorSpace instanceof PDIndexed;
//
//            int rowLen = width / 8;
//            if (width % 8 > 0) {
//                rowLen++;
//            }
//
//            // read stream
//            byte value0;
//            byte value1;
//            if (isIndexed || decode[0] < decode[1]) {
//                value0 = 0;
//                value1 = (byte) 255;
//            } else {
//                value0 = (byte) 255;
//                value1 = 0;
//            }
//            byte[] buff = new byte[rowLen];
//            int idx = 0;
//            for (int y = 0; y < height; y++) {
//                int x = 0;
//                int readLen = iis.read(buff);
//                for (int r = 0; r < rowLen && r < readLen; r++) {
//                    int value = buff[r];
//                    int mask = 128;
//                    for (int i = 0; i < 8; i++) {
//                        int bit = value & mask;
//                        mask >>= 1;
//                        output[idx++] = bit == 0 ? value0 : value1;
//                        x++;
//                        if (x == width) {
//                            break;
//                        }
//                    }
//                }
//                if (readLen != rowLen) {
//                    Log.w("PdfBox-Android", "premature EOF, image will be incomplete");
//                    break;
//                }
//            }
//
//
//            buffer.rewind();
//            raster.copyPixelsFromBuffer(buffer);
//            // use the color space to convert the image to RGB
//            return colorSpace.toRGBImage(raster);
//        } finally {
//            if (iis != null) {
//                iis.close();
//            }
//        }
//        return null;
//    }

    private static String extractText(File file, int start, int end) throws IOException {
//        PDFTextStripper pdfStripper = null;
//        PDDocument pdDoc = null;
//
//        PDFParser parser = new PDFParser(new RandomAccessBuffer(new FileInputStream(file)));
//
//        parser.parse();
//
//        COSDocument cosDoc = parser.getDocument();
//        pdfStripper = new PDFTextStripper();
//        pdDoc = new PDDocument(cosDoc);
//        pdfStripper.setStartPage(start);
//        pdfStripper.setEndPage(end);
//        String parsedText = pdfStripper.getText(pdDoc);
//        cosDoc.close();
//        return parsedText;
        return null;
    }

    private static PdfOutline getAbsoluteTreeNextOutline(PdfOutline outline) {
        PdfObject nextPdfObject = outline.getContent().get(PdfName.Next);
        PdfOutline nextPdfOutline = null;
        if (outline.getParent() != null && nextPdfObject != null) {
            Iterator var4 = outline.getParent().getAllChildren().iterator();

            while (var4.hasNext()) {
                PdfOutline pdfOutline = (PdfOutline) var4.next();
                if (pdfOutline.getContent().getIndirectReference().equals(nextPdfObject.getIndirectReference())) {
                    nextPdfOutline = pdfOutline;
                    break;
                }
            }
        }

        if (nextPdfOutline == null && outline.getParent() != null) {
            nextPdfOutline = getAbsoluteTreeNextOutline(outline.getParent());
        }

        return nextPdfOutline;
    }

    private static PdfPage getPageByOutline(PdfDocument pdfDocument, int fromPage, PdfOutline outline) {
        int size = pdfDocument.getNumberOfPages();

        for (int i = fromPage; i <= size; ++i) {
            PdfPage pdfPage = pdfDocument.getPage(i);
            List<PdfOutline> outlineList = pdfPage.getOutlines(false);
            if (outlineList != null) {
                Iterator var7 = outlineList.iterator();

                while (var7.hasNext()) {
                    PdfOutline pdfOutline = (PdfOutline) var7.next();
                    if (pdfOutline.equals(outline)) {
                        return pdfPage;
                    }
                }
            }
        }

        return null;
    }

    public static void splitBy(String marks, String fileName) throws IOException {

        List<Integer> integers = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();
        List<String> lines = lines(marks);
        for (String l : lines) {
            integers.add(Integer.parseInt(substringAfterLast(l, '-').trim()) + 1);
            fileNames.add(getValidFileName(substringBeforeLast(l, '-').trim(), ' '));
        }

        File dstDir = new File(new File(fileName).getParentFile(), substringBeforeLast(new File(fileName).getName(), '.'));
        if (!dstDir.isDirectory()) dstDir.mkdir();
        PdfReader pdfReader = new PdfReader(fileName);
        PdfSplitter splitter = new PdfSplitter(new PdfDocument(pdfReader)) {
            int partNumber = 1;

            @Override
            protected PdfWriter getNextPdfWriter(PageRange documentPageRange) {

                try {
                    return new PdfWriter(new File(dstDir,
                            String.format("%s_%03d%s",
                                    paddingStartZero(fileNames.get(partNumber - 1),
                                            3), partNumber++, ".pdf")));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException();
                }
            }
        };
        List<PdfDocument> pdfDocuments = splitter.splitByPageNumbers(integers);

        for (PdfDocument document : pdfDocuments) {
            int numbers = document.getNumberOfPages();
            for (int i = 1; i <= numbers; i++) {
                PdfPage pdfPage = document.getPage(i);
                for (PdfAnnotation annotation : pdfPage.getAnnotations()) {
                    pdfPage.removeAnnotation(annotation);
                }

            }
            document.close();
        }
        pdfReader.close();
    }

    public static void splitByOutline(PdfDocument pdfDocument, String outlineTitle, String f) throws FileNotFoundException {
        int startPage = -1;
        int endPage = -1;
        PdfWriter writer = new PdfWriter(f);
        //writer.SetCompressionLevel();
        PdfDocument pdf = new PdfDocument(writer);
        int size = pdfDocument.getNumberOfPages();

        for (int i = 1; i <= size; ++i) {
            PdfPage pdfPage = pdfDocument.getPage(i);
            List<PdfOutline> outlineList = pdfPage.getOutlines(false);
            if (outlineList != null) {
                Iterator var9 = outlineList.iterator();

                while (var9.hasNext()) {
                    PdfOutline pdfOutline = (PdfOutline) var9.next();
                    if (pdfOutline.getTitle().equals(outlineTitle)) {
                        startPage = pdfDocument.getPageNumber(pdfPage);
                        PdfOutline nextOutLine = getAbsoluteTreeNextOutline(pdfOutline);
                        if (nextOutLine != null) {
                            endPage = pdfDocument.getPageNumber(getPageByOutline(pdfDocument, i, nextOutLine)) - 1;
                        } else {
                            endPage = size;
                        }

                        if (startPage - endPage == 1) {
                            endPage = startPage;
                        }
                        break;
                    }
                }
            }
        }

        if (startPage != -1 && endPage != -1) {
            pdfDocument.copyPagesTo(startPage, endPage, pdf);
            pdf.close();
        }
    }

    public static void splitByTitles(File file, List<String> titles, File dir) throws IOException {
        PdfDocument document = new PdfDocument(new PdfReader(file));

//        PdfOutline pdfOutline = document.getOutlines(true);
//
//
//        List<PdfOutline> children = pdfOutline.getAllChildren();
//        List<String> list = new ArrayList<>();
//
//        for (PdfOutline element : children) {
//            List<PdfOutline> childrenSon = element.getAllChildren();
//            if (isDeep && childrenSon != null && childrenSon.size() > 0) {
//                for (PdfOutline outline : childrenSon) {
//                    list.add(outline.getTitle());
//                }
//            } else {
//                list.add(element.getTitle());
//            }
//        }
//        int count = 1;
//        for (String element : list) {
//
//            splitByOutline(document, element, new File(file.getParentFile(), String.format("%03d_%s.pdf", count++, Files.getValidFileName(element, ' '))).getAbsolutePath());
//
//        }

        List<PdfDocument> splitDocuments = new PdfSplitter(document) {
            int partNumber = 1;

            @Override
            protected PdfWriter getNextPdfWriter(PageRange documentPageRange) {
                try {
                    String filename = getValidFileName(titles.get(partNumber - 1), ' ').replaceAll("\\s+", " ");
                    filename = paddingStartZero(filename, 3);
                    File f = new File(dir,
                            String.format("%s_%03d%s", filename, partNumber++, ".pdf"));

                    Log.e(TAG, "Debug: getNextPdfWriter, " + f.getAbsolutePath());

                    return new PdfWriter(f);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException();
                }
            }
        }.splitByOutlines(titles);

        for (PdfDocument doc : splitDocuments)
            doc.close();
    }

    public static void splitByTitlesBox(File file, List<String> titles, File dir) throws IOException {
        PDDocument pdDocument = PDDocument.load(file);
        PDDocumentOutline pdDocumentOutline = pdDocument.getDocumentCatalog().getDocumentOutline();
        PDOutlineItem pdOutlineItem = pdDocumentOutline.getFirstChild();
        List<Pair<String, Integer>> outlines = new ArrayList<>();
        do {


            String title = pdOutlineItem.getTitle();
            if (title == null) continue;
            PDDestination pdDestination = pdOutlineItem.getDestination();
            if (pdDestination instanceof PDPageFitDestination) {
                PDPageFitDestination pdPageFitDestination = (PDPageFitDestination) pdDestination;
                int numberOfPage = pdPageFitDestination.getPageNumber();
                outlines.add(Pair.create(title, numberOfPage));
            }
            Iterator<PDOutlineItem> itemIterator = pdOutlineItem.children().iterator();
            while (itemIterator.hasNext()) {
                PDOutlineItem item = itemIterator.next();
                title = item.getTitle();
                if (title == null) continue;
            }

        } while ((pdOutlineItem = pdOutlineItem.getNextSibling()) != null);
        Splitter splitter = new Splitter();
    }

    public static void splitPdfInRange(File file, String range, File dir) throws IOException {
        if (Strings.containsLetter(range)) {
            splitBy(range, file.getAbsolutePath());
            return;
        }

        PdfDocument document = new PdfDocument(new PdfReader(file));


        List<Integer> numbers = new ArrayList<>();
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(range);

        while (m.find()) {
            numbers.add(Integer.parseInt(m.group()));
        }

        if (numbers.size() == 1) {
            //numbers.add(document.getNumberOfPages());
        } else {
            int tmp = numbers.get(0);
            numbers.set(0, Math.min(tmp, numbers.get(1)));
            numbers.set(1, Math.max(tmp, numbers.get(1)));
        }
        if (numbers.get(0) == 1) {
            try {
                Contexts.setText(Contexts.getContext(), Pdfboxs.extractOutlines(file));
            } catch (Exception ignore) {

                Log.e(TAG, "Debug: splitPdfInRange, " + ignore);

            }
            return;
        }

        if (numbers.get(0) == 2) {
            Contexts.setText(Contexts.getContext(), collectTitles(file, true));
            return;
        }

        String extension = ".pdf";
        int count = 1;

        List<PdfDocument> splitDocuments = new PdfSplitter(document) {
            int partNumber = 1;

            @Override
            protected PdfWriter getNextPdfWriter(PageRange documentPageRange) {
                try {
                    return new PdfWriter(new File(dir,
                            String.format("%s%03d%s", "splitted_", partNumber++, extension)));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException();
                }
            }
        }.splitByPageNumbers(numbers);
        for (PdfDocument doc : splitDocuments)
            doc.close();

    }

    public interface Listener {
        void onSuccess(String text);
    }
}
/*

 */