package com.digiventure.ventnote

import com.digiventure.utils.BaseUnitTest
import com.digiventure.ventnote.data.local.NoteDAO
import com.digiventure.ventnote.data.local.NoteLocalService
import com.digiventure.ventnote.data.local.NoteModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class NoteLocalServiceShould: BaseUnitTest() {
    private val dao: NoteDAO = mock()
    private val noteList = mock<List<NoteModel>>()
    private val note = mock<NoteModel>()

    private val id = 1

    private val detailException = RuntimeException("Failed to get note detail")
    private val listException = RuntimeException("Failed to get list of notes")
    private val deleteException = RuntimeException("Failed to delete list of notes")
    private val updateException = RuntimeException("Failed to update list of notes")

    private lateinit var service: NoteLocalService

    @Before
    fun setup() {
        service = NoteLocalService(dao)
    }

    /**
     * Test suite for getNoteDetail from dao
     * */
    @Test
    fun getNoteDetailFromDAO() = runTest {
        stubSuccessfulGetDetailCase()

        service.getNoteDetail(id).first()

        verify(dao, times(1)).getNoteDetail(id)
    }

    @Test
    fun emitsFlowOfNoteAndEmitsThem() = runTest {
        stubSuccessfulGetDetailCase()

        assertEquals(note, dao.getNoteDetail(id).first())
    }

    @Test
    fun emitsErrorResultWhenGetDetailsFails() = runTest {
        stubErrorGetDetailCase()

        try {
            dao.getNoteDetail(id).first()
        } catch (e: RuntimeException) {
            assertEquals(detailException.message, e.message)
        }
    }

    private fun stubSuccessfulGetDetailCase() {
        runBlocking {
            whenever(dao.getNoteDetail(id)).thenReturn(
                flow {
                    emit(note)
                }
            )
        }
    }

    private fun stubErrorGetDetailCase() {
        runBlocking {
            whenever(dao.getNoteDetail(id)).thenThrow(detailException)
        }
    }

    /**
     * Test suite for getNoteList from dao
     * */
    @Test
    fun getNoteListFromDAO() = runTest {
        stubSuccessfulGetListNoteCase()

        service.getNoteList().first()

        verify(dao, times(1)).getNotes()
    }

    @Test
    fun emitsFlowOfNoteListAndEmitsThem() = runTest {
        stubSuccessfulGetListNoteCase()

        assertEquals(Result.success(noteList), service.getNoteList().first())
    }

    @Test
    fun emitsErrorResultWhenGetNoteListFails() = runTest {
        stubErrorGetListNoteCase()

        try {
            service.getNoteList().first()
        } catch (e: RuntimeException) {
            assertEquals(listException.message, e.message)
        }
    }

    private fun stubSuccessfulGetListNoteCase() {
        runBlocking {
            whenever(dao.getNotes()).thenReturn(
                flow {
                    emit(noteList)
                }
            )
        }
    }

    private fun stubErrorGetListNoteCase() {
        runBlocking {
            whenever(dao.getNotes()).thenThrow(listException)
        }
    }

    /**
     * Test suite for deleteNoteList from dao
     * */
    @Test
    fun deleteNoteListFromDAO() = runTest {
        service.deleteNoteList(note).first()

        verify(dao, times(1)).deleteNotes(note)
    }

    @Test
    fun emitsFlowOfDeletedLengthAndEmitsThem() = runTest {
        runBlocking { whenever(dao.deleteNotes(note)).thenReturn(1) }
        assertEquals(Result.success(true), service.deleteNoteList(note).first())

        runBlocking { whenever(dao.deleteNotes(note)).thenReturn(0) }
        assertEquals(Result.success(false), service.deleteNoteList(note).first())
    }

    @Test
    fun emitsErrorWhenDeletionFails() = runTest {
        runBlocking {
            whenever(dao.deleteNotes(note)).thenThrow(deleteException)
        }

        assertEquals(
            deleteException.message,
            service.deleteNoteList(note).first().exceptionOrNull()?.message
        )
    }

    /**
     * Test suite for updateNote from dao
     * */
    @Test
    fun updateNoteFromDAO() = runTest {
        service.updateNoteList(note).first()

        verify(dao, times(1)).updateNote(note)
    }

    @Test
    fun emitsFlowOfUpdateLengthAndEmitsThem() = runTest {
        runBlocking { whenever(dao.updateNote(note)).thenReturn(1) }
        assertEquals(Result.success(true), service.updateNoteList(note).first())

        runBlocking { whenever(dao.updateNote(note)).thenReturn(0) }
        assertEquals(Result.success(false), service.updateNoteList(note).first())
    }

    @Test
    fun emitsErrorWhenUpdateFails() = runTest {
        runBlocking {
            whenever(dao.updateNote(note)).thenThrow(updateException)
        }

        assertEquals(
            updateException.message,
            service.updateNoteList(note).first().exceptionOrNull()?.message
        )
    }
}