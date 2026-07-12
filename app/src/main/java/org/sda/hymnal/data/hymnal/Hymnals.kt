package org.sda.hymnal.data.hymnal

val hymnalList = listOf(Hymnals.NewEnglish, Hymnals.OldEnglish, Hymnals.NewSpanish, Hymnals.OldSpanish, Hymnals.Portuguese, Hymnals.Russian)
object Hymnals {
    object NewEnglish: Hymnal("English - New Hymnal", "new-hymnal-en", "sheets_new_en_")
    object OldEnglish: Hymnal("English - Old Hymnal", "old-hymnal-en", "")
    object NewSpanish: Hymnal("Español - Nuevo Himnario", "new-hymnal-es", "sheets_new_es_")
    object OldSpanish: Hymnal("Español - Viejo Himnario", "old-hymnal-es", "")
    object Portuguese: Hymnal("Português - Novo Hinario", "new-hymnal-pt", "")
    object Russian: Hymnal("Russian - Hymnal", "new-hymnal-ru", "sheets_ru_")
}