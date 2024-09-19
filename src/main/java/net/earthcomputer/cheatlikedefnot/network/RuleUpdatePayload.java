package net.earthcomputer.cheatlikedefnot.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

public record RuleUpdatePayload(Map<String, Boolean> rules) implements CustomPayload {
    public static final Id<RuleUpdatePayload> ID = new Id<>(new Identifier("cheatlikedefnot", "rule_update"));
    public static final PacketCodec<RegistryByteBuf, RuleUpdatePayload> CODEC = PacketCodecs.map(
        (IntFunction<Map<String, Boolean>>) HashMap::new,
        PacketCodecs.string(256),
        PacketCodecs.BOOL
    ).<RegistryByteBuf>cast().xmap(RuleUpdatePayload::new, RuleUpdatePayload::rules);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
