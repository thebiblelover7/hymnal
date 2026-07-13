package org.sda.hymnal.data.hymnal

val hymnalList = listOf(Hymnals.NewEnglish, Hymnals.OldEnglish, Hymnals.NewSpanish, Hymnals.OldSpanish, Hymnals.Portuguese, Hymnals.Russian)
object Hymnals {
    object NewEnglish: Hymnal("English - New Hymnal (1985)", "new-hymnal-en", "sheets_new_en_")
    object OldEnglish: Hymnal("English - Old Hymnal (1941)", "old-hymnal-en", "")
    object NewSpanish: Hymnal("Español - Nuevo Himnario (2010)", "new-hymnal-es", "sheets_new_es_")
    object OldSpanish: Hymnal("Español - Viejo Himnario (1962)", "old-hymnal-es", "")
    object Portuguese: Hymnal("Português - Novo Hinario (1996)", "new-hymnal-pt", "")
    object Russian: Hymnal("Русский - Гимны Надежды", "new-hymnal-ru", "sheets_ru_")
}