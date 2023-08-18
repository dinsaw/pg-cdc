package com.dineshsawant.pg.cdc.state;

import com.dineshsawant.pg.cdc.config.Configuration;
import com.dineshsawant.pg.cdc.exception.PgCDCException;

import java.util.Optional;

public interface StateStore {
    String JSONFILE = "jsonfile";


    Optional<State> fetchState(String slotName);

    boolean createOrUpdateState(State state);

    static StateStore createStore(Configuration configuration) {
        final String stateStoreType = configuration.getStateStoreType();
        if (JSONFILE.equals(stateStoreType)) {
            return new JsonFileStateStore(configuration);
        } else {
            throw new PgCDCException("Not found any mapping for configured store = " + stateStoreType);
        }
    }
}
