package com.dineshsawant.pg.cdc.transform;

import io.vertx.core.json.JsonObject;

import java.util.List;

public interface BinlogTransformer {
    List<JsonObject> transform(JsonObject binlogJson);
}
