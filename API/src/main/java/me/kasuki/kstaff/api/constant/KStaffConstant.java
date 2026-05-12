package me.kasuki.kstaff.api.constant;

import lombok.experimental.UtilityClass;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Holds shared constants used across KStaff modules.
 */
@UtilityClass
public class KStaffConstant {

    /**
     * Constant for charset.
     */
    public final Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * Constant for sqlite database name.
     */
    public final String SQLITE_DATABASE_NAME = "kstaff-database";
    public final String PLAYER_TABLE_NAME = "profiles";

    /**
     * Constant for redis streams
     */
    public final String STAFF_CHAT_REDIS_KEY = "kstaff-staffchat";
}
