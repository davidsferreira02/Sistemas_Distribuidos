package ds.assign.chat;

public class PeerConnection {
    private String host;
    private int port;

    public PeerConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
