import json
import sqlite3
import os
import re


connection = sqlite3.connect("hymns.db")
cursor = connection.cursor()

res = cursor.execute("SELECT name FROM sqlite_master")
if not "hymns" in res.fetchall():
    cursor.execute("CREATE TABLE hymns(hymnal, num, title, sheetmusic, text)")

data = []

with open("new-hymnal-en.json", "r") as f:
    new_hymnal_en_source = json.load(f)

with open("new-hymnal-es.json", "r") as f:
    new_hymnal_es_source = json.load(f)

with open("new-hymnal-pt.json", "r") as f:
    new_hymnal_pt_source = json.load(f)

with open("new-hymnal-ru.json", "r") as f:
    new_hymnal_ru_source = json.load(f)

with open("old-hymnal-en.json", "r") as f:
    old_hymnal_en_source = json.load(f)

with open("old-hymnal-es.json", "r") as f:
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
            sheetmusic = json.dumps(filtered_sheets)
        else:
            sheetmusic = "[]"

        hymn = (hymnal[0], hymn_source["number"], hymn_source["title"], sheetmusic, hymn_source["content"])
        data.append(hymn)
    
cursor.executemany("INSERT INTO hymns VALUES(?, ?, ?, ?, ?)", data)

connection.commit()
connection.close()

