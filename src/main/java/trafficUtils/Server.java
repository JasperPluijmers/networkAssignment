package trafficUtils;

import java.net.InetAddress;

public class Server {



    private final String name;
    private final InetAddress address;
    private final int port;

    public Server(String name, InetAddress address, int port) {
        this.name = name;
        this.address = address;
        this.port = port;
    }


    public String getName() {
        return name;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Server)) {
            return false;
        }

        Server otherServer = (Server) o;

        return (otherServer.getName().equals(name))
                && (otherServer.getPort() == port)
                && (otherServer.getAddress().equals(address));

    }

    @Override
    public String toString() {
        return name + " on: " + address + ':' + port;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

}
