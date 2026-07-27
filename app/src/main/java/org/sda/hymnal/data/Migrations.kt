package org.sda.hymnal.data

import androidx.room3.migration.AutoMigrationSpec
import androidx.sqlite.SQLiteConnection
import org.sda.hymnal.data.hymn.hymnTagsExpression

abstract class Migrations {

    class AutoMigrationRebuildFts : AutoMigrationSpec {
        override suspend fun onPostMigrate(connection: SQLiteConnection) {
            super.onPostMigrate(connection)
            connection.prepare("INSERT INTO hymns_fts(hymns_fts) VALUES('rebuild')").use { statement ->
                statement.step()
            }
        }
    }
    class AutoMigration2To3 : AutoMigrationSpec {
        override suspend fun onPostMigrate(connection: SQLiteConnection) {
            super.onPostMigrate(connection)
            // This is a very complicated statement to add first lines from the text into a new column
            connection.prepare("""
                UPDATE hymns
                SET first_line = (
                	SELECT
                		REPLACE(
                			CASE """
                    + hymnTagsExpression
                    + """
                        THEN
                					TRIM(SUBSTR(
                						SUBSTR(text, INSTR(text || CHAR(10), CHAR(10)) + 1), 1, 50)
                						)
                				ELSE
                					TRIM(SUBSTR(text, 1, 50))
                			END,
                			CHAR(10),
                			' '
                		)
                	FROM (
                		SELECT
                			TRIM(SUBSTR(text, 1, INSTR(text || CHAR(10), CHAR(10)) - 1)) AS line1
                	)
                )
            """.trimIndent()).use { statement ->
                statement.step()
            }
        }
    }
}