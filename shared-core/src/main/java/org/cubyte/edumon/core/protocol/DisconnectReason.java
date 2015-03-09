package org.cubyte.edumon.core.protocol;

public enum DisconnectReason
{
    NORMAL_CLOSURE(1000),
    GOING_AWAY(1001),
    PROTOCOL_ERROR(1002),
    INCOMPATIBLE_DATA(1003),
    INCONSISTENT_DATA(1007),
    POLICY_VIOLATION(1008),
    MESSAGE_TOO_BIG(1009),
    MISSING_EXTENSION(1010),
    INTERNAL_ERROR(1011);

    private final int code;
    ;

    private DisconnectReason(int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }
}
