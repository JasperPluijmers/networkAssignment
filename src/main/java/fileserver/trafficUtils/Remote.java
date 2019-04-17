package fileserver.trafficUtils;

import java.net.InetAddress;

/**
 * The Remote class represents a remote fileserver.server.
 */
public class Remote {

    private final String name;
    private final InetAddress address;
    private final int port;

    public Remote(String name, InetAddress address, int port) {
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

        if (!(o instanceof Remote)) {
            return false;
        }

        Remote otherRemote = (Remote) o;

        return (otherRemote.getName().equals(name))
                && (otherRemote.getPort() == port)
                && (otherRemote.getAddress().equals(address));
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
