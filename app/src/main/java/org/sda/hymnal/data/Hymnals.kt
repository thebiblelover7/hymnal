package org.sda.hymnal.data

val hymnalList = listOf<Hymnal>(Hymnals.NewEnglish, Hymnals.OldEnglish, Hymnals.NewSpanish, Hymnals.OldSpanish, Hymnals.Portuguese, Hymnals.Russian)
object Hymnals {
    object NewEnglish: Hymnal("English - New Hymnal", "new-hymnal-en.json", "sheets_new_en_")
    object OldEnglish: Hymnal("English - Old Hymnal", "old-hymnal-en.json", "")
    object NewSpanish: Hymnal("Español - Nuevo Himnario", "new-hymnal-es.json", "sheets_new_es_")
    object OldSpanish: Hymnal("Español - Viejo Himnario", "old-hymnal-es.json", "")
    object Portuguese: Hymnal("Português - Novo Hinario", "new-hymnal-pt.json", "")
    object Russian: Hymnal("Russian - Hymnal", "new-hymnal-ru.json", "sheets_ru_")
}