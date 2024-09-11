package net.earthcomputer.cheatlikedefnot;

import net.earthcomputer.cheatlikedefnot.packets.RuleUpdatePacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.Map;

public class CheatLikeDefnotClient implements ClientModInitializer {
    public static boolean isCheatLikeDefnotOnServer() {
        System.out.println("Checking if cheatlikedefnot is on server " + ClientPlayNetworking.canSend(CheatLikeDefnot.CHEATLIKEDEFNOT_MARKER));
        return ClientPlayNetworking.canSend(CheatLikeDefnot.CHEATLIKEDEFNOT_MARKER);
    }

    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
            client.execute(() -> {
                if (!client.isIntegratedServerRunning()) {
                    for (Rules.RuleInstance rule : Rules.getRules()) {
                        rule.set(rule.metadata().defaultValue());
                    }
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(RuleUpdatePacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (!context.client().isIntegratedServerRunning()) {
                    Map<String, Boolean> serverRules = payload.rules();
                    for (Rules.RuleInstance rule : Rules.getRules()) {
                        rule.set(serverRules.getOrDefault(rule.name(), rule.metadata().defaultValue()));
                    }
                    System.out.println("Received rule update from server");
                }
            });
        });
    }
}
