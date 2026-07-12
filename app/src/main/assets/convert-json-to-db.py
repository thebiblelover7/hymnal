import json
import sqlite3
import os
import re


connection = sqlite3.connect("hymns.db")
cursor = connection.cursor()

cursor.execute("""
    CREATE TABLE "hymns" (
        "hymnal"	TEXT NOT NULL,
        "number"	INTEGER NOT NULL,
        "title"	TEXT NOT NULL,
        "favorite" INTEGER NOT NULL,
        "sheet_music"	TEXT NOT NULL,
        "text"	TEXT NOT NULL,
        PRIMARY KEY("hymnal","number")
    )
""")

cursor.execute("""
    CREATE TABLE "settings" (
        "id"    INTEGER NOT NULL,
        "hymnal"    TEXT NOT NULL,
        "font_size" REAL NOT NULL,
        PRIMARY KEY("id")
    )
""")

cursor.execute("""
    CREATE TABLE "playlists" (
        "id"    TEXT NOT NULL,
        "name"  TEXT NOT NULL,
        "count" INTEGER NOT NULL,
        PRIMARY KEY("id")
    )
""")

cursor.execute("""
    CREATE TABLE "playlist_hymns" (
        "id"        TEXT NOT NULL,
        "hymnal"    TEXT NOT NULL,
        "number"    INTEGER NOT NULL,
        "playlist"  TEXT NOT NULL,
        "position"  INTEGER NOT NULL,
        PRIMARY KEY("id")
    )
""")

data = []

with open("json/new-hymnal-en.json", "r") as f:
    new_hymnal_en_source = json.load(f)

with open("json/new-hymnal-es.json", "r") as f:
    new_hymnal_es_source = json.load(f)

with open("json/new-hymnal-pt.json", "r") as f:
    new_hymnal_pt_source = json.load(f)

with open("json/new-hymnal-ru.json", "r") as f:
    new_hymnal_ru_source = json.load(f)

with open("json/old-hymnal-en.json", "r") as f:
    old_hymnal_en_source = json.load(f)

with open("json/old-hymnal-es.json", "r") as f:
    old_hymnal_es_source = json.load(f)

sheet_music_list = os.listdir("../res/drawable")

hymnals = [
    ("new-hymnal-en", "sheets_new_en", new_hymnal_en_source), 
    ("new-hymnal-es", "sheets_new_es", new_hymnal_es_source),
    ("new-hymnal-pt", None, new_hymnal_pt_source),
    ("new-hymnal-ru", "sheets_ru", new_hymnal_ru_source),
    ("old-hymnal-en", None, old_hymnal_en_source),
    ("old-hymnal-es", None, old_hymnal_es_source)
]

for hymnal in hymnals:
    for hymn_source in hymnal[2]:
        if hymnal[1] is not None:
            filtered_sheets = list(filter(re.compile(f"{hymnal[1]}_{hymn_source["number"]:03}" + ".*").match, sheet_music_list))
            renamed_sheets = []
            for sheet in filtered_sheets:
                renamed_sheets.append(sheet.replace(".png", ""))
            sheetmusic = json.dumps(renamed_sheets)
        else:
            sheetmusic = "[]"

        hymn = (hymnal[0], hymn_source["number"], hymn_source["title"], False, sheetmusic, hymn_source["content"])
        data.append(hymn)
    
cursor.executemany("INSERT INTO hymns VALUES(?, ?, ?, ?, ?, ?)", data)

settings = [(0, "new-hymnal-en", 1)]
playlists = [("favorites", "Favorites", 0)]
cursor.executemany("INSERT INTO settings VALUES(?, ?, ?)", settings)
cursor.executemany("INSERT INTO playlists VALUES(?, ?, ?)",playlists)
connection.commit()
connection.close()

