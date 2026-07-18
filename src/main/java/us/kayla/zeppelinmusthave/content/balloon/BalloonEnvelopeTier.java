package us.kayla.zeppelinmusthave.content.balloon;

import net.minecraft.world.item.DyeColor;

/**
 * Structural grades layered on top of Create Aeronautics' standard envelope.
 * Tier I remains the native Aeronautics envelope; this enum defines the
 * Zeppelin Must Have upgrade grades.
 */
public enum BalloonEnvelopeTier {
    REINFORCED(2, DyeColor.LIGHT_GRAY, 1.75F),
    INDUSTRIAL(3, DyeColor.GRAY, 2.50F);

    private final int level;
    private final DyeColor color;
    private final float structuralRating;

    BalloonEnvelopeTier(int level, DyeColor color, float structuralRating) {
        this.level = level;
        this.color = color;
        this.structuralRating = structuralRating;
    }

    public int level() {
        return this.level;
    }

    public DyeColor color() {
        return this.color;
    }

    /** Relative resistance to accidental damage compared with the stock canvas. */
    public float structuralRating() {
        return this.structuralRating;
    }
}
