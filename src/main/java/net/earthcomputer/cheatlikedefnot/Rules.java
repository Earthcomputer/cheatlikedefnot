package net.earthcomputer.cheatlikedefnot;

import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class Rules {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final RuleInstance[] RULES;
    static {
        List<RuleInstance> rules = new ArrayList<>();
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        for (Field field : Rules.class.getFields()) {
            Rule rule = field.getAnnotation(Rule.class);
            if (rule == null) {
                continue;
            }

            int modifiers = field.getModifiers();
            if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
                throw new AssertionError("Rules contains a rule field with the wrong modifiers");
            }
            if (field.getType() != boolean.class) {
                throw new AssertionError("Rules contains a non-boolean rule field");
            }
            VarHandle fieldHandle;
            try {
                fieldHandle = lookup.unreflectVarHandle(field);
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
            rules.add(new RuleInstance(field.getName(), fieldHandle, rule));
        }
        RULES = rules.toArray(RuleInstance[]::new);
    }

    public static RuleInstance[] getRules() {
        return RULES;
    }

    private Rules() {
    }

    @Rule(defaultValue = true, description = "Enables the /dtp and /dimensionteleport commands for all spectators")
    public static boolean dtpCommand;

    @Rule(defaultValue = false, description = "Causes the /dtp and /dimensionteleport command to fail if it would teleport the player to an ungenerated chunk")
    public static boolean dtpPreventChunkGeneration;

    @Rule(defaultValue = true, description = "Enables the /locate command for everyone")
    public static boolean locateCommand;

    @Rule(defaultValue = true, description = "Enables the block NBT query packet for everyone")
    public static boolean nonOpBlockNbtQueries;

    @Rule(defaultValue = true, description = "Enables the entity NBT query packet for everyone")
    public static boolean nonOpEntityNbtQueries;

    @Rule(defaultValue = true, description = "Enables spectators to use the /tp command to teleport to arbitrary coordinates")
    public static boolean spectatorTeleport;

    public static void load() {
        RuleInstance[] rules = getRules();
        for (RuleInstance rule : rules) {
            rule.set(rule.metadata.defaultValue());
        }

        Properties properties = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(getConfigFile())) {
            properties.load(reader);
        } catch (NoSuchFileException e) {
            return;
        } catch (IOException e) {
            LOGGER.error("Failed to load cheatlikedefnot rules", e);
            return;
        }

        for (RuleInstance rule : rules) {
            String value = properties.getProperty(rule.name);
            if (value != null) {
                rule.set(Boolean.parseBoolean(value));
            }
        }
    }

    public static void save() {
        Properties properties = new Properties();
        for (RuleInstance rule : getRules()) {
            if (rule.get() != rule.metadata.defaultValue()) {
                properties.setProperty(rule.name, Boolean.toString(rule.get()));
            }
        }
        try {
            Files.createDirectories(getConfigFile().getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(getConfigFile())) {
                properties.store(writer, "CheatLikeDefnot configuration file");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save cheatlikedefnot rules", e);
        }
    }

    private static Path getConfigFile() {
        return FabricLoader.getInstance().getConfigDir().resolve("cheatlikedefnot").resolve("config.properties");
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Rule {
        boolean defaultValue();
        String description();
    }

    public record RuleInstance(String name, VarHandle field, Rule metadata) {
        public boolean get() {
            return (boolean) field.get();
        }

        public void set(boolean value) {
            field.set(value);
        }
    }
}
