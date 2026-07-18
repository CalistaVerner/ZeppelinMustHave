package us.kayla.zeppelinmusthave.content.fluid;

/**
 * Pressure grades for Zeppelin Must Have fluid trunks.
 * Tier I remains Create's native copper fluid pipe.
 */
public enum FluidPipeTier {
    REINFORCED(2, 1.50F, 384.0F),
    INDUSTRIAL(3, 2.00F, 512.0F);

    private final int level;
    private final float pressureMultiplier;
    private final float maximumEffectivePressure;

    FluidPipeTier(int level, float pressureMultiplier, float maximumEffectivePressure) {
        this.level = level;
        this.pressureMultiplier = pressureMultiplier;
        this.maximumEffectivePressure = maximumEffectivePressure;
    }

    public int level() {
        return this.level;
    }

    public float pressureMultiplier() {
        return this.pressureMultiplier;
    }

    public float maximumEffectivePressure() {
        return this.maximumEffectivePressure;
    }

    public float applyPressure(float suppliedPressure) {
        float positivePressure = Math.max(0.0F, suppliedPressure);
        return Math.min(this.maximumEffectivePressure, positivePressure * this.pressureMultiplier);
    }
}
