package net.earthcomputer.cheatlikedefnot;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.cheatlikedefnot.commands.CheatLikeDefnotCommands;
import net.earthcomputer.cheatlikedefnot.commands.DimensionCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class CheatLikeDefnot implements ModInitializer {
    public static final Identifier CHEATLIKEDEFNOT_MARKER = new Identifier("cheatlikedefnot", "marker");
    public static final Identifier RULE_UPDATE_PACKET = new Identifier("cheatlikedefnot", "rule_update");

    @Override
    public void onInitialize() {
        Rules.load();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher));

        ServerPlayNetworking.registerGlobalReceiver(CHEATLIKEDEFNOT_MARKER, (server, player, handler, buf, responseSender) -> {});

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
        if (!ServerPlayNetworking.canSend(player, RULE_UPDATE_PACKET)) {
            return;
        }

        Map<String, Boolean> rules = new HashMap<>();
        for (Rules.RuleInstance rule : Rules.getRules()) {
            rules.put(rule.name(), rule.get());
        }
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeMap(rules, PacketByteBuf::writeString, PacketByteBuf::writeBoolean);
        ServerPlayNetworking.send(player, RULE_UPDATE_PACKET, buf);
    }
}
