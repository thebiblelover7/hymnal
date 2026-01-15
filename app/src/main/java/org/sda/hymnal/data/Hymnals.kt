package org.sda.hymnal.data

val hymnalList = listOf<Hymnal>(Hymnals.NewEnglish, Hymnals.OldEnglish, Hymnals.NewSpanish, Hymnals.OldSpanish, Hymnals.Portuguese, Hymnals.Russian)
object Hymnals {
    object NewEnglish: Hymnal("New Hymnal", "new-hymnal-en.json")
    object OldEnglish: Hymnal("Old Hymnal", "old-hymnal-en.json")
    object NewSpanish: Hymnal("Nuevo Himnario", "new-hymnal-es.json")
    object OldSpanish: Hymnal("Viejo Himnario", "old-hymnal-es.json")
    object Portuguese: Hymnal("Novo Hinario", "new-hymnal-pt.json")
    object Russian: Hymnal("Russian Hymnal", "new-hymnal-ru.json")
}