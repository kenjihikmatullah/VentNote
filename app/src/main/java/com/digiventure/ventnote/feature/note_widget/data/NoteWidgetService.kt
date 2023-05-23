package com.digiventure.ventnote.feature.note_widget.data

import com.digiventure.ventnote.data.local.NoteModel

class NoteWidgetService(private val noteDatabase: NoteDatabase) {
    fun getNoteList(): List<NoteModel> = noteDatabase.noteDao().getPlainNotes()
    fun getNote(id: Int): NoteModel = noteDatabase.noteDao().getPlainNoteDetail(id)
}