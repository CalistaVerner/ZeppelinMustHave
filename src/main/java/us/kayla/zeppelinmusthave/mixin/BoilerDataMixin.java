package us.kayla.zeppelinmusthave.mixin;

import com.simibubi.create.content.fluids.tank.BoilerData;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import us.kayla.zeppelinmusthave.content.burner.AirshipBurnerBlock;

/** Adds top-mounted Airship Burners to Create's native boiler consumer count. */
@Mixin(BoilerData.class)
public abstract class BoilerDataMixin {
    @Shadow
    public int attachedEngines;

    @Unique
    private int zeppelinMustHave$attachedBurners;

    @Unique
    private int zeppelinMustHave$previousAttachedBurners;

    @Inject(method = "evaluate", at = @At("HEAD"))
    private void zeppelinMustHave$prepareBurnerCount(
            FluidTankBlockEntity controller,
            CallbackInfoReturnable<Boolean> cir
    ) {
        this.attachedEngines = Math.max(
                0,
                this.attachedEngines - this.zeppelinMustHave$attachedBurners
        );
        this.zeppelinMustHave$previousAttachedBurners = this.zeppelinMustHave$attachedBurners;
        this.zeppelinMustHave$attachedBurners = 0;
    }

    @Inject(method = "evaluate", at = @At("RETURN"), cancellable = true)
    private void zeppelinMustHave$countBurners(
            FluidTankBlockEntity controller,
            CallbackInfoReturnable<Boolean> cir
    ) {
        int attachedBurners = countAttachedBurners(controller);
        this.zeppelinMustHave$attachedBurners = attachedBurners;
        this.attachedEngines += attachedBurners;
        cir.setReturnValue(
                cir.getReturnValue()
                        || attachedBurners != this.zeppelinMustHave$previousAttachedBurners
        );
    }

    @Inject(method = "clear", at = @At("TAIL"))
    private void zeppelinMustHave$clearBurnerCount(CallbackInfo ci) {
        this.zeppelinMustHave$attachedBurners = 0;
        this.zeppelinMustHave$previousAttachedBurners = 0;
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void zeppelinMustHave$writeBurnerCount(CallbackInfoReturnable<CompoundTag> cir) {
        cir.getReturnValue().putInt(
                "ZmhAttachedBurners",
                this.zeppelinMustHave$attachedBurners
        );
    }

    @Inject(method = "read", at = @At("TAIL"))
    private void zeppelinMustHave$readBurnerCount(
            CompoundTag tag,
            int boilerSize,
            CallbackInfo ci
    ) {
        this.zeppelinMustHave$attachedBurners = tag.getInt("ZmhAttachedBurners");
    }

    @Unique
    private static int countAttachedBurners(FluidTankBlockEntity controller) {
        Level level = controller.getLevel();
        if (level == null) {
            return 0;
        }

        BlockPos controllerPos = controller.getBlockPos();
        int attachedBurners = 0;
        for (int yOffset = 0; yOffset < controller.getHeight(); yOffset++) {
            for (int xOffset = 0; xOffset < controller.getWidth(); xOffset++) {
                for (int zOffset = 0; zOffset < controller.getWidth(); zOffset++) {
                    BlockPos tankPos = controllerPos.offset(xOffset, yOffset, zOffset);
                    if (!FluidTankBlock.isTank(level.getBlockState(tankPos))) {
                        continue;
                    }

                    BlockState attachedState = level.getBlockState(tankPos.above());
                    if (attachedState.getBlock() instanceof AirshipBurnerBlock) {
                        attachedBurners++;
                    }
                }
            }
        }
        return attachedBurners;
    }
}
