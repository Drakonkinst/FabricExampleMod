package io.github.drakonkinst.worldsinger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.github.drakonkinst.worldsinger.util.json.JsonStack;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public final class WorldsingerConfig {

    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(WorldsingerConfig.class,
            new Serializer()).setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "worldsinger_config.json";
    private static final String DEFAULT_CONFIG_FILE_PATH = "/default_config.json";
    private static final String KEY_FLUIDLOGGABLE_FLUIDS = "fluidloggable_fluids";
    private static WorldsingerConfig INSTANCE;

    public static WorldsingerConfig instance() {
        if (INSTANCE == null) {
            Path configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
            try {
                if (!Files.exists(configPath)) {
                    Files.createDirectories(configPath.getParent());
                    Files.copy(Objects.requireNonNull(
                                    WorldsingerConfig.class.getResourceAsStream(DEFAULT_CONFIG_FILE_PATH)),
                            configPath);
                }
                try (Reader reader = Files.newBufferedReader(configPath)) {
                    INSTANCE = GSON.fromJson(reader, WorldsingerConfig.class);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return INSTANCE;
    }

    private final List<Identifier> fluidloggableFluids;

    private WorldsingerConfig(List<Identifier> fluidloggableFluids) {
        this.fluidloggableFluids = fluidloggableFluids;
    }

    public List<Identifier> getFluidloggableFluids() {
        return fluidloggableFluids;
    }

    private static class Serializer implements JsonDeserializer<WorldsingerConfig> {

        private static List<Identifier> stringListToIdentifierList(JsonStack stack,
                List<String> strList) {
            List<Identifier> idList = new ArrayList<>(strList.size());
            for (String str : strList) {
                Identifier id = Identifier.tryParse(str);
                if (id == null) {
                    stack.addError("Unable to parse id " + str);
                } else {
                    idList.add(id);
                }
            }
            return idList;
        }

        @Override
        public WorldsingerConfig deserialize(JsonElement root, Type type,
                JsonDeserializationContext context) throws JsonParseException {
            JsonStack jsonStack = new JsonStack(GSON, root);
            jsonStack.allow(KEY_FLUIDLOGGABLE_FLUIDS);

            List<String> fluidloggableFluidStrings = jsonStack.streamAs(KEY_FLUIDLOGGABLE_FLUIDS,
                    String.class).toList();
            List<Identifier> fluidloggableFluidIds = Serializer.stringListToIdentifierList(
                    jsonStack, fluidloggableFluidStrings);

            return new WorldsingerConfig(fluidloggableFluidIds);
        }
    }
}
