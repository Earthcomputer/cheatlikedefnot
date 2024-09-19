package net.earthcomputer.cheatlikedefnot.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.earthcomputer.cheatlikedefnot.Rules;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(DataCommand.class)
public class DataCommandMixin {
    @ModifyConstant(method = "method_13890", constant = @Constant(intValue = 2))
    private static int modifyMainPermissionLevel(int permissionLevel) {
        return Rules.dataGetCommand ? 0 : permissionLevel;
    }

    @WrapOperation(method = "register", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;then(Lcom/mojang/brigadier/builder/ArgumentBuilder;)Lcom/mojang/brigadier/builder/ArgumentBuilder;"))
    private static <T1 extends ArgumentBuilder<ServerCommandSource, T1>> ArgumentBuilder<ServerCommandSource, T1> modifySubPermissionLevel(
        LiteralArgumentBuilder<ServerCommandSource> instance,
        ArgumentBuilder<ServerCommandSource, ?> argumentBuilder,
        Operation<ArgumentBuilder<ServerCommandSource, T1>> original,
        @Local(ordinal = 0) LiteralArgumentBuilder<ServerCommandSource> dataNode
    ) {
        if (Rules.dataGetCommand && instance == dataNode) {
            if (!(argumentBuilder instanceof LiteralArgumentBuilder<?> literal) || !"get".equals(literal.getLiteral())) {
                argumentBuilder.requires(source -> source.hasPermissionLevel(2));
            }
        }
        return original.call(instance, argumentBuilder);
    }
}
