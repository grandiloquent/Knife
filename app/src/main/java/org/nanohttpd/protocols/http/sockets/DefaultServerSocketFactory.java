package org.nanohttpd.protocols.http.sockets;

import org.nanohttpd.util.IFactoryThrowing;

import java.io.IOException;
import java.net.ServerSocket;
/**
 * Creates a normal ServerSocket for TCP connections
 */
public class DefaultServerSocketFactory implements IFactoryThrowing<ServerSocket, IOException> {
    @Override
    public ServerSocket create() throws IOException {
        return new ServerSocket();
    }
}
