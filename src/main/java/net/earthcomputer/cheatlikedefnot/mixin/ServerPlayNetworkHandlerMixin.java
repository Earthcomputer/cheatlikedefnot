package net.earthcomputer.cheatlikedefnot.mixin;

import net.earthcomputer.cheatlikedefnot.Rules;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @ModifyArg(method = {"onQueryEntityNbt"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;hasPermissionLevel(I)Z"))
    private int modifyRequiredPermissionLevelForEntityNbtQuery(int oldPermissionLevel) {
        return Rules.nonOpEntityNbtQueries ? 0 : oldPermissionLevel;
    }

    @ModifyArg(method = {"onQueryBlockNbt"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;hasPermissionLevel(I)Z"))
    private int modifyRequiredPermissionLevelForBlockNbtQuery(int oldPermissionLevel) {
        return Rules.nonOpBlockNbtQueries ? 0 : oldPermissionLevel;
    }
}
