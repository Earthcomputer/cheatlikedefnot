package net.earthcomputer.cheatlikedefnot.mixin;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @ModifyArg(method = {"onQueryEntityNbt", "onQueryBlockNbt"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;hasPermissionLevel(I)Z"))
    private int modifyRequiredPermissionLevelForNbtQuery(int oldPermissionLevel) {
        return 0;
    }
}
