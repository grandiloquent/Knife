package euphoria.psycho.knife.server;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.nanohttpd.fileupload.NanoFileUpload;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import euphoria.common.Strings;
import euphoria.psycho.share.util.FileUtils;
import euphoria.psycho.share.util.MimeUtils;


public class WebServer extends NanoHTTPD {

    private static final String UTF8 = "utf-8";
    private File mStaticDirectory;
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
    private File mUploadDirectory;
    private NanoFileUpload mNanoFileUpload;
    private File mStartDirectory;
    private String mTreeUri;
    private Context mContext;

    public WebServer(int port) {
        super(port);
        initialize();
    }

    WebServer(String hostname, int port) {
        super(hostname, port);
        initialize();
    }

    private Response generateDirectoryPage(File dir, int sortBy) {

        File[] children = dir.listFiles();
        FileUtils.sortFiles(children, sortBy, true);
        byte[][] indexBytes = DataProvider.getIndex();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            os.write(indexBytes[0]);
            os.write(Strings.escapeHTML(dir.getAbsolutePath()).getBytes(UTF8));
            os.write(indexBytes[1]);
            os.write(generateFileInfos(children));
            os.write(indexBytes[2]);
            os.write(Integer.toString(children.length).getBytes(UTF8));
            os.write(indexBytes[3]);

        } catch (IOException e) {
            return getInternalErrorResponse(e.getMessage());
        }
        return Response.newFixedLengthResponse(Status.OK,
                MimeUtils.guessMimeTypeFromExtension("html"),
                os.toByteArray());
    }

    private byte[] generateFileInfos(File[] files) {

        byte[][] bytes = DataProvider.getFileList();
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        for (File file : files) {
            try {
                os.write(bytes[0]);
                if (file.isDirectory()) {
                    os.write("icon-file-m".getBytes(UTF8));
                } else {
                    os.write("icon-tdoc-m".getBytes(UTF8));
                }
                os.write(bytes[1]);
                os.write("/files?p=".getBytes(UTF8));
                os.write(encodeURI(file.getAbsolutePath()));
                os.write(bytes[2]);
                os.write(file.getName().getBytes(UTF8));
                os.write(bytes[3]);
                os.write(mSimpleDateFormat.format(file.lastModified()).getBytes(UTF8));
                os.write(bytes[4]);
                if (file.isFile())
                    os.write(FileUtils.formatFileSize(file.length()).getBytes(UTF8));
                os.write(bytes[5]);

            } catch (IOException e) {
            }
        }


        return os.toByteArray();

    }

    String getURL() {
        return getHostname() + ":" + myPort;
    }

    private Response handleIndex() {


        return generateDirectoryPage(mStartDirectory, 0);

    }

    private Response handleQueryFile(Map<String, List<String>> parameters, Map<String, String> headers) {

        try {
            String path = URLDecoder.decode(ServerUtils.getStringFromMap(parameters, "p", null), UTF8);
            File file = new File(path);
            if (file.isDirectory()) {
                int sortBy = ServerUtils.getIntFromMap(parameters, "sort", 0);
                return generateDirectoryPage(file, sortBy);
            } else if (file.isFile()) {
                String ext = FileUtils.getExtension(file.getName());
                switch (ext) {
                    case "mp4":
                        return handleVideo(file, headers);
                    default:
                        try {
                            FileInputStream inputStream = new FileInputStream(file);
                            Response response = Response.newFixedLengthResponse(Status.OK,
                                    ServerUtils.getMimeType(file.getName(), "*/*"),
                                    inputStream,
                                    file.length());
                            // For browser to generate a more suitable filename
                            response.addHeader(ServerUtils.HTTP_CONTENT_DISPOSITION,
                                    String.format("attachment; filename=\"%s\"", file.getName()));
                            return response;
                        } catch (Exception e) {
                            return getInternalErrorResponse(e.getMessage());
                        }
                }
            }
            return getNotFoundResponse();
        } catch (UnsupportedEncodingException e) {
            return getInternalErrorResponse(e.getMessage());
        }

    }

    private Response handleStaticFile(String uri) {
        int dotIndex = uri.lastIndexOf('.');
        if (dotIndex == -1) return null;
        String extension = uri.substring(dotIndex + 1);
        String mimeType = MimeUtils.guessMimeTypeFromExtension(extension);
        File file = new File(mStaticDirectory, uri);
        if (!file.exists()) return getNotFoundResponse();
        try {

            FileInputStream is = new FileInputStream(file);
            Response response = Response.newFixedLengthResponse(Status.OK, mimeType, is, file.length());
            addHeaderForFile(response, file);
            return response;
        } catch (FileNotFoundException e) {
            return getInternalErrorResponse(e.getMessage());
        }
    }

    private Response handleUploadFile(IHTTPSession session) {
        if (!mUploadDirectory.isDirectory()) return getInternalErrorResponse("Not implements");

        // Parse session
        if (mNanoFileUpload == null) {
            mNanoFileUpload = new NanoFileUpload(new DiskFileItemFactory(10240, mUploadDirectory));
        }
        try {
            Map<String, List<FileItem>> map = mNanoFileUpload.parseParameterMap(session);


            // Respond JSON
            Map<String, Boolean> results = new HashMap<>();
            Gson gson = new GsonBuilder().create();
            StringWriter writer = new StringWriter();


            for (String s : map.keySet()) {

                List<FileItem> fileItems = map.get(s);
                for (FileItem fileItem : fileItems) {
                    DiskFileItem diskFileItem = (DiskFileItem) fileItem;
                    String disposition = fileItem.getHeaders().getHeader("content-disposition");
                    String fileName = ServerUtils.getFileNameFromContentDisposition(disposition);


                    Log.e("TAG/WebServer", "handleUploadFile: " + fileItem.getName());

                    if (fileName != null) {
                        File dstFile = new File(mUploadDirectory, fileName);
                        dstFile = FileUtils.getUniqueFile(dstFile);

                        // when the file has been read into memory
                        if (diskFileItem.isInMemory()) {

                            try (InputStream inputStream = diskFileItem.getInputStream()) {
                                ServerUtils.copyToFile(inputStream, dstFile);
                                results.put(dstFile.getName(), true);
                            } catch (IOException e) {
                                results.put(dstFile.getName(), false);
                            }
                        } else {

                            Log.e("TAG/WebServer", "handleUploadFile: " + diskFileItem.getStoreLocation());

                            // 从临时文件复制到上传文件夹
                            boolean success = diskFileItem.getStoreLocation()
                                    .renameTo(dstFile);

                            results.put(dstFile.getName(), success);
                        }
                    } else {
                        results.put(diskFileItem.getName(), false);
                    }
                }


            }

            // Convert map to json string
            gson.toJson(results, writer);

            return Response.newFixedLengthResponse(Status.OK, ServerUtils.getMimeType(".json", "*/*"),
                    writer.toString());

        } catch (FileUploadException e) {
            return getInternalErrorResponse(e.getMessage());
        }

    }

    private Response handleVideo(File file, Map<String, String> headers) {

        try {

            long[] values = ServerUtils.parseRange(headers);
            long start = values[0];
            long end = values[1];
            if (end <= 0) {
                end = file.length() - 1;
            }
            long newLen = end - start + 1;
            FileInputStream is = new FileInputStream(file);
            Response response;
            if (start > 0) {
                is.skip(start);


                response = Response.newFixedLengthResponse(Status.PARTIAL_CONTENT,
                        ServerUtils.getMimeType(file.getName(), "*/*"), is, newLen);


            } else {

                response = Response.newFixedLengthResponse(Status.OK,
                        ServerUtils.getMimeType(file.getName(), "*/*"), is, newLen);

            }
            response.addHeader(ServerUtils.HTTP_CONTENT_RANGE, "bytes "
                    + start
                    + "-"
                    + end
                    + "/"
                    + file.length());
            response.addHeader(ServerUtils.HTTP_CONTENT_LENGTH, Long.toString(newLen));
            response.addHeader(ServerUtils.HTTP_ACCEPT_RANGES, "bytes");
            response.addHeader(ServerUtils.HTTP_ETAG, ServerUtils.generateETag(file));
            response.addHeader(ServerUtils.HTTP_LAST_MODIFIED, ServerUtils.getGMTDateTime(file.lastModified()));


//            Log.e("TAG/WebServer", "handleVideo: "
//                    + "\n start = " + start
//                    + "\n end = " + end
//                    + "\n newLen = " + newLen
//                    + "\n fileLenght = " + file.length());


            return response;
        } catch (Exception e) {
            return getInternalErrorResponse(e.getMessage());
        }

    }

    private void initialize() {
        setHTTPHandler(this::respond);
    }

    // 主要方法
    private Response respond(IHTTPSession input) {


        Response response = null;
        String uri = input.getUri();
        Map<String, List<String>> parameters;


        if (uri.equals("/")) {
            /// response = handleStaticFile("/index.html");
            response = handleIndex();
        } else if (uri.indexOf('.') != -1) {

            response = handleStaticFile(uri);

        } else if (input.getMethod() == Method.POST && uri.equals("/upload")) {
            response = handleUploadFile(input);
        } else {


            parameters = input.getParameters();
            if (parameters.containsKey("p")) {


                response = handleQueryFile(parameters, input.getHeaders());

            }

        }
        if (response == null)
            response = getNotFoundResponse();
        response.addHeader(ServerUtils.HTTP_X_POWERED_BY, "Java/NanoHTTPD");
        response.addHeader(ServerUtils.HTTP_SERVER, "NanoHTTPD");
        return response;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void setStartDirectory(File startDirectory) {
        mStartDirectory = startDirectory;
    }

    void setStaticDirectory(File staticDirectory) {
        mStaticDirectory = staticDirectory;
    }

    public void setTreeUri(String treeUri) {
        mTreeUri = treeUri;
    }

    void setUploadDirectory(File uploadDirectory) {
        mUploadDirectory = uploadDirectory;
    }

    private static void addHeaderForFile(Response response, File file) {
        response.addHeader(ServerUtils.HTTP_ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        String ext = FileUtils.getExtension(file.getName());

        if (ext == null) return;
        switch (ext.toLowerCase()) {
            case "svg":
            case "png":
                response.addHeader(ServerUtils.HTTP_CACHE_CONTROL, "max-age=" + 259200 /*3 Days*/);
                response.addHeader(ServerUtils.HTTP_EXPIRES, ServerUtils.getGMTDateTime(259200));
                break;
        }

        response.addHeader(ServerUtils.HTTP_CONNECTION, "close");


    }

    private static byte[] encodeURI(String str) {
        try {
            return URLEncoder.encode(str, UTF8).getBytes(UTF8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static Response getInternalErrorResponse(String s) {
        return Response.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "INTERNAL ERROR: " + s);
    }

    private static Response getNotFoundResponse() {
        return Response.newFixedLengthResponse(Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Error 404, file not found.");
    }


}
