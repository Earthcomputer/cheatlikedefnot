package net.earthcomputer.cheatlikedefnot;

import net.earthcomputer.cheatlikedefnot.network.MarkerPayload;
import net.earthcomputer.cheatlikedefnot.network.RuleUpdatePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class CheatLikeDefnotClient implements ClientModInitializer {
    public static boolean isCheatLikeDefnotOnServer() {
        return ClientPlayNetworking.canSend(MarkerPayload.ID);
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
        ClientPlayNetworking.registerGlobalReceiver(RuleUpdatePayload.ID, (payload, context) -> {
            if (!context.client().isIntegratedServerRunning()) {
                for (Rules.RuleInstance rule : Rules.getRules()) {
                    rule.set(payload.rules().getOrDefault(rule.name(), rule.metadata().defaultValue()));
                }
            }
        });
    }
}
