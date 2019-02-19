package euphoria.psycho.knife.server;

import org.apache.commons.io.IOUtils;
import org.nanohttpd.protocols.http.IHTTPSession;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import euphoria.psycho.share.util.FileUtils;
import euphoria.psycho.share.util.MimeUtils;

/*
https://referencesource.microsoft.com/#System.Web/StaticFileHandler.cs
https://referencesource.microsoft.com/#System/net/System/Net/Internal.cs
https://referencesource.microsoft.com/#System.Web/WorkerRequest.cs
 */

class ServerUtils {
    public static final String HTTP_ACCEPT_RANGES = "Accept-Ranges";
    /*
    https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Origin

    The Access-Control-Allow-Origin response header indicates
    whether the response can be shared with requesting code from the given origin.

    *

    For requests without credentials,
    the literal value "*" can be specified,
    as a wildcard;
    the value tells browsers to allow requesting code
    from any origin to access the resource.
    Attempting to use the wildcard with credentials will result in an error.

     */
    public static final String HTTP_ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String HTTP_ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
    /*
    https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cache-Control

    The Cache-Control general-header field is used to specify directives for caching
    mechanisms in both requests and responses. Caching directives are unidirectional,
    meaning that a given directive in a request is not implying that the same directive
    is to be given in the response.

    max-age=<seconds> Specifies the maximum amount of time a resource will be considered
    fresh. Contrary to Expires, this directive is relative to the time of the request.

     */
    public static final String HTTP_CACHE_CONTROL = "Cache-Control";
    public static final String HTTP_CONNECTION = "Connection";
    public static final String HTTP_CONTENT_LENGTH = "Content-Length";
    public static final String HTTP_CONTENT_LOCATION = "Content-Location";
    /*
    https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Range
    Content-Range: <unit> <range-start>-<range-end>/<size>
Content-Range: <unit> <range-start>-<range-end>/*
Content-Range: <unit> *\/<size>
     */
    public static final String HTTP_CONTENT_RANGE = "Content-Range";
    public static final String HTTP_CONTENT_TYPE = "Content-Type";
    public static final String HTTP_ETAG = "ETag";
    public static final String HTTP_EXPIRES = "Expires";
    public static final String HTTP_LAST_MODIFIED = "Last-Modified";
    public static final String HTTP_SERVER = "Server";
    public static final String HTTP_X_POWERED_BY = "X-Powered-By";
    /*
    The Range HTTP request header indicates the part of a document that the server should
return. Several parts can be requested with one Range header at once, and the server
may send back these ranges in a multipart document. If the server sends back ranges,
it uses the 206 Partial Content for the response. If the ranges are invalid, the
server returns the 416 Range Not Satisfiable error. The server can also ignore the
Range header and return the whole document with a 200 status code.

Range: <unit>=<range-start>-
Range: <unit>=<range-start>-<range-end>
Range: <unit>=<range-start>-<range-end>, <range-start>-<range-end>
Range: <unit>=<range-start>-<range-end>, <range-start>-<range-end>, <range-start>-<range-end>

     */
    public static final String HTTP_RANGE = "Range";
    public static final String HTTP_CONTENT_DISPOSITION = "Content-Disposition";

    public static void copyToFile(InputStream is, File dstFile) {


        try (FileOutputStream os = new FileOutputStream(dstFile)) {
            IOUtils.copy(is, os);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getFileNameFromContentDisposition(String value) {
        int index = value.indexOf("filename=\"");
        if (index == -1) return null;
        int length = "filename=\"".length();
        return value.substring(index + length, value.length() - 1);


    }

    private static final SimpleDateFormat mGMTFormat = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);


    private static void dumpParameters(IHTTPSession session) {
        Map<String, List<String>> parameters = session.getParameters();

        Iterator<String> keyIterator = parameters.keySet().iterator();
        while (keyIterator.hasNext()) {
            List<String> values = parameters.get(keyIterator.next());
            for (String value : values) {
                System.out.println(value);
            }
        }
    }

    public static long[] parseRange(Map<String, String> headers) {
        String value = headers.get("range");
        long[] values = new long[2];
        if (value == null) {
            return values;
        }


        int index = value.indexOf("bytes=");
        if (index == -1) return values;
        value = value.substring(index + 6);
        index = value.lastIndexOf('-');
        if (index != -1) {
            values[0] = Long.parseLong(value.substring(0, index));
        } else {
            values[0] = Long.parseLong(value.substring(0, index));
            values[1] = Long.parseLong(value.substring(index + 1));

        }


        return values;
    }

    public static void dumpReuqest(IHTTPSession session) {
        Map<String, String> header = session.getHeaders();
        Map<String, String> parms = session.getParms();
        String uri = session.getUri();


        System.out.println(session.getMethod() + " '" + uri + "' ");

        Iterator<String> e = header.keySet().iterator();
        while (e.hasNext()) {
            String value = e.next();
            System.out.println("  HDR: '" + value + "' = '" + header.get(value) + "'");
        }
        e = parms.keySet().iterator();
        while (e.hasNext()) {
            String value = e.next();
            System.out.println("  PRM: '" + value + "' = '" + parms.get(value) + "'");
        }

    }

    public static String generateETag(File file) {

        long lastModFileTime = file.lastModified();
        long nowFileTime = new Date().getTime();

        String hexFileTime = Long.toHexString(lastModFileTime);
        if ((nowFileTime - lastModFileTime) <= 30000000) {
            return "W/\"" + hexFileTime + "\"";
        }
        return "\"" + hexFileTime + "\"";
    }

    public static String getGMTDateTime(int seconds) {
        long millis = new Date().getTime() + (seconds * 1000);

        return mGMTFormat.format(millis);
    }

    public static String getGMTDateTime(long millis) {

        return mGMTFormat.format(millis);
    }

    public static int getIntFromMap(Map<String, List<String>> map, String key, int defaultValue) {
        String value = getStringFromMap(map, key, null);
        if (value == null) return defaultValue;

        for (int i = 0, len = value.length(); i < len; i++) {
            if (!Character.isDigit(value.charAt(i))) return defaultValue;
        }
        return Integer.parseInt(value);

    }

    /**
     * Returns an <code>InetAddress</code> object encapsulating what is most likely the machine's LAN IP address.
     * <p/>
     * This method is intended for use as a replacement of JDK method <code>InetAddress.getLocalHost</code>, because
     * that method is ambiguous on Linux systems. Linux systems enumerate the loopback network interface the same
     * way as regular LAN network interfaces, but the JDK <code>InetAddress.getLocalHost</code> method does not
     * specify the algorithm used to select the address returned under such circumstances, and will often return the
     * loopback address, which is not valid for network communication. Details
     * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037">here</a>.
     * <p/>
     * This method will scan all IP addresses on all network interfaces on the host machine to determine the IP address
     * most likely to be the machine's LAN address. If the machine has multiple IP addresses, this method will prefer
     * a site-local IP address (e.g. 192.168.x.x or 10.10.x.x, usually IPv4) if the machine has one (and will return the
     * first site-local address if the machine has more than one), but if the machine does not hold a site-local
     * address, this method will return simply the first non-loopback address found (IPv4 or IPv6).
     * <p/>
     * If this method cannot find a non-loopback address using this selection algorithm, it will fall back to
     * calling and returning the result of JDK method <code>InetAddress.getLocalHost</code>.
     * <p/>
     *
     * @throws UnknownHostException If the LAN address of the machine cannot be found.
     */
    public static InetAddress getLocalHostLANAddress() throws UnknownHostException {
        try {
            InetAddress candidateAddress = null;
            // Iterate all NICs (network interface cards)...
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // Iterate all IP addresses assigned to each card...
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {

                        if (inetAddr.isSiteLocalAddress()) {
                            // Found non-loopback site-local address. Return it immediately...
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            // Found non-loopback address, but not necessarily site-local.
                            // Store it as a candidate to be returned if site-local address is not subsequently found...
                            candidateAddress = inetAddr;
                            // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
                            // only the first. For subsequent iterations, candidate will be non-null.
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                // We did not find a site-local address, but we found some other non-loopback address.
                // Server might have a non-site-local address assigned to its NIC (or it might be running
                // IPv6 which deprecates the "site-local" concept).
                // Return this non-loopback candidate address...
                return candidateAddress;
            }
            // At this point, we did not find a non-loopback address.
            // Fall back to returning whatever InetAddress.getLocalHost() returns...
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress;
        } catch (Exception e) {
            UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address: " + e);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }

    public static String getLocalIp() {
        try {
            return getLocalHostLANAddress().getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public static String getMimeType(String name, String defType) {
        String ext = FileUtils.getExtension(name);
        if (ext != null) return MimeUtils.guessMimeTypeFromExtension(ext);
        return defType;
    }

    public static String getStringFromMap(Map<String, List<String>> map, String key, String defaultValue) {
        if (map.containsKey(key)) {
            List<String> values = map.get(key);
            if (values == null || values.isEmpty()) return defaultValue;
            return values.get(0);
        }
        return defaultValue;
    }
}
