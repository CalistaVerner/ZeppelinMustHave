package us.kayla.zeppelinmusthave.content.burner;

/**
 * Semantic heat quality understood by the airship burner reservoir.
 * Numerical output multipliers remain in the data-pack burner profile.
 */
public enum AirshipHeatGrade {
    NONE(
            false,
            "message.zeppelin_must_have.burner.fuel.none",
            "goggles.burner.grade.none"
    ),
    REGULAR(
            false,
            "message.zeppelin_must_have.burner.fuel.normal",
            "goggles.burner.grade.normal"
    ),
    SUPERHEATED(
            true,
            "message.zeppelin_must_have.burner.fuel.superheated",
            "goggles.burner.grade.superheated"
    );

    private final boolean superheated;
    private final String statusTranslationKey;
    private final String goggleTranslationKey;

    AirshipHeatGrade(
            boolean superheated,
            String statusTranslationKey,
            String goggleTranslationKey
    ) {
        this.superheated = superheated;
        this.statusTranslationKey = statusTranslationKey;
        this.goggleTranslationKey = goggleTranslationKey;
    }

    public boolean isSuperheated() {
        return this.superheated;
    }

    public String statusTranslationKey() {
        return this.statusTranslationKey;
    }

    public String goggleTranslationKey() {
        return this.goggleTranslationKey;
    }
}
