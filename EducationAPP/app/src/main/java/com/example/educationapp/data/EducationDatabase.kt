package com.example.educationapp.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import android.content.Context
import com.example.educationapp.data.dao.*

class Converters {
    @TypeConverter
    fun fromUserType(value: UserType): String {
        return value.name
    }

    @TypeConverter
    fun toUserType(value: String): UserType {
        return UserType.valueOf(value)
    }
    
    @TypeConverter
    fun fromBehaviorType(value: BehaviorType): String {
        return value.name
    }

    @TypeConverter
    fun toBehaviorType(value: String): BehaviorType {
        return BehaviorType.valueOf(value)
    }
}

@Database(
    entities = [
        User::class, 
        LearningRecord::class, 
        Resource::class, 
        Recommendation::class,
        LearningProgress::class,
        LearningStatistics::class,
        LearningBehavior::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class EducationDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun learningRecordDao(): LearningRecordDao
    abstract fun resourceDao(): ResourceDao
    abstract fun recommendationDao(): RecommendationDao
    abstract fun learningProgressDao(): LearningProgressDao

    companion object {
        @Volatile
        private var INSTANCE: EducationDatabase? = null

        fun getDatabase(context: Context): EducationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EducationDatabase::class.java,
                    "education_database"
                )
                .fallbackToDestructiveMigration() // 开发阶段允许破坏性迁移
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
