package packetUtils;

public enum Request {
    files ((byte) 0),
    down ((byte) 1),
    up((byte) 2);


    private final byte value;

    Request(byte value) {
        this.value = value;
    }
}
