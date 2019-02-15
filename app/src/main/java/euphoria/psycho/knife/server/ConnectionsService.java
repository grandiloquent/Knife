package euphoria.psycho.knife.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.listener.ListenerFactory;

import androidx.annotation.Nullable;
import euphoria.psycho.share.util.ConnectionUtils;

public class ConnectionsService extends Service {
    private FtpServer mFtpServer;

    public FtpServer getFtpServer() {
        return mFtpServer;
    }

    public boolean launchServer() {
        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(ConnectionUtils.getAvailablePortForFTP());

        FtpServerFactory serverFactory = new FtpServerFactory();
        serverFactory.addListener("default", listenerFactory.createListener());

        ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory();
        connectionConfigFactory.setAnonymousLoginEnabled(true);
        connectionConfigFactory.setMaxLoginFailures(5);
        connectionConfigFactory.setLoginFailureDelay(2000);
        serverFactory.setConnectionConfig(connectionConfigFactory.createConnectionConfig());

//        BaseUser user = new BaseUser();
//        user.setName(getNetworkConnection().getUserName());
//        user.setPassword(getNetworkConnection().getPassword());
//        user.setHomeDirectory(getNetworkConnection().getPath());
//
//        List<Authority> list = new ArrayList<>();
//        list.add(new WritePermission());
//        list.add(new TransferRatePermission(0, 0));
//        list.add(new ConcurrentLoginPermission(10, 10));
//        user.setAuthorities(list);

//        try {
//            serverFactory.getUserManager().save(user);
//        } catch (FtpException e) {
//        }

        // do start server
        try {
            mFtpServer = serverFactory.createServer();
            mFtpServer.start();
            return true;
        } catch (Exception e) {
            mFtpServer = null;
        }
        return false;
    }

    public void stopServer() {
        mFtpServer.stop();
        mFtpServer = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
