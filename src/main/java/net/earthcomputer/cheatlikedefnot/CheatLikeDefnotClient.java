package net.earthcomputer.cheatlikedefnot;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;

import java.util.Map;

public class CheatLikeDefnotClient implements ClientModInitializer {
    public static boolean isCheatLikeDefnotOnServer() {
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
        ClientPlayNetworking.registerGlobalReceiver(CheatLikeDefnot.RULE_UPDATE_PACKET, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                if (!client.isIntegratedServerRunning()) {
                    Map<String, Boolean> serverRules = buf.readMap(buf1 -> buf1.readString(256), PacketByteBuf::readBoolean);
                    for (Rules.RuleInstance rule : Rules.getRules()) {
                        rule.set(serverRules.getOrDefault(rule.name(), rule.metadata().defaultValue()));
                    }
                }
            });
        });
    }
}
