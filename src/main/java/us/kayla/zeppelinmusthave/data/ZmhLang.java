package us.kayla.zeppelinmusthave.data;

import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.lang.LangBuilder;
import net.createmod.catnip.lang.LangNumberFormat;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;

import java.util.List;

public final class ZmhLang {
    private ZmhLang() {
    }

    public static LangBuilder builder() {
        return Lang.builder(ZeppelinMustHave.MOD_ID);
    }

    public static LangBuilder translate(String key, Object... arguments) {
        return builder().translate(key, arguments);
    }

    public static LangBuilder number(double value) {
        return builder().text(LangNumberFormat.format(value));
    }

    public static LangBuilder blockName(BlockState state) {
        return builder().add(state.getBlock().getName());
    }

    public static void emptyLine(List<Component> tooltip) {
        builder().text("").forGoggles(tooltip);
    }
}
