package cn.x302.exts

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import java.sql.Connection
import java.sql.DriverManager


/**
 * Makes a connection to a Postgres database.
 *
 * In order to connect to your running Postgres process,
 * please specify the following parameters in your configuration file:
 * - postgres.url -- Url of your running database process.
 * - postgres.user -- Username for database connection
 * - postgres.password -- Password for database connection
 *
 * If you don't have a database process running yet, you may need to [download]((https://www.postgresql.org/download/))
 * and install Postgres and follow the instructions [here](https://postgresapp.com/).
 * Then, you would be able to edit your url,  which is usually "jdbc:postgresql://host:port/database", as well as
 * user and password values.
 *
 *
 * @param embedded -- if [true] defaults to an embedded database for tests that runs locally in the same process.
 * In this case you don't have to provide any parameters in configuration file, and you don't have to run a process.
 *
 * @return [Connection] that represent connection to the database. Please, don't forget to close this connection when
 * your application shuts down by calling [Connection.close]
 * */
fun Application.getConnection(embedded: Boolean = true): Connection {
    Class.forName("org.h2.Driver")
    Class.forName("org.postgresql.Driver")
    Class.forName("org.sqlite.JDBC")
    if (embedded) {
        return DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "", "")
    } else {
        val url = environment.config.property("db.url").getString()
        val user = environment.config.property("db.user").getString()
        val password = environment.config.property("db.password").getString()

        return DriverManager.getConnection(url, user, password)
    }
}

fun Application.getDatabase(embedded: Boolean = true): Database {
    Class.forName("org.h2.Driver")
    Class.forName("org.postgresql.Driver")
    Class.forName("org.sqlite.JDBC")
    if (embedded) {
        return Database.connect(url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", user = "", password = "")
    } else {
        val url = environment.config.property("db.url").getString()
        val user = environment.config.property("db.user").getString()
        val password = environment.config.property("db.password").getString()

        return Database.connect(url = url, user = user, password = password)
    }
}
