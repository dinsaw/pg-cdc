package com.dineshsawant.pg.cdc.exception;

import org.postgresql.util.PSQLException;

public class PgCDCException extends RuntimeException {
    public PgCDCException(PSQLException pse) {
        super("Postgres Exception occurred", pse);
    }

    public PgCDCException(String msg) {
        super(msg);
    }

    public PgCDCException(String msg, Exception e) {
        super(msg, e);
    }
}
