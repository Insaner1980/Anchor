package com.finnvek.anchor.domain.model

enum class ResourceType {
    SONG,
    PHOTO,
}

enum class TagCategory {
    FEELING,
    NEED,
}

enum class TagRole {
    FEELING_THEN,
    HELPS_WITH,
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    CALM,
}

enum class LibraryContentType {
    ALL,
    SONGS,
    PHOTOS,
}

enum class LibraryViewMode {
    LIBRARY,
    TIMELINE,
}
