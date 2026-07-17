package us.kayla.zeppelinmusthave.content.steam;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.simibubi.create.content.kinetics.steamEngine.SteamJetParticleData;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
final class SteamEngineGradeClientEffects {
    private SteamEngineGradeClientEffects() {
    }

    static @Nullable Float targetAngle(SteamEngineGradeBlockEntity engine) {
        BlockState blockState = engine.getBlockState();
        if (!(blockState.getBlock() instanceof SteamEngineGradeBlock)) {
            return null;
        }

        Direction facing = SteamEngineBlock.getFacing(blockState);
        PoweredShaftBlockEntity shaft = engine.getShaft();
        if (shaft == null) {
            return null;
        }

        Axis facingAxis = facing.getAxis();
        Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);
        float angle = KineticBlockEntityRenderer.getAngleForBe(shaft, shaft.getBlockPos(), axis);
        if (axis == facingAxis) {
            return null;
        }
        if (axis.isHorizontal()
                && (facingAxis == Axis.X ^ facing.getAxisDirection() == AxisDirection.POSITIVE)) {
            angle *= -1.0F;
        }
        if (axis == Axis.X && facing == Direction.DOWN) {
            angle *= -1.0F;
        }
        return angle;
    }

    static void spawnParticles(
            SteamEngineGradeBlockEntity engine,
            PoweredShaftBlockEntity shaft,
            float efficiency,
            SteamEngineGradeProfile profile
    ) {
        if (engine.getLevel() == null || efficiency <= 0.0F || shaft.getSpeed() == 0.0F) {
            return;
        }

        float speedRatio = Mth.clamp(Math.abs(shaft.getSpeed()) / 64.0F, 0.0F, 1.0F);
        float chance = 0.035F
                * profile.steamParticleScale()
                * profile.cylinderCount()
                * efficiency
                * speedRatio;
        if (engine.getLevel().random.nextFloat() >= chance) {
            return;
        }

        Direction facing = SteamEngineBlock.getFacing(engine.getBlockState());
        Vec3 offset = VecHelper.rotate(
                new Vec3(0.0D, 0.0D, 1.0D)
                        .add(VecHelper.offsetRandomly(Vec3.ZERO, engine.getLevel().random, 1.0F)
                                .multiply(1.0D, 1.0D, 0.0D)
                                .normalize()
                                .scale(0.5D)),
                AngleHelper.verticalAngle(facing),
                Axis.X
        );
        offset = VecHelper.rotate(offset, AngleHelper.horizontalAngle(facing), Axis.Y);
        Vec3 position = offset.scale(0.5D).add(Vec3.atCenterOf(engine.getBlockPos()));
        Vec3 motion = offset.subtract(Vec3.atLowerCornerOf(facing.getNormal()).scale(0.75D));
        int particleStrength = Math.max(1, Math.round(profile.steamParticleScale()));
        engine.getLevel().addParticle(
                new SteamJetParticleData(particleStrength),
                position.x,
                position.y,
                position.z,
                motion.x,
                motion.y,
                motion.z
        );
    }
}
