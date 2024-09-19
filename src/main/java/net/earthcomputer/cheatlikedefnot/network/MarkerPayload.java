package net.earthcomputer.cheatlikedefnot.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public final class MarkerPayload implements CustomPayload {
    public static final Id<MarkerPayload> ID = new Id<>(new Identifier("cheatlikedefnot", "marker"));
    public static final MarkerPayload INSTANCE = new MarkerPayload();
    public static final PacketCodec<RegistryByteBuf, MarkerPayload> CODEC = PacketCodec.unit(INSTANCE);

    private MarkerPayload() {
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
