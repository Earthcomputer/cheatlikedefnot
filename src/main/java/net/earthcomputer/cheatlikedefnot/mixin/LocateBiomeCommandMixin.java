package net.earthcomputer.cheatlikedefnot.mixin;

import net.minecraft.server.command.LocateBiomeCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LocateBiomeCommand.class)
public class LocateBiomeCommandMixin {
    @ModifyConstant(method = "method_24494", constant = @Constant(intValue = 2))
    private static int modifyPermissionLevel(int permissionLevel) {
        return 0;
    }
}
