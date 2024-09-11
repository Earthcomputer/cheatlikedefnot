package net.earthcomputer.cheatlikedefnot;

import com.mojang.brigadier.CommandDispatcher;
import io.netty.handler.codec.DecoderException;
import net.earthcomputer.cheatlikedefnot.commands.CheatLikeDefnotCommands;
import net.earthcomputer.cheatlikedefnot.commands.DimensionCommands;
import net.earthcomputer.cheatlikedefnot.packets.CheatLikeDefnotMarkerPacket;
import net.earthcomputer.cheatlikedefnot.packets.RuleUpdatePacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CheatLikeDefnot implements ModInitializer {
    public static final Identifier CHEATLIKEDEFNOT_MARKER = Identifier.of("cheatlikedefnot", "marker");
    public static final Identifier RULE_UPDATE_PACKET_ID = Identifier.of("cheatlikedefnot", "rule_update");

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        CheatLikeDefnotCommands.register(dispatcher);
        DimensionCommands.register(dispatcher);
    }

    public static void syncRules(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (ServerPlayNetworking.canSend(player, RULE_UPDATE_PACKET_ID)) {
                syncRules(ServerPlayNetworking.getSender(player));
            }
        }
    }

    public static void syncRules(PacketSender sender) {
        Map<String, Boolean> rules = new HashMap<>();
        for (Rules.RuleInstance rule : Rules.getRules()) {
            rules.put(rule.name(), rule.get());
        }

        sender.sendPacket(new RuleUpdatePacket(rules));
    }

    @Nullable
    public static NbtCompound readUnlimitedNbt(PacketByteBuf buf) {
        NbtElement nbt = buf.readNbt(NbtSizeTracker.ofUnlimitedBytes());
        if (nbt != null && !(nbt instanceof NbtCompound)) {
            throw new DecoderException("Not a compound tag: " + nbt);
        }
        return (NbtCompound) nbt;
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> Rules.load());
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher));

        PayloadTypeRegistry.playC2S().register(CheatLikeDefnotMarkerPacket.ID, CheatLikeDefnotMarkerPacket.CODEC);

        PayloadTypeRegistry.playS2C().register(RuleUpdatePacket.ID, RuleUpdatePacket.CODEC);

        S2CPlayChannelEvents.REGISTER.register((handler, sender, server, channels) -> {
            if (channels.contains(RULE_UPDATE_PACKET_ID)) {
                syncRules(sender);
            }
        });
    }
}
