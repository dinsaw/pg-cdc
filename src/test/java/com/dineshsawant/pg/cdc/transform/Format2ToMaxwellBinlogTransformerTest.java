package com.dineshsawant.pg.cdc.transform;

import com.google.common.flogger.FluentLogger;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class Format2ToMaxwellBinlogTransformerTest {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private BinlogTransformer binlogTransformer = new Format2ToMaxwellBinlogTransformer();

    @Test
    public void transformInsert() {
        logger.atSevere().log(String.format("System Property = %s", System.getProperty("user.timezone")));

        final JsonObject transformedBinlogJson = binlogTransformer.transform(new JsonObject("{\n" +
                "  \"action\": \"I\",\n" +
                "  \"xid\": 694,\n" +
                "  \"timestamp\": \"2020-02-25 17:40:06.754641+05:30\"," +
                "  \"schema\": \"public\",\n" +
                "  \"table\": \"students\",\n" +
                "  \"columns\": [\n" +
                "    {\n" +
                "      \"name\": \"id\",\n" +
                "      \"type\": \"integer\",\n" +
                "      \"value\": 2\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"name\",\n" +
                "      \"type\": \"character varying(255)\",\n" +
                "      \"value\": \"Ak\"\n" +
                "    }\n" +
                "  ]\n" +
                "}")).get(0);

        logger.atInfo().log("transformed %s", transformedBinlogJson.encodePrettily());

        final JsonObject expectedTransformedBinlogJson = new JsonObject("{\n" +
                "  \"database\" : \"public\",\n" +
                "  \"table\" : \"students\",\n" +
                "  \"type\" : \"insert\",\n" +
                "  \"ts\" : 1582652406754,\n" +
                "  \"xid\" : 694,\n" +
                "  \"commit\" : true,\n" +
                "  \"data\" : {\n" +
                "    \"id\" : 2,\n" +
                "    \"name\" : \"Ak\"\n" +
                "  }\n" +
                "}");

        Assert.assertEquals(expectedTransformedBinlogJson, transformedBinlogJson);
    }

    @Test
    public void transformUpdate() {

        final JsonObject transformedBinlogJson = binlogTransformer.transform(new JsonObject("{\n" +
                "  \"action\": \"U\",\n" +
                "  \"xid\": 737,\n" +
                "  \"timestamp\": \"2020-02-25 17:40:06.754641+05:30\",\n" +
                "  \"schema\": \"public\",\n" +
                "  \"table\": \"table_with_pk\",\n" +
                "  \"columns\": [\n" +
                "    {\n" +
                "      \"name\": \"a\",\n" +
                "      \"type\": \"integer\",\n" +
                "      \"value\": 26\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"b\",\n" +
                "      \"type\": \"character varying(30)\",\n" +
                "      \"value\": \"test1001\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"c\",\n" +
                "      \"type\": \"timestamp without time zone\",\n" +
                "      \"value\": \"2020-02-25 11:36:53.396834\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"identity\": [\n" +
                "    {\n" +
                "      \"name\": \"a\",\n" +
                "      \"type\": \"integer\",\n" +
                "      \"value\": 26\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"b\",\n" +
                "      \"type\": \"character varying(30)\",\n" +
                "      \"value\": \"test100\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"c\",\n" +
                "      \"type\": \"timestamp without time zone\",\n" +
                "      \"value\": \"2020-02-25 11:36:53.396834\"\n" +
                "    }\n" +
                "  ]\n" +
                "}")).get(0);

        logger.atInfo().log("transformed %s", transformedBinlogJson.encodePrettily());

        final JsonObject expectedTransformedBinlogJson = new JsonObject("{\n" +
                "  \"database\": \"public\",\n" +
                "  \"table\": \"table_with_pk\",\n" +
                "  \"type\": \"update\",\n" +
                "  \"ts\": 1582652406754,\n" +
                "  \"xid\": true,\n" +
                "  \"data\": {\n" +
                "    \"a\": 26,\n" +
                "    \"b\": \"test1001\",\n" +
                "    \"c\": \"2020-02-25 11:36:53.396834\"\n" +
                "  },\n" +
                "  \"old\": {\n" +
                "    \"b\": \"test100\"\n" +
                "  }\n" +
                "}");

        Assert.assertEquals(expectedTransformedBinlogJson, transformedBinlogJson);
    }

    @Test
    public void transformDelete() {

        final JsonObject transformedBinlogJson = binlogTransformer.transform(new JsonObject("{\n" +
                "  \"action\": \"D\",\n" +
                "  \"xid\": 738,\n" +
                "  \"timestamp\": \"2020-02-25 18:06:07.35415+05:30\",\n" +
                "  \"schema\": \"public\",\n" +
                "  \"table\": \"table_with_pk\",\n" +
                "  \"identity\": [\n" +
                "    {\n" +
                "      \"name\": \"a\",\n" +
                "      \"type\": \"integer\",\n" +
                "      \"value\": 26\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"b\",\n" +
                "      \"type\": \"character varying(30)\",\n" +
                "      \"value\": \"test1001\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"c\",\n" +
                "      \"type\": \"timestamp without time zone\",\n" +
                "      \"value\": \"2020-02-25 11:36:53.396834\"\n" +
                "    }\n" +
                "  ]\n" +
                "}")).get(0);

        logger.atInfo().log("transformed %s", transformedBinlogJson.encodePrettily());

        final JsonObject expectedTransformedBinlogJson = new JsonObject("{\n" +
                "  \"database\": \"public\",\n" +
                "  \"table\": \"table_with_pk\",\n" +
                "  \"type\": \"delete\",\n" +
                "  \"ts\": 1582653967354,\n" +
                "  \"xid\": 738,\n" +
                "  \"commit\": true,\n" +
                "  \"data\": {\n" +
                "    \"a\": 26,\n" +
                "    \"b\": \"test1001\",\n" +
                "    \"c\": \"2020-02-25 11:36:53.396834\"\n" +
                "  }\n" +
                "}");

        Assert.assertEquals(expectedTransformedBinlogJson, transformedBinlogJson);
    }
}