package ru.lionzxy.stopchangefuckingconfig.helper;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;
import ru.lionzxy.stopchangefuckingconfig.AbstractConfig;
import ru.lionzxy.stopchangefuckingconfig.ConfigPath;
import ru.lionzxy.stopchangefuckingconfig.ConfigWorkaround;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class ForgeConfig implements AbstractConfig {
    private final Map<ConfigPath, ForgeConfigSpec.ConfigValue<?>> specs = new HashMap<>();
    private final ForgeConfigSpec.Builder builder;

    private ForgeConfig(final ForgeConfigSpec.Builder builder) {
        this.builder = builder;
    }

    public static void initConfig(String modid, Class<?> clazz) {
        final ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        final AbstractConfig abstractConfig = new ForgeConfig(configBuilder);
        ConfigWorkaround.init(abstractConfig, clazz);
        final ForgeConfigSpec forgeConfigSpec = configBuilder.build();
        final Path configPath = FMLPaths.CONFIGDIR.get().resolve(modid + ".toml");

        final CommentedFileConfig configData = CommentedFileConfig.builder(configPath)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        configData.load();
        forgeConfigSpec.setConfig(configData);
        ConfigWorkaround.onReload(clazz);
    }

    @Override
    public Object getValue(ConfigPath path) {
        final ForgeConfigSpec.ConfigValue<?> configValue = specs.get(path);
        if (configValue == null) {
            return null;
        }
        return configValue.get();
    }

    @Override
    public void setList(ConfigPath path, List<?> value, String... comment) {
        int level = pushTo(path.getParent());
        if (!isNullOrContainsNull(comment)) {
            builder.comment(comment);
        }
        final ForgeConfigSpec.ConfigValue<?> configValue = builder.define(path.getName(), value, Objects::nonNull);
        specs.put(path, configValue);
        builder.pop(level);
    }

    @Override
    public void setValue(ConfigPath path, Object value, String... comment) {
        int level = pushTo(path.getParent());
        if (!isNullOrContainsNull(comment)) {
            builder.comment(comment);
        }
        final ForgeConfigSpec.ConfigValue<?> configValue = builder.define(path.getName(), value);
        specs.put(path, configValue);

        builder.pop(level);
    }

    private int pushTo(@Nullable ConfigPath path) {
        if (path == null) {
            return 0;
        }
        int level = pushTo(path.getParent());
        builder.push(path.getName());
        return level + 1;
    }

    public static boolean isNullOrContainsNull(@Nullable Object[] objs) {
        if (objs == null) {
            return true;
        }
        if (objs.length == 0) {
            return false;
        }
        for (Object obj : objs) {
            if (obj == null) {
                return true;
            }
        }
        return false;
    }
}
