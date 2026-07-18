package us.kayla.zeppelinmusthave.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.BasePropellerBlock;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import us.kayla.zeppelinmusthave.content.ballast.BallastTankBlockEntity;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

final class ZmhServicePonderScenes {
    private ZmhServicePonderScenes() {
    }

    static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        var items = ZmhPonderRegistration.items(helper);
        items.forComponents(ZmhBlocks.BALLAST_TANK_ITEM.get())
                .addStoryBoard(
                        "service/ballast_tank",
                        ZmhServicePonderScenes::ballastTank,
                        ZmhPonderTags.ZEPPELIN_SYSTEMS
                );
        items.forComponents(ZmhBlocks.MOORING_WINCH_ITEM.get())
                .addStoryBoard(
                        "service/mooring_winch",
                        ZmhServicePonderScenes::mooringWinch,
                        ZmhPonderTags.ZEPPELIN_SYSTEMS
                );
        items.forComponents(ZmhBlocks.VERTICAL_THRUSTER_ITEM.get())
                .addStoryBoard(
                        "service/vertical_thruster",
                        ZmhServicePonderScenes::verticalThruster,
                        ZmhPonderTags.ZEPPELIN_SYSTEMS
                );
    }

    private static void ballastTank(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos tankPos = util.grid().at(2, 1, 2);

        scene.title("ballast_tank", "Controlling Airship Mass with Ballast");
        scene.configureBasePlate(0, 0, 5);
        scene.world().setBlock(tankPos, ZmhBlocks.BALLAST_TANK.get().defaultBlockState(), false);
        scene.idle(1);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.world().showSection(util.select().position(tankPos), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(80)
                .text("The Ballast Tank accepts water through buckets, pipes, and any NeoForge fluid handler")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(tankPos))
                .placeNearTarget();
        scene.overlay().showControls(
                        util.vector().blockSurface(tankPos, Direction.UP),
                        Pointing.DOWN,
                        40
                )
                .withItem(new ItemStack(Items.WATER_BUCKET))
                .rightClick();
        scene.idle(95);

        scene.world().modifyBlockEntity(
                tankPos,
                BallastTankBlockEntity.class,
                tank -> tank.fluidHandler().fill(
                        new FluidStack(Fluids.WATER, 5_000),
                        IFluidHandler.FluidAction.EXECUTE
                )
        );
        scene.effects().indicateSuccess(tankPos);
        scene.overlay().showText(90)
                .text("Stored water is registered as real dynamic mass in the containing Sable sub-level")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(Vec3.atCenterOf(tankPos))
                .placeNearTarget();
        scene.idle(105);

        scene.overlay().showText(80)
                .text("Moving ballast changes both total vessel mass and its center of mass; a comparator reports the fill level")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(Vec3.atCenterOf(tankPos))
                .placeNearTarget();
        scene.idle(95);
    }

    private static void mooringWinch(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos winchPos = util.grid().at(2, 2, 2);
        BlockPos shaftPos = util.grid().at(3, 2, 2);

        scene.title("mooring_winch", "Mooring a Zeppelin with a Physical Rope");
        scene.configureBasePlate(0, 0, 5);
        scene.world().setBlock(
                winchPos,
                ZmhBlocks.MOORING_WINCH.get()
                        .defaultBlockState()
                        .setValue(DirectionalKineticBlock.FACING, Direction.DOWN)
                        .setValue(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE, false),
                false
        );
        scene.world().setBlock(
                shaftPos,
                AllBlocks.SHAFT.getDefaultState().setValue(ShaftBlock.AXIS, Direction.Axis.X),
                false
        );
        scene.idle(1);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.world().showSection(util.select().fromTo(winchPos, shaftPos), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(85)
                .text("The Mooring Winch is a specialized Create Simulated Rope Winch, not a decorative cable block")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(winchPos))
                .placeNearTarget();
        scene.idle(100);

        scene.effects().indicateSuccess(shaftPos);
        scene.overlay().showText(85)
                .text("Kinetic rotation extends or retracts the rope through the native winch behavior")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(Vec3.atCenterOf(shaftPos))
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(90)
                .text("The deployed line uses the Simulated rope strand, attachment, tension, break force, and physics constraints")
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(Vec3.atCenterOf(winchPos).add(0.0D, -1.0D, 0.0D))
                .placeNearTarget();
        scene.idle(105);
    }

    private static void verticalThruster(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos shaftPos = util.grid().at(2, 1, 2);
        BlockPos thrusterPos = util.grid().at(2, 2, 2);

        scene.title("vertical_thruster", "Generating Vertical Propulsion");
        scene.configureBasePlate(0, 0, 5);
        scene.world().setBlock(
                shaftPos,
                AllBlocks.SHAFT.getDefaultState().setValue(ShaftBlock.AXIS, Direction.Axis.Y),
                false
        );
        scene.world().setBlock(
                thrusterPos,
                ZmhBlocks.VERTICAL_THRUSTER.get()
                        .defaultBlockState()
                        .setValue(BasePropellerBlock.FACING, Direction.UP)
                        .setValue(BasePropellerBlock.REVERSED, false),
                false
        );
        scene.idle(1);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.world().showSection(util.select().fromTo(shaftPos, thrusterPos), Direction.DOWN);
        scene.idle(20);

        scene.effects().indicateSuccess(shaftPos);
        scene.overlay().showText(85)
                .text("Create kinetic speed drives the Vertical Thruster through its shaft connection")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(Vec3.atCenterOf(shaftPos))
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(90)
                .text("Aeronautics exposes the propeller and Sable applies the resulting force at the block position on every physics tick")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(Vec3.atCenterOf(thrusterPos))
                .placeNearTarget();
        scene.idle(105);

        scene.world().modifyBlock(
                thrusterPos,
                state -> state.setValue(BasePropellerBlock.REVERSED, true),
                false
        );
        scene.effects().indicateRedstone(thrusterPos);
        scene.overlay().showText(80)
                .text("Use the Create Wrench on the fan to reverse thrust or flip the unit between upward and downward operation")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(thrusterPos))
                .placeNearTarget();
        scene.idle(95);
    }
}
