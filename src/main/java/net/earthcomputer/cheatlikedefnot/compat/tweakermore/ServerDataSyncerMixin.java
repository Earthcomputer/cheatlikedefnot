package net.earthcomputer.cheatlikedefnot.compat.tweakermore;

import net.earthcomputer.cheatlikedefnot.CheatLikeDefnot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "me.fallenbreath.tweakermore.impl.mod_tweaks.serverDataSyncer.ServerDataSyncer", remap = false)
@Pseudo
public class ServerDataSyncerMixin {
    @Inject(method = "hasEnoughPermission", at = @At("HEAD"), cancellable = true)
    private static void allowWhenOnServer(CallbackInfoReturnable<Boolean> cir) {
        if (CheatLikeDefnot.isCheatLikeDefnotOnServer()) {
            cir.setReturnValue(true);
        }
    }
}
