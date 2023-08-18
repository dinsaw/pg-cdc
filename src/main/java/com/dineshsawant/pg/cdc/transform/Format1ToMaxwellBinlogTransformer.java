package com.dineshsawant.pg.cdc.transform;

import com.dineshsawant.pg.cdc.exception.PgCDCException;
import com.dineshsawant.pg.cdc.util.DateTimeUtil;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Format1ToMaxwellBinlogTransformer implements BinlogTransformer {
    @Override
    public List<JsonObject> transform(JsonObject binlogJson) {

        final Long transactionId = binlogJson.getLong("xid");
        final long epochTimestamp = DateTimeUtil.convertToEpoch(binlogJson.getString("timestamp"));

        return binlogJson.getJsonArray("change")
                .stream()
                .map(c -> transform(transactionId, epochTimestamp, (JsonObject) c))
                .collect(Collectors.toList());
    }

    private JsonObject transform(Long transactionId, long epochTimestamp, JsonObject changeJson) {
        final String operationKind = changeJson.getString("kind");
        switch (operationKind) {
            case "insert":
                return transformInsert(transactionId, epochTimestamp, changeJson);
            case "update":
                return transformUpdate(transactionId, epochTimestamp, changeJson);
            case "delete":
                return transformDelete(transactionId, epochTimestamp, changeJson);
            default:
                throw new PgCDCException("Unable to transform action=" + operationKind);
        }
    }



    private JsonObject transformInsert(Long transactionId, long epochTimestamp, JsonObject changeJson) {
        return new JsonObject()
                .put(MaxwellBinlogKeys.DATABASE, changeJson.getString("schema"))
                .put(MaxwellBinlogKeys.TABLE, changeJson.getString("table"))
                .put(MaxwellBinlogKeys.TYPE, "insert")
                .put(MaxwellBinlogKeys.TS, epochTimestamp)
                .put(MaxwellBinlogKeys.XID, transactionId)
                .put(MaxwellBinlogKeys.COMMIT, true)
                .put(MaxwellBinlogKeys.DATA, getData(changeJson));
    }

    private JsonObject getData(JsonObject changeJson) {

        final JsonArray columnNames = changeJson.getJsonArray("columnnames");
        final JsonArray columnValues = changeJson.getJsonArray("columnvalues");
        final JsonObject dataJson = new JsonObject();

        for (int i = 0; i < columnNames.size(); i++) {
            dataJson.put(columnNames.getString(i), columnValues.getValue(i));
        }
        return dataJson;
    }

    private JsonObject transformUpdate(Long transactionId, long epochTimestamp, JsonObject changeJson) {
        final JsonObject data = getData(changeJson);
        final JsonObject oldData = getChangedOldData(changeJson, data);

        return new JsonObject()
                .put(MaxwellBinlogKeys.DATABASE, changeJson.getString("schema"))
                .put(MaxwellBinlogKeys.TABLE, changeJson.getString("table"))
                .put(MaxwellBinlogKeys.TYPE, "update")
                .put(MaxwellBinlogKeys.TS, epochTimestamp)
                .put(MaxwellBinlogKeys.XID, transactionId)
                .put(MaxwellBinlogKeys.COMMIT, true)
                .put(MaxwellBinlogKeys.DATA, data)
                .put(MaxwellBinlogKeys.OLD, oldData);
    }

    private JsonObject getChangedOldData(JsonObject changeJson, JsonObject data) {
        final JsonArray keyNames = changeJson.getJsonObject("oldkeys").getJsonArray("keynames");
        final JsonArray keyValues = changeJson.getJsonObject("oldkeys").getJsonArray("keyvalues");

        final JsonObject oldJson = new JsonObject();
        for (int i = 0; i < keyNames.size(); i++) {
            final String keyName = keyNames.getString(i);
            final Object value = keyValues.getValue(i);

            if (!Objects.equals(data.getValue(keyName), value)) {
                oldJson.put(keyName, value);
            }
        }
        return oldJson;
    }

    private JsonObject getOldData(JsonObject changeJson) {
        final JsonArray keyNames = changeJson.getJsonObject("oldkeys").getJsonArray("keynames");
        final JsonArray keyValues = changeJson.getJsonObject("oldkeys").getJsonArray("keyvalues");

        final JsonObject oldJson = new JsonObject();
        for (int i = 0; i < keyNames.size(); i++) {
            final String keyName = keyNames.getString(i);
            final Object value = keyValues.getValue(i);
             oldJson.put(keyName, value);
        }
        return oldJson;
    }

    private JsonObject transformDelete(Long transactionId, long epochTimestamp, JsonObject changeJson) {
        final JsonObject oldDataJSON = getOldData(changeJson);

        return new JsonObject()
                .put(MaxwellBinlogKeys.DATABASE, changeJson.getString("schema"))
                .put(MaxwellBinlogKeys.TABLE, changeJson.getString("table"))
                .put(MaxwellBinlogKeys.TYPE, "delete")
                .put(MaxwellBinlogKeys.TS, epochTimestamp)
                .put(MaxwellBinlogKeys.XID, transactionId)
                .put(MaxwellBinlogKeys.COMMIT, true)
                .put(MaxwellBinlogKeys.DATA, oldDataJSON);
    }
}
