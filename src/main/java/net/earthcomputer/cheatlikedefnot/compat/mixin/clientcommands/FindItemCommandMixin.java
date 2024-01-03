package net.earthcomputer.cheatlikedefnot.compat.mixin.clientcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import net.earthcomputer.cheatlikedefnot.CheatLikeDefnotClient;
import net.earthcomputer.cheatlikedefnot.Rules;
import net.earthcomputer.cheatlikedefnot.compat.clientcommands.CLDFindItemTask;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(targets = "net.earthcomputer.clientcommands.command.FindItemCommand", remap = false)
@Pseudo
public class FindItemCommandMixin {
    @Inject(method = "findItem", at = @At("HEAD"), cancellable = true)
    private static void onFindItem(CommandContext<FabricClientCommandSource> ctx, boolean noSearchShulkerBox, boolean keepSearching, Pair<String, Predicate<ItemStack>> item, CallbackInfoReturnable<Integer> cir) {
        if (!CheatLikeDefnotClient.isCheatLikeDefnotOnServer()) {
            return;
        }
        if (!Rules.nonOpBlockNbtQueries || !Rules.nonOpEntityNbtQueries) {
            return;
        }

        try {
            Class<?> longTaskClass = Class.forName("net.earthcomputer.clientcommands.task.LongTask");
            String taskName = (String) Class.forName("net.earthcomputer.clientcommands.task.TaskManager").getMethod("addTask", String.class, longTaskClass).invoke(null, "cfinditem", new CLDFindItemTask(item.getLeft(), item.getRight(), !noSearchShulkerBox, keepSearching));

            if (keepSearching) {
                ctx.getSource().sendFeedback(new TranslatableText("commands.cfinditem.starting.keepSearching", item.getLeft())
                    .append(" ")
                    .append((Text) Class.forName("net.earthcomputer.clientcommands.command.ClientCommandHelper").getMethod("getCommandTextComponent", String.class, String.class).invoke(null, "commands.client.cancel", "/ctask stop " + taskName)));
            } else {
                ctx.getSource().sendFeedback(new TranslatableText("commands.cfinditem.starting", item.getLeft()));
            }
        } catch (ReflectiveOperationException e) {
            LogUtils.getLogger().error("Failed to run cheatlikedefnot cfinditem command", e);
            ctx.getSource().sendFeedback(new TranslatableText("cheatlikedefnot.cfinditem.failed").styled(style -> style.withColor(Formatting.YELLOW)));
            return;
        }

        cir.setReturnValue(Command.SINGLE_SUCCESS);
    }
}
