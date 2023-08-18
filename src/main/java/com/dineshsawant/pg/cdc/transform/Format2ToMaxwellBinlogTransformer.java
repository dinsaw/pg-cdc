package com.dineshsawant.pg.cdc.transform;

import com.dineshsawant.pg.cdc.exception.PgCDCException;
import com.dineshsawant.pg.cdc.util.DateTimeUtil;
import io.vertx.core.json.JsonObject;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Format2ToMaxwellBinlogTransformer implements BinlogTransformer {

    @Override
    public List<JsonObject> transform(JsonObject binlogJson) {
        final String action = binlogJson.getString("action");

        switch (action) {
            case "I":
                return Collections.singletonList(transformInsert(binlogJson));
            case "U":
                return Collections.singletonList(transformUpdate(binlogJson));
            case "D":
                return Collections.singletonList(transformDelete(binlogJson));
            default:
                throw new PgCDCException("Unable to transform action=" + action);
        }
    }

    private JsonObject transformInsert(JsonObject binlogJson) {
        return new JsonObject()
                .put(MaxwellBinlogKeys.DATABASE, binlogJson.getString("schema"))
                .put(MaxwellBinlogKeys.TABLE, binlogJson.getString("table"))
                .put(MaxwellBinlogKeys.TYPE, "insert")
                .put(MaxwellBinlogKeys.TS, DateTimeUtil.convertToEpoch(binlogJson.getString("timestamp")))
                .put(MaxwellBinlogKeys.XID, binlogJson.getLong("xid"))
                .put(MaxwellBinlogKeys.COMMIT, true)
                .put(MaxwellBinlogKeys.DATA, getData(binlogJson));
    }

    private JsonObject getData(JsonObject binlogJson) {
        JsonObject dataJson = new JsonObject();
        binlogJson.getJsonArray("columns").forEach(col -> {
            JsonObject colJson = (JsonObject) col;
            dataJson.put(colJson.getString("name"), colJson.getValue("value"));
        });
        return dataJson;
    }

    private JsonObject transformUpdate(JsonObject binlogJson) {


        final JsonObject data = getData(binlogJson);
        final JsonObject oldData = getOldData(binlogJson, data);

        return new JsonObject()
                .put(MaxwellBinlogKeys.DATABASE, binlogJson.getString("schema"))
                .put(MaxwellBinlogKeys.TABLE, binlogJson.getString("table"))
                .put(MaxwellBinlogKeys.TYPE, "update")
                .put(MaxwellBinlogKeys.TS, DateTimeUtil.convertToEpoch(binlogJson.getString("timestamp")))
                .put(MaxwellBinlogKeys.XID, binlogJson.getLong("xid"))
                .put(MaxwellBinlogKeys.COMMIT, true)
                .put(MaxwellBinlogKeys.DATA, data)
                .put(MaxwellBinlogKeys.OLD, oldData);
    }

    private JsonObject getOldData(JsonObject binlogJson, JsonObject data) {
        final JsonObject oldData = new JsonObject();
        binlogJson.getJsonArray("identity").forEach(col -> {
            JsonObject colJson = (JsonObject) col;
            final String key = colJson.getString("name");
            final Object oldValue = colJson.getValue("value");

            if (data.containsKey(key) && !Objects.equals(oldValue, data.getValue(key))) {
                oldData.put(key, oldValue);
            }
        });
        return oldData;
    }

    private JsonObject transformDelete(JsonObject binlogJson) {
        final JsonObject oldDataJSON = new JsonObject();
        binlogJson.getJsonArray("identity").forEach(col -> {
            JsonObject colJson = (JsonObject) col;
            oldDataJSON.put(colJson.getString("name"), colJson.getValue("value"));
        });

        return new JsonObject()
                .put(MaxwellBinlogKeys.DATABASE, binlogJson.getString("schema"))
                .put(MaxwellBinlogKeys.TABLE, binlogJson.getString("table"))
                .put(MaxwellBinlogKeys.TYPE, "delete")
                .put(MaxwellBinlogKeys.TS, DateTimeUtil.convertToEpoch(binlogJson.getString("timestamp")))
                .put(MaxwellBinlogKeys.XID, binlogJson.getLong("xid"))
                .put(MaxwellBinlogKeys.COMMIT, true)
                .put(MaxwellBinlogKeys.DATA, oldDataJSON);
    }

}
