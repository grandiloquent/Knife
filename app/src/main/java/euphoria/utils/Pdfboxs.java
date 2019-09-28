package euphoria.utils;


import android.util.Log;
import android.util.Pair;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageTree;
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDAction;
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitDestination;
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import euphoria.common.Contexts;
import euphoria.common.Logs;


public class Pdfboxs {
    private static final int BUFFER_SIZE = 8192;
    private static final String TAG = "TAG/" + Pdfboxs.class.getSimpleName();
    private static boolean sInitailized;
    private static final
    char[] sInvalidFileNamechars = {'\n', '\"', '<', '>', '|', '\0', (char) 1, (char) 2, (char) 3, (char) 4, (char) 5, (char) 6, (char) 7, (char) 8, (char) 9, (char) 10, (char) 11, (char) 12, (char) 13, (char) 14, (char) 15, (char) 16, (char) 17, (char) 18, (char) 19, (char) 20, (char) 21, (char) 22, (char) 23, (char) 24, (char) 25, (char) 26, (char) 27, (char) 28, (char) 29, (char) 30, (char) 31, ':', '*', '?', '\\', '/'};

    public static void changeFileName(File file) throws Exception {
        if (!sInitailized) {
            sInitailized = true;
            PDFBoxResourceLoader.init(Contexts.getContext());
        }
        PDDocument pdDocument = PDDocument.load(file);
        PDDocumentInformation pdDocumentInformation = pdDocument.getDocumentInformation();


        String author = pdDocumentInformation.getAuthor();
        String title = pdDocumentInformation.getTitle();
        pdDocument.close();


        String fileName = "";
        if (title != null) {
            fileName = title;
        } else {
            throw new NullPointerException("The title is null.");
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
    }

    public static long copy(InputStream source, OutputStream sink)
            throws IOException {
        long nread = 0L;
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        while ((n = source.read(buf)) > 0) {
            sink.write(buf, 0, n);
            nread += n;
        }
        return nread;
    }

    public static String extractOutlines(File pdfFile) throws IOException {
        if (!sInitailized) {
            sInitailized = true;
            PDFBoxResourceLoader.init(Contexts.getContext());
        }
        PDDocument pdDocument = PDDocument.load(pdfFile);
        PDDocumentOutline pdDocumentOutline = pdDocument.getDocumentCatalog().getDocumentOutline();
        PDOutlineItem pdOutlineItem = pdDocumentOutline.getFirstChild();
        List<Pair<String, Integer>> outlines = new ArrayList<>();
        do {


            parseOutline(pdOutlineItem, outlines);
            Iterator<PDOutlineItem> itemIterator = pdOutlineItem.children().iterator();
            while (itemIterator.hasNext()) {
                PDOutlineItem item = itemIterator.next();
                parseOutline(item, outlines);
            }

        } while ((pdOutlineItem = pdOutlineItem.getNextSibling()) != null);
        pdDocument.close();

        outlines = removeRedundancy(outlines);

        StringBuilder sb = new StringBuilder();
        for (Pair<String, Integer> o : outlines) {
            sb.append(o.first).append(" - ").append(o.second).append('\n');
        }

        Log.e(TAG, "Debug: extractOutlines, "
                + pdfFile.getAbsolutePath()
                + "\n"
                + sb.toString());

        return sb.toString();


    }

    public static void fixOutline(File pdfFile) throws IOException {
        PDDocument pdDocument = PDDocument.load(pdfFile);
        pdDocument.getDocumentCatalog().setDocumentOutline(new PDDocumentOutline());
        pdDocument.save(new File(pdfFile.getParentFile(), "fixed_" + pdfFile.getName()));
        pdDocument.close();
    }

    public static String getValidFileName(String value, char c) {

        int len = Math.min(125, value.length());

        char[] buffer = new char[len];

        for (int i = 0; i < len; i++) {
            char ch = value.charAt(i);
            for (int j = 0; j < sInvalidFileNamechars.length; j++) {
                if (ch == sInvalidFileNamechars[j]) {
                    ch = c;
                    break;
                }
            }
            buffer[i] = ch;
        }
        return new String(buffer).trim();
    }

    private static void parseOutline(PDOutlineItem pdOutlineItem, List<Pair<String, Integer>> outlines) throws IOException {
        String title = pdOutlineItem.getTitle();
        if (title == null) return;


        PDDestination pdDestination = pdOutlineItem.getDestination();
        if (pdDestination == null) {
            PDAction action = pdOutlineItem.getAction();
            if (action instanceof PDActionGoTo) {
                PDActionGoTo pdActionGoTo = (PDActionGoTo) action;
                pdDestination = pdActionGoTo.getDestination();
            }
        }
        if (pdDestination instanceof PDPageFitDestination) {
            PDPageFitDestination pdPageFitDestination = (PDPageFitDestination) pdDestination;
            int numberOfPage = pdPageFitDestination.getPageNumber();
            if (numberOfPage == -1) numberOfPage = pdPageFitDestination.retrievePageNumber();
            outlines.add(Pair.create(title, numberOfPage));
        }
    }


    private static List<Pair<String, Integer>> removeRedundancy(List<Pair<String, Integer>> outlines) {
        List<Pair<String, Integer>> retVal = new ArrayList<>();
        for (int i = 0, j = outlines.size(); i < j; i++) {
            int k = i;
            int pageNumber = outlines.get(i).second;
            while (k + 1 < j && pageNumber == outlines.get(k + 1).second) {
                k++;
            }
            i = k;

            retVal.add(outlines.get(i));
        }

        return retVal;
    }

    private static void saveSplitFiles(PDDocument pdDocument,
                                       File destinationDirectory,
                                       List<Pair<String, Integer>> outlines) throws IOException {
        PDPageTree pdPageTree = pdDocument.getPages();
        int count = 1;
        for (int i = 0, j = outlines.size(); i < j; i++) {
            int pageNumber = outlines.get(i).second;
            int nextPageNumber = i + 1 < j ? outlines.get(i + 1).second : pdDocument.getNumberOfPages();
            PDDocument newDocument = new PDDocument();

            File targetFile = new File(destinationDirectory,
                    String.format("%03d_%s.pdf", count++, getValidFileName(outlines.get(i).first, ' ')));
            for (int k = pageNumber; k < nextPageNumber; k++) {

                PDPage pdPage = pdPageTree.get(k);
                newDocument.addPage(pdPage);
            }

            newDocument.save(targetFile);
            newDocument.close();

        }
    }

    public static void setTitle(File pdfFile, String title) throws IOException {
        if (!sInitailized) {
            sInitailized = true;
            PDFBoxResourceLoader.init(Contexts.getContext());
        }
        PDDocument pdDocument = PDDocument.load(pdfFile);
        PDDocumentInformation pdDocumentInformation = pdDocument.getDocumentInformation();
        pdDocumentInformation.setTitle(title);

        Log.e(TAG, "Debug: setTitle, " + title + " " + pdfFile);

        pdDocument.save(pdfFile);
        pdDocument.close();
    }

    public static void splitByOutline(File pdfFile) throws IOException {
        PDDocument pdDocument = PDDocument.load(pdfFile);
        PDDocumentOutline pdDocumentOutline = pdDocument.getDocumentCatalog().getDocumentOutline();
        PDOutlineItem pdOutlineItem = pdDocumentOutline.getFirstChild();
        List<Pair<String, Integer>> outlines = new ArrayList<>();
        do {


            parseOutline(pdOutlineItem, outlines);
            Iterator<PDOutlineItem> itemIterator = pdOutlineItem.children().iterator();
            while (itemIterator.hasNext()) {
                PDOutlineItem item = itemIterator.next();
                parseOutline(item, outlines);
            }

        } while ((pdOutlineItem = pdOutlineItem.getNextSibling()) != null);
        outlines = removeRedundancy(outlines);

        saveSplitFiles(pdDocument, pdfFile.getParentFile(), outlines);
        pdDocument.close();
    }

}
