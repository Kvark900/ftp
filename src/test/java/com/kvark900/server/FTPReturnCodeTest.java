package com.kvark900.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FTPReturnCodeTest {

    @Test
    void assertEqualsFTPCodeFromID() {
        FTPReturnCode ftpReturnCode = FTPReturnCode.fromId(227);
        assertEquals(FTPReturnCode.ENTERING_PASSIVE_MODE, ftpReturnCode);
    }

    @Test
    void assertEqualsUnkownErrorFromID() {
        FTPReturnCode ftpReturnCode = FTPReturnCode.fromId(123123);
        assertEquals(FTPReturnCode.UNKNOWN_ERROR, ftpReturnCode);
    }
}