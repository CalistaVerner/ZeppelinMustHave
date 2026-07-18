package us.kayla.zeppelinmusthave.content.control.fcn;

/** Deterministic arbitration priority for competing command sources. */
public enum FlightControlAuthority {
    AUTOMATIC(0),
    MANUAL(1),
    SAFETY(2);

    private final int priority;

    FlightControlAuthority(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return this.priority;
    }
}
