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
    fun emitsFlowOfNoteResultThem() = runTest {
        stubSuccessfulGetDetailCase()

        Assert.assertEquals(note, dao.getNoteDetail(id).first())
    }

    @Test
    fun emitsErrorResultWhenGetDetailsFails() = runTest {
        stubErrorGetDetailCase()

        try {
            dao.getNoteDetail(id).first()
        } catch (e: RuntimeException) {
            Assert.assertEquals("Failed to get note detail", e.message)
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
    fun convertValuesToFlowResultAndEmitsThem() = runTest {
        stubSuccessfulGetListNoteCase()

        Assert.assertEquals(Result.success(noteList), service.getNoteList().first())
    }

    @Test
    fun emitsErrorResultWhenFails() = runTest {
        stubErrorGetListNoteCase()

        Assert.assertEquals(
            "Failed to get list of notes",
            service.getNoteList().first().exceptionOrNull()?.message
        )
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
            whenever(dao.getNoteDetail(id)).thenThrow(listException)
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
    fun convertResultToFLowAndEmitsThemAfterDeleteNoteList() = runTest {
        runBlocking { whenever(dao.deleteNotes(note)).thenReturn(1) }
        Assert.assertEquals(Result.success(true), service.deleteNoteList(note).first())

        runBlocking { whenever(dao.deleteNotes(note)).thenReturn(0) }
        Assert.assertEquals(Result.success(false), service.deleteNoteList(note).first())
    }

    @Test
    fun emitsErrorWhenDeletionFails() = runTest {
        stubErrorDeleteListNoteCase()

        Assert.assertEquals(
            "Failed to delete list of notes",
            service.deleteNoteList(note).first().exceptionOrNull()?.message
        )
    }

    private fun stubSuccessfulDeleteListNoteCase() {
        runBlocking {
            whenever(dao.getNotes()).thenReturn(
                flow {
                    emit(noteList)
                }
            )
        }
    }

    private fun stubErrorDeleteListNoteCase() {
        runBlocking {
            whenever(dao.deleteNotes(note)).thenThrow(deleteException)
        }
    }
}