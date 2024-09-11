package net.earthcomputer.cheatlikedefnot.packets;

import net.earthcomputer.cheatlikedefnot.CheatLikeDefnot;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record CheatLikeDefnotMarkerPacket() implements CustomPayload {
    public static final CustomPayload.Id<CheatLikeDefnotMarkerPacket> ID = new CustomPayload.Id<>(CheatLikeDefnot.CHEATLIKEDEFNOT_MARKER);
    public static final PacketCodec<RegistryByteBuf, CheatLikeDefnotMarkerPacket> CODEC = CustomPayload.codecOf(CheatLikeDefnotMarkerPacket::write, CheatLikeDefnotMarkerPacket::new);

    private CheatLikeDefnotMarkerPacket(RegistryByteBuf buf) {
        this();
    }

    private void write(RegistryByteBuf buf) {

    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
