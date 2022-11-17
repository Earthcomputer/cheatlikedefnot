package net.earthcomputer.cheatlikedefnot.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow @Final protected ServerPlayerEntity player;

    @Inject(method = "setGameMode", at = @At("RETURN"))
    protected void onSetGameMode(CallbackInfo ci) {
        if (this.player.networkHandler != null) { // can be null on server startup
            this.player.server.getPlayerManager().sendCommandTree(this.player);
        }
    }
}
