package net.earthcomputer.cheatlikedefnot.compat.mixin.clientcommands;

import net.earthcomputer.cheatlikedefnot.CheatLikeDefnotClient;
import net.earthcomputer.cheatlikedefnot.Rules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "net.earthcomputer.clientcommands.command.FindItemCommand", remap = false)
@Pseudo
public class FindItemCommandMixin {
    @ModifyArg(method = "makeFindItemsTask", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasPermissionLevel(I)Z", remap = true))
    private static int modifyFindItemsTaskPermissionLevel(int oldPermissionLevel) {
        if (CheatLikeDefnotClient.isCheatLikeDefnotOnServer() && Rules.nonOpBlockNbtQueries && Rules.nonOpEntityNbtQueries) {
            return 0;
        } else {
            return oldPermissionLevel;
        }
    }
}
