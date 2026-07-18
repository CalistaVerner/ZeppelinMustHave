package us.kayla.zeppelinmusthave.content.control.fcn;

/** Existing physical blocks implement this to accept a nearby FCN receiver. */
public interface FlightControlActuator {
    void applyFlightControl(FlightControlChannel channel, int value, boolean emergencyLatched);

    void clearFlightControl(FlightControlChannel channel);
}
