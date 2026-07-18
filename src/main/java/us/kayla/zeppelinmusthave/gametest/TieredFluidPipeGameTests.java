package us.kayla.zeppelinmusthave.gametest;

import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.FluidTransportBehaviour.AttachmentTypes;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.fluid.FluidPipeTier;
import us.kayla.zeppelinmusthave.content.fluid.TieredGlassFluidPipeBlock;
import us.kayla.zeppelinmusthave.content.fluid.TieredGlassFluidPipeBlockEntity;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

@GameTestHolder(ZeppelinMustHave.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TieredFluidPipeGameTests {
    private static final String TEMPLATE = "piped_redstone_empty";

    private TieredFluidPipeGameTests() {
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 20)
    public static void sameGradeStraightConnectionHasNoInternalCollar(GameTestHelper helper) {
        BlockPos first = new BlockPos(2, 2, 2);
        BlockPos second = first.east();
        BlockState firstState = ZmhBlocks.INDUSTRIAL_FLUID_PIPE.get()
                .defaultBlockState()
                .setValue(FluidPipeBlock.PROPERTY_BY_DIRECTION.get(Direction.EAST), true);
        BlockState secondState = ZmhBlocks.INDUSTRIAL_FLUID_PIPE.get()
                .defaultBlockState()
                .setValue(FluidPipeBlock.PROPERTY_BY_DIRECTION.get(Direction.WEST), true);

        helper.setBlock(first, firstState);
        helper.setBlock(second, secondState);
        helper.runAfterDelay(2, () -> {
            BlockPos absolute = helper.absolutePos(first);
            FluidTransportBehaviour behaviour = BlockEntityBehaviour.get(
                    helper.getLevel(),
                    absolute,
                    FluidTransportBehaviour.TYPE
            );
            if (behaviour == null) {
                helper.fail("Industrial fluid pipe behaviour was not created");
                return;
            }
            AttachmentTypes attachment = behaviour.getRenderedRimAttachment(
                    helper.getLevel(),
                    absolute,
                    helper.getLevel().getBlockState(absolute),
                    Direction.EAST
            );
            if (attachment != AttachmentTypes.CONNECTION) {
                helper.fail("Same-grade pipe boundary rendered " + attachment + " instead of CONNECTION");
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 20)
    public static void gradeTransitionKeepsOneVisibleCoupling(GameTestHelper helper) {
        BlockPos first = new BlockPos(2, 2, 2);
        BlockPos second = first.east();
        BlockState firstState = ZmhBlocks.REINFORCED_FLUID_PIPE.get()
                .defaultBlockState()
                .setValue(FluidPipeBlock.PROPERTY_BY_DIRECTION.get(Direction.EAST), true);
        BlockState secondState = ZmhBlocks.INDUSTRIAL_FLUID_PIPE.get()
                .defaultBlockState()
                .setValue(FluidPipeBlock.PROPERTY_BY_DIRECTION.get(Direction.WEST), true);

        helper.setBlock(first, firstState);
        helper.setBlock(second, secondState);
        helper.runAfterDelay(2, () -> {
            BlockPos absolute = helper.absolutePos(first);
            FluidTransportBehaviour behaviour = BlockEntityBehaviour.get(
                    helper.getLevel(),
                    absolute,
                    FluidTransportBehaviour.TYPE
            );
            if (behaviour == null) {
                helper.fail("Reinforced fluid pipe behaviour was not created");
                return;
            }
            AttachmentTypes attachment = behaviour.getRenderedRimAttachment(
                    helper.getLevel(),
                    absolute,
                    helper.getLevel().getBlockState(absolute),
                    Direction.EAST
            );
            if (attachment == AttachmentTypes.CONNECTION) {
                helper.fail("MK II to MK III pipe transition lost its visible coupling");
                return;
            }
            helper.succeed();
        });
    }
    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 20)
    public static void windowFormsRetainTheirPressureGrade(GameTestHelper helper) {
        BlockPos reinforcedPos = new BlockPos(2, 2, 2);
        BlockPos industrialPos = new BlockPos(4, 2, 2);

        helper.setBlock(
                reinforcedPos,
                ZmhBlocks.REINFORCED_GLASS_FLUID_PIPE.get()
                        .defaultBlockState()
                        .setValue(TieredGlassFluidPipeBlock.AXIS, Axis.X)
        );
        helper.setBlock(
                industrialPos,
                ZmhBlocks.INDUSTRIAL_GLASS_FLUID_PIPE.get()
                        .defaultBlockState()
                        .setValue(TieredGlassFluidPipeBlock.AXIS, Axis.Z)
        );

        helper.runAfterDelay(2, () -> {
            TieredGlassFluidPipeBlockEntity reinforced = helper.getBlockEntity(reinforcedPos);
            TieredGlassFluidPipeBlockEntity industrial = helper.getBlockEntity(industrialPos);
            if (reinforced.tier() != FluidPipeTier.REINFORCED) {
                helper.fail("Reinforced pipe window lost its MK II pressure grade");
                return;
            }
            if (industrial.tier() != FluidPipeTier.INDUSTRIAL) {
                helper.fail("Industrial pipe window lost its MK III pressure grade");
                return;
            }
            helper.succeed();
        });
    }

}
