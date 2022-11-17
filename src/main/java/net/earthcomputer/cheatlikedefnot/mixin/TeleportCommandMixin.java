package net.earthcomputer.cheatlikedefnot.mixin;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeleportCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TeleportCommand.class)
public class TeleportCommandMixin {
    @Inject(method = {"method_13764", "method_13763"}, at = @At("HEAD"), cancellable = true)
    private static void changeRequirement(ServerCommandSource source, CallbackInfoReturnable<Boolean> cir) {
        if (source.getEntity() instanceof ServerPlayerEntity player) {
            if (player.interactionManager.getGameMode() == GameMode.SPECTATOR) {
                cir.setReturnValue(true);
            }
        }
    }
}
