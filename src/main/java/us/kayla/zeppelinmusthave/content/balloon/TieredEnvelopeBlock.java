package us.kayla.zeppelinmusthave.content.balloon;

import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import dev.eriksonn.aeronautics.content.blocks.hot_air.envelope.Envelope;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Airtight structural envelope segment compatible with the native Aeronautics
 * balloon graph.
 *
 * <p>Every segment records which orthogonal neighbours belong to the same
 * envelope grade. Client models use those six connections to remove repeated
 * block frames and draw structural ribs only around the outer perimeter of the
 * assembled balloon skin.</p>
 */
public final class TieredEnvelopeBlock extends Block implements Envelope, SpecialBlockItemRequirement {
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    private final BalloonEnvelopeTier tier;

    public TieredEnvelopeBlock(Properties properties, BalloonEnvelopeTier tier) {
        super(properties);
        this.tier = tier;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    public BalloonEnvelopeTier tier() {
        return this.tier;
    }

    @Override
    public DyeColor getColor() {
        return this.tier.color();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState state = this.defaultBlockState();

        for (Direction direction : Direction.values()) {
            state = state.setValue(
                    connectionProperty(direction),
                    this.connectsTo(level.getBlockState(pos.relative(direction)))
            );
        }
        return state;
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighbourState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighbourPos
    ) {
        return state.setValue(connectionProperty(direction), this.connectsTo(neighbourState));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        BlockState rotated = state;
        for (Direction direction : Direction.values()) {
            rotated = rotated.setValue(
                    connectionProperty(rotation.rotate(direction)),
                    state.getValue(connectionProperty(direction))
            );
        }
        return rotated;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        BlockState mirrored = state;
        for (Direction direction : Direction.values()) {
            mirrored = mirrored.setValue(
                    connectionProperty(mirror.mirror(direction)),
                    state.getValue(connectionProperty(direction))
            );
        }
        return mirrored;
    }

    public static BooleanProperty connectionProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    private boolean connectsTo(BlockState neighbourState) {
        return neighbourState.is(this);
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 1;
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (entity.isSuppressingBounce()) {
            super.fallOn(level, state, pos, entity, fallDistance);
        } else {
            entity.causeFallDamage(fallDistance, 0.0F, level.damageSources().fall());
        }
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        if (entity.isSuppressingBounce()) {
            super.updateEntityAfterFallOn(level, entity);
            return;
        }

        Vec3 movement = entity.getDeltaMovement();
        if (movement.y < 0.0D) {
            double scale = 0.65D * (entity instanceof LivingEntity ? 1.0D : 0.8D);
            entity.setDeltaMovement(movement.x, -movement.y * scale, movement.z);
        }
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, @Nullable BlockEntity blockEntity) {
        return new ItemRequirement(
                ItemRequirement.ItemUseType.CONSUME,
                new ItemStack(this)
        );
    }
}
