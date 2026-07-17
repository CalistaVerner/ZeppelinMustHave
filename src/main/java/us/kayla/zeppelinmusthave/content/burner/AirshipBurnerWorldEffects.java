package us.kayla.zeppelinmusthave.content.burner;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

/** Centralizes world publication, blockstate lighting, and burner sounds. */
final class AirshipBurnerWorldEffects {
    private AirshipBurnerWorldEffects() {
    }

    static void publish(AirshipBurnerBlockEntity burner, boolean synchronizeClient) {
        updateLitState(burner);
        burner.setChanged();
        if (synchronizeClient) {
            burner.sendData();
        }
        if (burner.getLevel() != null) {
            burner.getLevel().updateNeighborsAt(
                    burner.getBlockPos(),
                    burner.getBlockState().getBlock()
            );
        }
    }

    static void updateLitState(AirshipBurnerBlockEntity burner) {
        if (burner.getLevel() == null || burner.getLevel().isClientSide) {
            return;
        }

        BlockState state = burner.getBlockState();
        if (!state.hasProperty(AirshipBurnerBlock.LIT)) {
            return;
        }
        boolean lit = burner.canOutputGas();
        if (state.getValue(AirshipBurnerBlock.LIT) != lit) {
            burner.getLevel().setBlock(
                    burner.getBlockPos(),
                    state.setValue(AirshipBurnerBlock.LIT, lit),
                    3
            );
        }
    }

    static void playFuelInserted(AirshipBurnerBlockEntity burner, AirshipHeatSource source) {
        if (burner.getLevel() == null) {
            return;
        }
        boolean superheated = source.grade().isSuperheated();
        burner.getLevel().playSound(
                null,
                burner.getBlockPos(),
                superheated ? SoundEvents.BLAZE_SHOOT : SoundEvents.FIRECHARGE_USE,
                SoundSource.BLOCKS,
                0.35F,
                superheated ? 1.15F : 0.9F
        );
    }

    static void playDepleted(AirshipBurnerBlockEntity burner) {
        if (burner.getLevel() == null) {
            return;
        }
        burner.getLevel().playSound(
                null,
                burner.getBlockPos(),
                SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS,
                0.35F,
                0.8F
        );
    }
}
