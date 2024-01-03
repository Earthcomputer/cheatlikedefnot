package net.earthcomputer.cheatlikedefnot;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.cheatlikedefnot.commands.CheatLikeDefnotCommands;
import net.earthcomputer.cheatlikedefnot.commands.DimensionCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents;
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
        ServerLifecycleEvents.SERVER_STARTING.register(server -> Rules.load());
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> registerCommands(dispatcher));

        ServerPlayNetworking.registerGlobalReceiver(CHEATLIKEDEFNOT_MARKER, (server, player, handler, buf, responseSender) -> {});

        S2CPlayChannelEvents.REGISTER.register((handler, sender, server, channels) -> {
            if (channels.contains(RULE_UPDATE_PACKET)) {
                syncRules(sender);
            }
        });
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        CheatLikeDefnotCommands.register(dispatcher);
        DimensionCommands.register(dispatcher);
    }

    public static void syncRules(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (ServerPlayNetworking.canSend(player, RULE_UPDATE_PACKET)) {
                syncRules(ServerPlayNetworking.getSender(player));
            }
        }
    }

    public static void syncRules(PacketSender sender) {
        Map<String, Boolean> rules = new HashMap<>();
        for (Rules.RuleInstance rule : Rules.getRules()) {
            rules.put(rule.name(), rule.get());
        }
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeMap(rules, PacketByteBuf::writeString, PacketByteBuf::writeBoolean);
        sender.sendPacket(RULE_UPDATE_PACKET, buf);
    }
}
