package us.kayla.zeppelinmusthave.content.steam;

import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeBlock;

/** Central validity policy for single-block and multi-block graded steam engines. */
final class SteamEngineAssemblyValidator {
    private SteamEngineAssemblyValidator() {
    }

    static boolean isValid(Level level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof LeviathanSteamEngineBlock leviathan) {
            return leviathan.isAssemblyComplete(level, pos, state);
        }
        if (state.getBlock() instanceof MkViiSteamEngineBlock mkVii) {
            return mkVii.isAssemblyComplete(level, pos, state);
        }
        Direction direction = SteamEngineBlock.getConnectedDirection(state).getOpposite();
        return level.getBlockState(pos.relative(direction)).getBlock() instanceof BoilerGradeBlock;
    }
}
