package us.kayla.zeppelinmusthave.content.control.fcn;

/** Latest health and output report published by one physical ship system. */
public record FlightSystemStatus(
        FlightSystemType type,
        boolean active,
        double command,
        double output,
        double resourceRatio
) {
    public FlightSystemStatus {
        command = finite(command);
        output = finite(output);
        resourceRatio = Math.clamp(finite(resourceRatio), 0.0D, 1.0D);
    }

    private static double finite(double value) {
        return Double.isFinite(value) ? value : 0.0D;
    }
}
