package us.kayla.zeppelinmusthave.content.control.fcn;

import net.minecraft.nbt.CompoundTag;

import java.util.Locale;
import java.util.Objects;

/** Human-readable network name plus one of sixteen color frequencies. */
public record FlightControlAddress(String networkName, int frequency) {
    public static final String DEFAULT_NETWORK = "primary";
    public static final FlightControlAddress DEFAULT = new FlightControlAddress(DEFAULT_NETWORK, 0);
    private static final int MAX_NAME_LENGTH = 32;

    public FlightControlAddress {
        networkName = normalizeNetworkName(networkName);
        frequency = Math.clamp(frequency, 0, 15);
    }

    public FlightControlAddress withNetworkName(String name) {
        return new FlightControlAddress(name, this.frequency);
    }

    public FlightControlAddress withFrequency(int newFrequency) {
        return new FlightControlAddress(this.networkName, newFrequency);
    }

    public void write(CompoundTag tag) {
        tag.putString("Network", this.networkName);
        tag.putInt("Frequency", this.frequency);
    }

    public static FlightControlAddress read(CompoundTag tag) {
        return new FlightControlAddress(
                tag.contains("Network") ? tag.getString("Network") : DEFAULT_NETWORK,
                tag.contains("Frequency") ? tag.getInt("Frequency") : 0
        );
    }

    public static String normalizeNetworkName(String value) {
        String source = Objects.requireNonNullElse(value, DEFAULT_NETWORK)
                .strip()
                .toLowerCase(Locale.ROOT);
        StringBuilder normalized = new StringBuilder(Math.min(source.length(), MAX_NAME_LENGTH));
        for (int index = 0; index < source.length() && normalized.length() < MAX_NAME_LENGTH; index++) {
            char character = source.charAt(index);
            if (Character.isLetterOrDigit(character)
                    || character == '_'
                    || character == '-'
                    || character == '.') {
                normalized.append(character);
            } else if (Character.isWhitespace(character)
                    && !normalized.isEmpty()
                    && normalized.charAt(normalized.length() - 1) != '-') {
                normalized.append('-');
            }
        }
        return normalized.isEmpty() ? DEFAULT_NETWORK : normalized.toString();
    }
}
