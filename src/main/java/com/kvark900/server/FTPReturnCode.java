package com.kvark900.server;

import java.util.Arrays;
import java.util.Optional;

/**
 * Server's response codes
 */
public enum FTPReturnCode {
    ENTERING_PASSIVE_MODE(227),
    LOGGED_IN(230),
    NEED_PASSWORD(331),
    NOT_LOGGED_IN(530),
    FILE_UNAVAILABLE(550),
    FILE_NAME_NOT_ALLOWED(553),
    CONNECTION_CLOSED(426),
    UNKNOWN_ERROR(999);

    private final int code;

    FTPReturnCode(int code) {
        this.code = code;
    }

    public static FTPReturnCode fromId(int code) {
        Optional<FTPReturnCode> r = Arrays.stream(FTPReturnCode.values()).filter(c -> c.code == code).findFirst();
        return r.orElse(FTPReturnCode.UNKNOWN_ERROR);
    }

    public int getCode() {
        return code;
    }
}
