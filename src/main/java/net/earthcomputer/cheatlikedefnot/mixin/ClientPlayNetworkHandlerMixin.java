package net.earthcomputer.cheatlikedefnot.mixin;

import net.earthcomputer.cheatlikedefnot.CLDDataQueryHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.NbtQueryResponseS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin implements CLDDataQueryHandler.IClientPlayNetworkHandler {
    @Unique
    private final CLDDataQueryHandler cheatlikedefnot_cldDataQueryHandler = new CLDDataQueryHandler((ClientPlayNetworkHandler) (Object) this);

    @Override
    public CLDDataQueryHandler cheatlikedefnot_getCLDDataQueryHandler() {
        return cheatlikedefnot_cldDataQueryHandler;
    }

    @Inject(method = "onNbtQueryResponse", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onOnNbtQueryResponse(NbtQueryResponseS2CPacket packet, CallbackInfo ci) {
        if (cheatlikedefnot_cldDataQueryHandler.handleQueryResponse(packet.getTransactionId(), packet.getNbt())) {
            ci.cancel();
        }
    }
}
