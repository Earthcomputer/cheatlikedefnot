package net.earthcomputer.cheatlikedefnot;

import com.mojang.brigadier.CommandDispatcher;
import io.netty.handler.codec.DecoderException;
import net.earthcomputer.cheatlikedefnot.commands.CheatLikeDefnotCommands;
import net.earthcomputer.cheatlikedefnot.commands.DimensionCommands;
import net.earthcomputer.cheatlikedefnot.network.MarkerPayload;
import net.earthcomputer.cheatlikedefnot.network.RuleUpdatePayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CheatLikeDefnot implements ModInitializer {
    @Override
    public void onInitialize() {
        Rules.load();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher));

        PayloadTypeRegistry.playC2S().register(MarkerPayload.ID, MarkerPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(MarkerPayload.ID, (payload, context) -> {});
        PayloadTypeRegistry.playS2C().register(RuleUpdatePayload.ID, RuleUpdatePayload.CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> syncRules(handler.player));
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        CheatLikeDefnotCommands.register(dispatcher);
        DimensionCommands.register(dispatcher);
    }

    public static void syncRules(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            syncRules(player);
        }
    }

    public static void syncRules(ServerPlayerEntity player) {
        if (!ServerPlayNetworking.canSend(player, RuleUpdatePayload.ID)) {
            return;
        }

        Map<String, Boolean> rules = new HashMap<>();
        for (Rules.RuleInstance rule : Rules.getRules()) {
            rules.put(rule.name(), rule.get());
        }
        ServerPlayNetworking.send(player, new RuleUpdatePayload(rules));
    }

    @Nullable
    public static NbtCompound readUnlimitedNbt(PacketByteBuf buf) {
        NbtElement nbt = buf.readNbt(NbtSizeTracker.ofUnlimitedBytes());
        if (nbt != null && !(nbt instanceof NbtCompound)) {
            throw new DecoderException("Not a compound tag: " + nbt);
        }
        return (NbtCompound) nbt;
    }
}
