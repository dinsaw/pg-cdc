package com.dineshsawant.pg.cdc.state;

import com.google.common.flogger.FluentLogger;
import com.dineshsawant.pg.cdc.config.Configuration;
import com.dineshsawant.pg.cdc.exception.PgCDCException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class JsonFileStateStore implements StateStore {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private final String storeFilePath;
    private File jsonFile;

    public JsonFileStateStore(Configuration configuration) {
        this.storeFilePath = configuration.getStateStoreJsonFilePath();
        try {
            prepareFile(storeFilePath);
        } catch (IOException e) {
            throw new PgCDCException("Exception while creating json file path", e);
        }

    }

    private void prepareFile(String storeFilePath) throws IOException {
        this.jsonFile = new File(storeFilePath);
        final boolean isCreated = this.jsonFile.createNewFile();
        if (isCreated) {
            logger.atInfo().log("A new file created at json file path %s", storeFilePath);
            try (FileOutputStream fileOutputStream = new FileOutputStream(this.jsonFile, false)) {
                fileOutputStream.write("{}".getBytes());
            }
        } else {
            logger.atInfo().log("Using existing file at json file path %s", storeFilePath);
        }
    }

    @Override
    public Optional<State> fetchState(String slotName) {
        JsonObject states = fetchStatesFromFile();
        if (states.containsKey(slotName)) {
            final JsonObject stateJson = states.getJsonObject(slotName, new JsonObject());
            return Optional.of(stateJson.mapTo(State.class));
        } else {
            return Optional.empty();
        }
    }

    @NotNull
    private JsonObject fetchStatesFromFile() {
        try (FileInputStream fileInputStream = new FileInputStream(this.jsonFile)) {
            return new JsonObject(readWholeFile(fileInputStream));
        } catch (IOException e) {
            throw new PgCDCException("Error while fetching state", e);
        }
    }

    private String readWholeFile(FileInputStream fileInputStream) throws IOException {
        byte[] data = new byte[(int) this.jsonFile.length()];
        final int read = fileInputStream.read(data);
        logger.atInfo().log("Bytes Read = " + read);
        return new String(data, StandardCharsets.UTF_8);
    }

    @Override
    public boolean createOrUpdateState(State currentState) {
        try (FileInputStream fileInputStream = new FileInputStream(this.jsonFile)) {
            final String statesJsonStr = readWholeFile(fileInputStream);
            final JsonObject statesJson = new JsonObject(statesJsonStr);

            if (statesJson.containsKey(currentState.getSlotName())) {
                final JsonObject storedStateJson = statesJson.getJsonObject(currentState.getSlotName());
                final State storedState = storedStateJson.mapTo(State.class);
                storedState.merge(currentState);

                statesJson.put(storedState.getSlotName(), new JsonObject(Json.encode(storedState)));

                try (PrintWriter writer = new PrintWriter(this.jsonFile)) {
                    writer.print(statesJson);
                }
            } else {
                statesJson.put(currentState.getSlotName(), new JsonObject(Json.encode(currentState)));
                try (PrintWriter writer = new PrintWriter(this.jsonFile)) {
                    writer.print(statesJson);
                }
            }
            return true;
        } catch (IOException e) {
            throw new PgCDCException("Error when saving state", e);
        }
    }
}
