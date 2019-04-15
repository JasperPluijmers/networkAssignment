package packetUtils;

public enum PacketOption {
    Discover((byte) 0),
    Discovered((byte) 1),
    Setup((byte) 2),
    Accept((byte) 3),
    Request((byte) 4),
    Acknowledge((byte) 5),
    Files ((byte) 6),
    Data((byte) 7),
    EndOfFile((byte) 8),
    BeginOfFile((byte) 9),
    Error((byte) 10),
    Close((byte) 10);

    private final byte value;

    PacketOption(byte value) {
        this.value = value;
    }

    public static PacketOption fromValue(byte value) {
        for (PacketOption packetOption : PacketOption.values()) {
            if (packetOption.value == value) {
                return packetOption;
            }
        }
        throw new IllegalArgumentException("No field in enum found corresponding to value: " +
                "" + value);
    }

    public byte getValue() {
        return this.value;
    }

}
