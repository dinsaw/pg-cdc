package com.dineshsawant.pg.cdc.producer;

import com.google.common.flogger.FluentLogger;
import com.dineshsawant.pg.cdc.config.Configuration;
import com.dineshsawant.pg.cdc.exception.PgCDCException;
import io.vertx.core.json.JsonObject;
import okhttp3.*;

import java.io.IOException;

public class WebhookCDCProducer extends AbstractCDCProducer {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private OkHttpClient httpClient = new OkHttpClient();
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");



    public WebhookCDCProducer(Configuration configuration) {
        super(configuration);
    }

    @Override
    public void pushTransaction(JsonObject binlogTransactionJson) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void pushRow(JsonObject binlogRowJson) {
        logger.atInfo().every(100).log("Sending jsonObject %s", binlogRowJson);

        RequestBody body = RequestBody.create(binlogRowJson.toString(), JSON);
        Request request = new Request.Builder()
                .url(configuration.getWebhookUrl())
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (200 == response.code()) {
                logger.atInfo().every(100).log("Sent log successfully");
            } else {
                logger.atWarning().log("Unable to send log. Failed with status %s", response.code());
                throw new PgCDCException("Failed to send data over webhook. Shutting down");
            }
        } catch (IOException e) {
            throw new PgCDCException("Failed to send data over webhook. Should Shutting down", e);
        }
    }
}
