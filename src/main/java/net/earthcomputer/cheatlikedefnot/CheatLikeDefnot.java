package net.earthcomputer.cheatlikedefnot;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.cheatlikedefnot.commands.CheatLikeDefnotCommands;
import net.earthcomputer.cheatlikedefnot.commands.DimensionCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

public class CheatLikeDefnot implements ModInitializer {
    private static final Identifier CHEATLIKEDEFNOT_MARKER = new Identifier("cheatlikedefnot", "marker");

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> Rules.load());
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher));

        ServerPlayNetworking.registerGlobalReceiver(CHEATLIKEDEFNOT_MARKER, (server, player, handler, buf, responseSender) -> {});
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        CheatLikeDefnotCommands.register(dispatcher);
        DimensionCommands.register(dispatcher);
    }

    public static boolean isCheatLikeDefnotOnServer() {
        return ClientPlayNetworking.canSend(CHEATLIKEDEFNOT_MARKER);
    }
}
