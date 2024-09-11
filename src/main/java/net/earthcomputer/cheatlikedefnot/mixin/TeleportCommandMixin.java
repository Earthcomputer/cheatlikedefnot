package net.earthcomputer.cheatlikedefnot.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.earthcomputer.cheatlikedefnot.Rules;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeleportCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(TeleportCommand.class)
public class TeleportCommandMixin {
    @Unique
    private static final SimpleCommandExceptionType OTHER_ENTITY_EXCEPTION = new SimpleCommandExceptionType(Text.literal("You can't teleport other entities"));

    @Inject(method = {"method_13764", "method_13763"}, at = @At("HEAD"), cancellable = true)
    private static void changeRequirement(ServerCommandSource source, CallbackInfoReturnable<Boolean> cir) {
        if (Rules.spectatorTeleport && source.getEntity() instanceof ServerPlayerEntity player) {
            if (player.interactionManager.getGameMode() == GameMode.SPECTATOR) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "teleport", at = @At("HEAD"))
    private static void preventSpectatorsTeleportingOthers(ServerCommandSource source, Entity target, ServerWorld world, double x, double y, double z, Set<PositionFlag> movementFlags, float yaw, float pitch, TeleportCommand.LookTarget facingLocation, CallbackInfo ci) throws CommandSyntaxException {
        if (source.hasPermissionLevel(2)) {
            return;
        }

        if (target != source.getEntity()) {
            throw OTHER_ENTITY_EXCEPTION.create();
        }
    }
}
