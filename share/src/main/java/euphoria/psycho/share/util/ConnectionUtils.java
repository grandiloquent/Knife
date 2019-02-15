package euphoria.psycho.share.util;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class ConnectionUtils {
    public static int FTP_SERVER_PORT = 2211;

    public static int getAvailablePortForFTP() {
        int port = 0;
        for (int i = FTP_SERVER_PORT; i < 65000; i++) {
            if (isPortAvailable(i)) {
                port = i;
                break;
            }
        }
        return port;
    }

    public static boolean isPortAvailable(int port) {

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }

}
