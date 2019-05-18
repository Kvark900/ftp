package com.kvark900.server;

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

	public static FTPReturnCode fromId(int code)
	{
		for (FTPReturnCode ftpCode : FTPReturnCode.values())
		{
			if (ftpCode.getCode() == code)
			{
				return ftpCode;
			}
		}
		return FTPReturnCode.UNKNOWN_ERROR;
	}

	public int getCode() {
        return code;
    }
}
