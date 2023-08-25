package net.earthcomputer.cheatlikedefnot.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeleportCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.EnumSet;
import java.util.Objects;

import static net.minecraft.command.argument.DimensionArgumentType.*;
import static net.minecraft.server.command.CommandManager.*;

public class DimensionCommands {
    private static final DynamicCommandExceptionType SAME_DIMENSION_EXCEPTION = new DynamicCommandExceptionType(dimension -> Text.literal("You are already in " + dimension + "!"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        registerDtp(dispatcher, "dtp");
        registerDtp(dispatcher, "dimensionteleport");
    }

    private static void registerDtp(CommandDispatcher<ServerCommandSource> dispatcher, String name) {
        dispatcher.register(literal(name)
            .requires(source -> {
                ServerPlayerEntity player = source.getPlayer();
                if (player == null) {
                    return true;
                }
                GameMode gameMode = player.interactionManager.getGameMode();
                return gameMode == GameMode.SPECTATOR || gameMode == GameMode.CREATIVE;
            })
            .then(argument("dimension", dimension())
                .executes(ctx -> changeDimension(ctx.getSource(), getDimensionArgument(ctx, "dimension")))));
    }

    private static int changeDimension(ServerCommandSource source, ServerWorld destWorld) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerWorld sourceWorld = player.getWorld();

        if (sourceWorld == destWorld) {
            throw SAME_DIMENSION_EXCEPTION.create(destWorld.getRegistryKey().getValue());
        }

        Vec3d destPos = getDestPos(player, player.getPos(), sourceWorld, destWorld);

        TeleportCommand.teleport(source, player, destWorld, destPos.x, destPos.y, destPos.z, EnumSet.noneOf(PlayerPositionLookS2CPacket.Flag.class), player.getYaw(), player.getPitch(), null);

        source.sendFeedback(Text.literal("You have been teleported to " + destWorld.getRegistryKey().getValue()), true);

        return Command.SINGLE_SUCCESS;
    }

    private static Vec3d getDestPos(ServerPlayerEntity player, Vec3d pos, ServerWorld sourceWorld, ServerWorld destWorld) {
        if (destWorld.getRegistryKey() == World.END) {
            return Vec3d.ofBottomCenter(ServerWorld.END_SPAWN_POS);
        } else if (sourceWorld.getRegistryKey() == World.END) {
            Vec3d tempPos;
            RegistryKey<World> tempDimension;
            if (player.getSpawnPointPosition() == null || player.getSpawnPointDimension() == World.END) {
                tempPos = Vec3d.ofBottomCenter(Objects.requireNonNull(player.server.getWorld(World.OVERWORLD)).getSpawnPos());
                tempDimension = World.OVERWORLD;
            } else {
                tempPos = Vec3d.ofBottomCenter(player.getSpawnPointPosition());
                tempDimension = player.getSpawnPointDimension();
            }
            if (tempDimension == destWorld.getRegistryKey()) {
                return tempPos;
            } else {
                ServerWorld tempWorld = player.server.getWorld(tempDimension);
                if (tempWorld == null) {
                    // This is possible if a datapack is removed and someone's spawn point was in the datapack-added dimension
                    tempWorld = Objects.requireNonNull(player.server.getWorld(World.OVERWORLD));
                }
                return getDestPos(player, tempPos, tempWorld, destWorld);
            }
        } else {
            double scaleFactor = DimensionType.getCoordinateScaleFactor(sourceWorld.getDimension(), destWorld.getDimension());
            return new Vec3d(pos.x * scaleFactor, pos.y, pos.z * scaleFactor);
        }
    }
}