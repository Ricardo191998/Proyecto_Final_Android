package com.example.revision_app.room.db
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.revision_app.retrofit.Constants
import com.example.revision_app.room.goal.data.GoalDao
import com.example.revision_app.room.goal.data.model.GoalEntity
import com.example.revision_app.room.revision.data.RevisionDao
import com.example.revision_app.room.revision.data.model.RevisionEntity
import com.example.revision_app.room.user.data.UserDao
import com.example.revision_app.room.user.data.model.UserEntity

@Database(
    entities = [GoalEntity::class, UserEntity::class, RevisionEntity::class],
    version = 1
)
abstract class RevisionDatabase : RoomDatabase() {

    abstract fun goalDao() : GoalDao
    abstract fun userDao() : UserDao
    abstract fun revisionDao(): RevisionDao

    // Sin inyección de dependencias patron singleton
    companion object{
        @Volatile //lo que se escriba en este campo, será inmediatamente visible a otros hilos
        private var INSTANCE: RevisionDatabase? = null

        fun getDatabase(context: Context): RevisionDatabase{
            return INSTANCE?: synchronized(this){
                //Si la instancia no es nula, entonces se regresa
                // si es nula, entonces se crea la base de datos (patrón singleton)
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RevisionDatabase::class.java,
                    Constants.DATABASE_NAME
                ).fallbackToDestructiveMigration() //Permite a Room recrear las tablas de la BD si las migraciones no se encuentran
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }

}