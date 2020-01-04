package euphoria.psycho.knife.download;

import android.os.SystemClock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import euphoria.common.Files;
import euphoria.psycho.common.log.FileLogger;

import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_GONE;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;


public class DownloadThread extends Thread {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int DEFAULT_DELAY_TIME = 5 * 1000;
    private static final int DEFAULT_RETRIES = 20;
    private static final String HTTP_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String HTTP_CONNECTION = "Connection";
    private static final String HTTP_CONTENT_LENGTH = "Content-Length";
    private static final String HTTP_RANGE = "Range";
    private static final int HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    private static final String HTTP_TRANSFER_ENCODING = "Transfer-Encoding";
    private static final String HTTP_USER_AGENT = "User-Agent";
    private static final int MIN_PROGRESS_STEP = 65536;
    private static final long MIN_PROGRESS_TIME = 2000;
    private DownloadInfo mInfo;
    private AtomicBoolean mIsStop = new AtomicBoolean();
    private long mLastUpdateBytes = 0;
    private long mLastUpdateTime = 0;
    private DownloadObserver mObserver;
    private long mSpeed;
    private long mSpeedSampleBytes = 0L;
    private long mSpeedSampleStart = 0L;
    private int mTimeout = 20 * 1000;

    public DownloadThread(DownloadInfo info, DownloadObserver observer) {


        mInfo = info;
        mObserver = observer;

    }

    private void addRequestHeaders(HttpsURLConnection c) {


        // identity
        // Indicates the identity function (i.e. no compression,
        // nor modification). This value is always considered
        // as acceptable, even if not present.

        c.addRequestProperty(HTTP_ACCEPT_ENCODING, "identity");
        // close
        // Indicates that either the client or the server would
        // like to close the connection. This is the default on
        // HTTP/1.0 requests.

        c.addRequestProperty(HTTP_CONNECTION, "close");
        c.addRequestProperty(HTTP_USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.92 Safari/537.36");
        File file = new File(mInfo.filePath);
        if (file.exists()) {
            mInfo.bytesReceived = file.length();
            c.addRequestProperty(HTTP_RANGE, "bytes=" + mInfo.bytesReceived + "-");
        } else {
            mInfo.bytesReceived = 0L;
        }
    }

    private void executeDownload() throws DownloadRequestException {


        URL url;

        try {
            url = new URL(mInfo.url);
        } catch (MalformedURLException e) {
            throw new DownloadRequestException(DownloadStatus.FAILED, e);
        }
        SSLContext appContext = null;
        try {
            appContext = SSLContext.getDefault();
        } catch (NoSuchAlgorithmException e) {


        }
        // https://developer.android.com/reference/java/net/HttpURLConnection.html


        int tries = 0;
        while (tries++ < DEFAULT_RETRIES) {


            if (tries > 1) {
                mInfo.status = DownloadStatus.RETIRED;
                mInfo.message = Integer.toString(tries);
                mObserver.updateProgress(mInfo);

            }
            //mInfo.listener.onStatusChanged(mTaskId, "第 " + tries + " 尝试下载 " + FileUtils.getFileName(mInfo.fileName));
            HttpsURLConnection c = null;

            try {
                if (mIsStop.get()) {
                    throw new DownloadRequestException(DownloadStatus.PAUSED, "User terminates current task");
                }
                c = (HttpsURLConnection) url.openConnection();


                c.setInstanceFollowRedirects(false);


                c.setConnectTimeout(mTimeout);


                c.setReadTimeout(mTimeout);
                addRequestHeaders(c);
                if (c instanceof HttpsURLConnection) {
                    ((HttpsURLConnection) c).setSSLSocketFactory(appContext.getSocketFactory());
                }
                int rc = c.getResponseCode();


                switch (rc) {
                    case HTTP_OK:
                    case HTTP_PARTIAL: {
                        mInfo.status = DownloadStatus.IN_PROGRESS;
                        parseOkHeaders(c);
                        transferData(c);
                        mInfo.status = DownloadStatus.COMPLETED;
                        mObserver.updateProgress(mInfo);
                        //mInfo.listener.onFinished(mInfo.id);
                        return;
                    }
                    case HTTP_FORBIDDEN: {
                        mInfo.status = DownloadStatus.RETIRED;
                        mInfo.message = "服务器还回代码: " + HTTP_FORBIDDEN + " 第三次 " + tries + " 尝试重新下载";
                        mObserver.retried(mInfo);

                        SystemClock.sleep(DEFAULT_DELAY_TIME);
                        continue;
                    }
                    case HTTP_GONE: {

                        throw new DownloadRequestException(DownloadStatus.FAILED, "The server rejected the current request");
                    }
                    case HTTP_REQUESTED_RANGE_NOT_SATISFIABLE: {

                        throw new DownloadRequestException(DownloadStatus.FAILED, "Range Not Satisfiable. It is possible that the file has been downloaded but the task is not marked as completed.");
                    }
                    default: {

                        //mInfo.listener.onStatusChanged(mTaskId, "发生未捕获错误。 状态码: " + rc);
                        throw new DownloadRequestException(DownloadStatus.RETIRED, "ResponseCode: " + rc);
                    }

                }
            } catch (DownloadRequestException e) {

                if (e.getFinalStatus() == DownloadStatus.PAUSED || e.getFinalStatus() == DownloadStatus.FAILED) {

                    throw e;
                }
            } catch (SocketTimeoutException e) {

                SystemClock.sleep(DEFAULT_DELAY_TIME);

            } catch (IOException e) {
                SystemClock.sleep(DEFAULT_DELAY_TIME);

            } finally {
                if (c != null) c.disconnect();
            }
        }

        throw new DownloadRequestException(DownloadStatus.FAILED, "Too many retries");
    }

    private void parseOkHeaders(HttpsURLConnection c) {

        String e = c.getHeaderField(HTTP_TRANSFER_ENCODING);

        if (e == null) {
            try {

                // 如果目标文件已存在
                // 累加其大小

                mInfo.bytesTotal = Long.parseLong(c.getHeaderField(HTTP_CONTENT_LENGTH));
                File file = new File(mInfo.filePath);
                if (file.exists()) {
                    mInfo.bytesTotal += file.length();
                }


            } catch (Exception error) {
                mInfo.bytesTotal = -1L;
            }
        }
        mObserver.updateStatus(mInfo);


    }

    public void stopDownload() {
        mObserver.updateStatus(mInfo);
        FileLogger.log("TAG/DownloadThread", "stopDownload");
        mIsStop.set(true);

    }

    private void transferData(HttpURLConnection c) throws DownloadRequestException {

        InputStream in = null;
        RandomAccessFile out = null;
        try {
            try {
                in = c.getInputStream();
            } catch (IOException e) {

                throw new DownloadRequestException(DownloadStatus.RETIRED, e);
            }

            try {
                out = new RandomAccessFile(mInfo.filePath, "rwd");
                if (mInfo.bytesReceived > 0)
                    out.seek(mInfo.bytesReceived);
            } catch (IOException e) {

                throw new DownloadRequestException(DownloadStatus.FAILED, e);

            }
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE >> 1];

            while (true) {
                if (mIsStop.get()) {
                    throw new DownloadRequestException(DownloadStatus.RETIRED, "Local halt requested; job probably timed out");
                }
                int len = -1;
                try {
                    len = in.read(buffer);
                } catch (IOException e) {
                    throw new DownloadRequestException(DownloadStatus.RETIRED, e);
                }
                if (len == -1) {
                    break;
                }

                try {
                    out.write(buffer, 0, len);
                    mInfo.bytesReceived += len;
                    updateProgress();
                } catch (IOException e) {

                    throw new DownloadRequestException(DownloadStatus.RETIRED, e);
                }
            }

        } catch (DownloadRequestException e) {

            throw e;
        } finally {
            if (in != null) Files.closeSilently(in);
            if (out != null) Files.closeSilently(out);
        }
    }

    private void updateProgress() {
        final long now = SystemClock.elapsedRealtime();
        final long currentBytes = mInfo.bytesReceived;
        final long sampleDelta = now - mSpeedSampleStart;
        if (sampleDelta > 500) {
            final long sampleSpeed = ((currentBytes - mSpeedSampleBytes) * 1000)
                    / sampleDelta;
            if (mSpeed == 0) {
                mSpeed = sampleSpeed;
            } else {
                mSpeed = ((mSpeed * 3) + sampleSpeed) / 4;
            }

            if (mSpeedSampleStart != 0) {
                mInfo.speed = mSpeed;
                mObserver.updateProgress(mInfo);
                //mInfo.listener.notifySpeed(mTaskId, mSpeed);
            }
            mSpeedSampleStart = now;
            mSpeedSampleBytes = currentBytes;
        }
        final long bytesDelta = currentBytes - mLastUpdateBytes;
        final long timeDelta = now - mLastUpdateTime;
        if (bytesDelta > MIN_PROGRESS_STEP && timeDelta > MIN_PROGRESS_TIME) {
            mLastUpdateBytes = currentBytes;
            mLastUpdateTime = now;
        }
    }


    @Override
    public void run() {


        mInfo.message = "下载 " + mInfo.url;
        mInfo.status = DownloadStatus.STARTED;
        mObserver.updateProgress(mInfo);
        try {


            executeDownload();


        } catch (DownloadRequestException e) {

            e.printStackTrace();

            int s = e.getFinalStatus();
            if (s == DownloadStatus.PAUSED) {
                mInfo.status = DownloadStatus.PAUSED;
                mObserver.updateProgress(mInfo);
            } else if (s == DownloadStatus.FAILED) {
                mInfo.message = e.getMessage();
                mInfo.status = DownloadStatus.FAILED;
                mObserver.updateProgress(mInfo);
            } else {
            }

            //mInfo.listener.onError(mTaskId, e.getMessage());
        }
    }
}