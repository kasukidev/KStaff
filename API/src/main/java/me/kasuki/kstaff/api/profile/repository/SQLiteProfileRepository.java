package me.kasuki.kstaff.api.profile.repository;

import me.kasuki.kstaff.api.constant.KStaffConstant;
import me.kasuki.kstaff.api.database.sqlite.AbstractSQLiteRepository;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;

public class SQLiteProfileRepository extends AbstractSQLiteRepository<UUID> {
    public SQLiteProfileRepository(JavaPlugin instance) {
        super(instance.getDataFolder(), KStaffConstant.SQLITE_DATABASE_NAME, KStaffConstant.PLAYER_TABLE_NAME);

        File dataFolder = instance.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    @Override
    public void close() throws Exception {
    }
}