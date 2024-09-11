package net.earthcomputer.cheatlikedefnot.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.earthcomputer.cheatlikedefnot.CheatLikeDefnot;
import net.earthcomputer.cheatlikedefnot.Rules;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.NbtQueryResponseS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NbtQueryResponseS2CPacket.class)
public class NbtQueryResponseS2CPacketMixin {
    @WrapOperation(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readNbt()Lnet/minecraft/nbt/NbtCompound;"))
    private NbtCompound removeReadNbtLimit(PacketByteBuf instance, Operation<NbtCompound> original) {
        return Rules.removeNbtQuerySizeLimit ? CheatLikeDefnot.readUnlimitedNbt(instance) : original.call(instance);
    }
}
