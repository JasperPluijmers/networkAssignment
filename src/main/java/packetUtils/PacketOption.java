package packetUtils;

public enum PacketOption {
    Setup((byte) 0),
    Discover((byte) 1);

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
