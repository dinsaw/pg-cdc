package com.dineshsawant.pg.cdc.state;

import com.google.common.flogger.FluentLogger;
import com.dineshsawant.pg.cdc.config.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import static org.junit.Assert.*;

public class JsonFileStateStoreTest {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    public static final String TEST_SLOT = "test-slot";
    public static final String TEST_SEQUENCE_NUMBER = "test-sequence-number";
    Configuration configuration = new Configuration("conf/test/test-json-file-store.properties");

    public JsonFileStateStoreTest() throws IOException {
    }

    @Test
    public void testFetchAndCreate() throws IOException {
        StateStore stateStore = new JsonFileStateStore(configuration);

        final Optional<State> state = stateStore.fetchState("test-slot");
        assertFalse(state.isPresent());

        final boolean persisted = stateStore.createOrUpdateState(new State(TEST_SLOT, TEST_SEQUENCE_NUMBER));
        assertTrue(persisted);

        final boolean persisted2 = stateStore.createOrUpdateState(new State(TEST_SLOT + 1, TEST_SEQUENCE_NUMBER + 1));
        assertTrue(persisted2);

        final Optional<State> stateOptional = stateStore.fetchState("test-slot");
        assertTrue(stateOptional.isPresent());

        final State fetchedState = stateOptional.get();
        logger.atInfo().log("State = %s", fetchedState);
        assertEquals(TEST_SLOT, fetchedState.getSlotName());
        assertEquals(TEST_SEQUENCE_NUMBER, fetchedState.getLastLogSequenceNumber());
    }

    @Test
    public void testUpdate() {
        StateStore stateStore = new JsonFileStateStore(configuration);

        final Optional<State> state = stateStore.fetchState("test-slot");
        assertFalse(state.isPresent());

        //Create
        assertTrue(stateStore.createOrUpdateState(new State(TEST_SLOT, TEST_SEQUENCE_NUMBER)));

        //Update
        assertTrue(stateStore.createOrUpdateState(new State(TEST_SLOT, TEST_SEQUENCE_NUMBER+1)));

        final Optional<State> stateOptional = stateStore.fetchState("test-slot");
        assertTrue(stateOptional.isPresent());

        final State fetchedState = stateOptional.get();
        logger.atInfo().log("State = %s", fetchedState);
        assertEquals(TEST_SLOT, fetchedState.getSlotName());
        assertEquals(TEST_SEQUENCE_NUMBER+1, fetchedState.getLastLogSequenceNumber());
    }

    @After
    @Before
    public void cleanUp() throws IOException {
        logger.atInfo().log("Deleting test file");
        Files.deleteIfExists(new File(configuration.getStateStoreJsonFilePath()).toPath());
    }
}