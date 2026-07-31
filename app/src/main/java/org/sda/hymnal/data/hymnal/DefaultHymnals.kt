package org.sda.hymnal.data.hymnal


val defaultHymnals = listOf(
    DefaultHymnals.NewEnglish,
    DefaultHymnals.OldEnglish,
    DefaultHymnals.NewSpanish,
    DefaultHymnals.OldSpanish,
    DefaultHymnals.Portuguese,
    DefaultHymnals.Russian
)

object DefaultHymnals {
    object NewEnglish : Hymnal(
        id = "new-hymnal-en",
        fileName = "new-hymnal-en",
        title = "English - New Hymnal (1985)",
        userAdded = false,
        version = 1
    )

    object OldEnglish : Hymnal(
        id = "old-hymnal-en",
        fileName = "old-hymnal-en",
        title = "English - Old Hymnal (1941)",
        userAdded = false,
        version = 1
    )

    object NewSpanish : Hymnal(
        id = "new-hymnal-es",
        fileName = "new-hymnal-es",
        title = "Español - Nuevo Himnario (2010)",
        userAdded = false,
        version = 1
    )

    object OldSpanish : Hymnal(
        id = "old-hymnal-es",
        fileName = "old-hymnal-es",
        title = "Español - Viejo Himnario (1962)",
        userAdded = false,
        version = 1
    )

    object Portuguese : Hymnal(
        id = "new-hymnal-pt",
        fileName = "new-hymnal-pt",
        title = "Português - Novo Hinario (1996)",
        userAdded = false,
        version = 1
    )

    object Russian : Hymnal(
        id = "new-hymnal-ru",
        fileName = "new-hymnal-ru",
        title = "Русский - Гимны Надежды (1997)",
        userAdded = false,
        version = 1
    )
}