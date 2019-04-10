package packetUtils;

public enum Request {
    files ((byte) 0),
    down ((byte) 1),
    up((byte) 2);


    private final byte value;

    Request(byte value) {
        this.value = value;
    }

    public static Request fromValue(byte value) {
        for (Request request : Request.values()) {
            if (request.value == value) {
                return request;
            }
        }
        throw new IllegalArgumentException("No field in enum found corresponding to value: " +
                "" + value);
    }

    public byte getValue() {
        return this.value;
    }
}
