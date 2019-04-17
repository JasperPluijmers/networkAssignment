package fileserver.utils.packetUtils;

/**
 * The different requests of the protocol
 */
public enum Request {
    files ((byte) 0),
    down ((byte) 1);


    private final byte value;

    Request(byte value) {
        this.value = value;
    }
}
