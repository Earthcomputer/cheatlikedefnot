package net.earthcomputer.cheatlikedefnot.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.earthcomputer.cheatlikedefnot.CheatLikeDefnot;
import net.earthcomputer.cheatlikedefnot.Rules;
import net.minecraft.command.CommandSource;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;

import static com.mojang.brigadier.arguments.BoolArgumentType.*;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.minecraft.server.command.CommandManager.*;

public class CheatLikeDefnotCommands {
    private static final SuggestionProvider<ServerCommandSource> RULE_COMPLETION = (context, builder) -> CommandSource.suggestMatching(Arrays.stream(Rules.getRules()).map(Rules.RuleInstance::name), builder);
    private static final DynamicCommandExceptionType NO_SUCH_RULE_EXCEPTION = new DynamicCommandExceptionType(rule -> Text.literal("No such rule: " + rule));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("cheatlikedefnot")
            .requires(source -> source.hasPermissionLevel(2))
            .then(literal("rules")
                .executes(ctx -> listRules(ctx.getSource())))
            .then(literal("explain")
                .then(argument("rule", word())
                    .suggests(RULE_COMPLETION)
                    .executes(ctx -> explainRule(ctx.getSource(), getString(ctx, "rule")))))
            .then(literal("get")
                .then(argument("rule", word())
                    .suggests(RULE_COMPLETION)
                    .executes(ctx -> getRule(ctx.getSource(), getString(ctx, "rule")))))
            .then(literal("set")
                .then(argument("rule", word())
                    .suggests(RULE_COMPLETION)
                    .then(argument("value", bool())
                        .executes(ctx -> setRule(ctx.getSource(), getString(ctx, "rule"), getBool(ctx, "value")))))));
    }

    private static int listRules(ServerCommandSource source) {
        for (Rules.RuleInstance rule : Rules.getRules()) {
            source.sendFeedback(() -> ruleValueOutput(rule), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int explainRule(ServerCommandSource source, String rule) throws CommandSyntaxException {
        System.out.println("Explaining rule " + rule);
        Rules.RuleInstance ruleInstance = getRule(rule);
        source.sendFeedback(() -> Text.literal(ruleInstance.name()).styled(style -> style.withUnderline(true)), false);
        source.sendFeedback(() -> Text.literal(ruleInstance.metadata().description()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int getRule(ServerCommandSource source, String rule) throws CommandSyntaxException {
        Rules.RuleInstance ruleInstance = getRule(rule);
        source.sendFeedback(() -> ruleValueOutput(ruleInstance), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int setRule(ServerCommandSource source, String rule, boolean value) throws CommandSyntaxException {
        Rules.RuleInstance ruleInstance = getRule(rule);
        ruleInstance.set(value);
        Rules.save();
        CheatLikeDefnot.syncRules(source.getServer());
        source.sendFeedback(() -> Text.literal(rule + " has been updated to " + value), true);

        // re-send command trees as command permissions may have changed
        PlayerManager playerManager = source.getServer().getPlayerManager();
        for (ServerPlayerEntity player : playerManager.getPlayerList()) {
            playerManager.sendCommandTree(player);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static Text ruleValueOutput(Rules.RuleInstance rule) {
        return Text.literal("")
            .append(Text.literal(rule.name()).styled(style -> style.withBold(true)))
            .append(" = ")
            .append(Text.literal(Boolean.toString(rule.get()))
                .styled(style -> style.withColor(Formatting.GRAY).withItalic(rule.get() != rule.metadata().defaultValue())))
            .append(Text.literal(" [default: " + rule.metadata().defaultValue() + "]")
                .styled(style -> style.withColor(Formatting.DARK_GRAY)));
    }

    private static Rules.RuleInstance getRule(String rule) throws CommandSyntaxException {
        return Arrays.stream(Rules.getRules())
            .filter(r -> r.name().equals(rule))
            .findAny()
            .orElseThrow(() -> NO_SUCH_RULE_EXCEPTION.create(rule));
    }
}
