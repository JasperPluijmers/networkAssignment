package packetUtils;

public enum PacketOption {
    Discover((byte) 0),
    Discovered((byte) 1),
    Setup((byte) 2),
    Accept((byte) 3);

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
