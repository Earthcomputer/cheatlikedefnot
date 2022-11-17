package net.earthcomputer.cheatlikedefnot.mixin;

import net.minecraft.server.command.LocateCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LocateCommand.class)
public class LocateCommandMixin {
    @ModifyConstant(method = "method_13448", constant = @Constant(intValue = 2))
    private static int modifyPermissionLevel(int permissionLevel) {
        return 0;
    }
}
