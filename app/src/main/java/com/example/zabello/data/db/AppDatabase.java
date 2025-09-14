package com.example.zabello.data.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.zabello.data.dao.AlertRuleDao;
import com.example.zabello.data.dao.ArticleDao;
import com.example.zabello.data.dao.ChronicConditionDao;
import com.example.zabello.data.dao.ParameterEntryDao;
import com.example.zabello.data.dao.ParameterTypeDao;
import com.example.zabello.data.dao.SurgeryDao;
import com.example.zabello.data.dao.UserDao;
import com.example.zabello.data.db.converters.DateConverters;
import com.example.zabello.data.db.converters.EnumConverters;
import com.example.zabello.data.entity.AlertRule;
import com.example.zabello.data.entity.Article;
import com.example.zabello.data.entity.ChronicCondition;
import com.example.zabello.data.entity.ParameterEntry;
import com.example.zabello.data.entity.ParameterType;
import com.example.zabello.data.entity.Surgery;
import com.example.zabello.data.entity.User;

@Database(
        entities = {
                User.class,
                ParameterType.class,
                ParameterEntry.class,
                Article.class,
                ChronicCondition.class,
                Surgery.class,
                AlertRule.class
        },
        version = 2,
        exportSchema = true
)
@TypeConverters({DateConverters.class, EnumConverters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    // DAO getters
    public abstract UserDao userDao();
    public abstract ParameterTypeDao parameterTypeDao();
    public abstract ParameterEntryDao parameterEntryDao();
    public abstract ArticleDao articleDao();
    public abstract ChronicConditionDao chronicConditionDao();
    public abstract SurgeryDao surgeryDao();
    public abstract AlertRuleDao alertRuleDao();

    // Migration 1 -> 2: добавляем все доменные таблицы сверх users
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // parameter_types
            db.execSQL("CREATE TABLE IF NOT EXISTS `parameter_types` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`code` TEXT NOT NULL, `title` TEXT NOT NULL, `unit` TEXT, " +
                    "`minNormal` REAL, `maxNormal` REAL, `description` TEXT)");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_parameter_types_code` ON `parameter_types` (`code`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_parameter_types_title` ON `parameter_types` (`title`)");

            // parameter_entries
            db.execSQL("CREATE TABLE IF NOT EXISTS `parameter_entries` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`userId` INTEGER NOT NULL, `typeId` INTEGER NOT NULL, " +
                    "`value` REAL NOT NULL, `timestamp` INTEGER, `note` TEXT, " +
                    "FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                    "FOREIGN KEY(`typeId`) REFERENCES `parameter_types`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_parameter_entries_userId` ON `parameter_entries` (`userId`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_parameter_entries_typeId` ON `parameter_entries` (`typeId`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_parameter_entries_timestamp` ON `parameter_entries` (`timestamp`)");

            // articles
            db.execSQL("CREATE TABLE IF NOT EXISTS `articles` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`slug` TEXT NOT NULL, `title` TEXT NOT NULL, `body` TEXT NOT NULL, " +
                    "`tags` TEXT, `createdAt` INTEGER)");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_articles_slug` ON `articles` (`slug`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_articles_title` ON `articles` (`title`)");

            // chronic_conditions
            db.execSQL("CREATE TABLE IF NOT EXISTS `chronic_conditions` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` INTEGER NOT NULL, " +
                    "`name` TEXT NOT NULL, `notes` TEXT, `diagnosedAt` INTEGER, " +
                    "FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_chronic_conditions_userId` ON `chronic_conditions` (`userId`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_chronic_conditions_name` ON `chronic_conditions` (`name`)");

            // surgeries
            db.execSQL("CREATE TABLE IF NOT EXISTS `surgeries` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` INTEGER NOT NULL, " +
                    "`name` TEXT NOT NULL, `notes` TEXT, `date` INTEGER, " +
                    "FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_surgeries_userId` ON `surgeries` (`userId`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_surgeries_date` ON `surgeries` (`date`)");

            // alert_rules
            db.execSQL("CREATE TABLE IF NOT EXISTS `alert_rules` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`userId` INTEGER, `parameterTypeId` INTEGER NOT NULL, " +
                    "`lowThreshold` REAL, `highThreshold` REAL, `enabled` INTEGER NOT NULL, `message` TEXT, " +
                    "FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                    "FOREIGN KEY(`parameterTypeId`) REFERENCES `parameter_types`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_alert_rules_parameterTypeId` ON `alert_rules` (`parameterTypeId`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_alert_rules_userId` ON `alert_rules` (`userId`)");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "zabello.db"
                            )
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
