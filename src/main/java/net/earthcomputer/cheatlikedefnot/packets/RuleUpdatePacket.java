package net.earthcomputer.cheatlikedefnot.packets;

import net.earthcomputer.cheatlikedefnot.CheatLikeDefnot;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.Map;

public record RuleUpdatePacket(Map<String, Boolean> rules) implements CustomPayload {
    public static final CustomPayload.Id<RuleUpdatePacket> ID = new CustomPayload.Id<>(CheatLikeDefnot.RULE_UPDATE_PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, RuleUpdatePacket> CODEC = CustomPayload.codecOf(RuleUpdatePacket::write, RuleUpdatePacket::new);

    private RuleUpdatePacket(RegistryByteBuf buf) {
        this(buf.readMap(buf1 -> buf1.readString(256), PacketByteBuf::readBoolean));
    }

    private void write(RegistryByteBuf buf) {
        buf.writeMap(this.rules, PacketByteBuf::writeString, PacketByteBuf::writeBoolean);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
