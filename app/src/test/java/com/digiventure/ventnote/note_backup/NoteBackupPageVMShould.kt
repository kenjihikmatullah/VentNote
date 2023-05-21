package com.digiventure.ventnote.note_backup

import com.digiventure.utils.BaseUnitTest
import com.digiventure.utils.captureValues
import com.digiventure.utils.getValueForTest
import com.digiventure.ventnote.data.NoteRepository
import com.digiventure.ventnote.data.data_store.DataStoreHelper
import com.digiventure.ventnote.feature.note_backup.viewmodel.NoteBackupPageVM
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class NoteBackupPageVMShould: BaseUnitTest() {
    private val repository: NoteRepository = mock()
    private val googleSignInClient: GoogleSignInClient = mock()
    private val googleCredential: GoogleAccountCredential = mock()
    private val dataStoreHelper: DataStoreHelper = mock()

    private lateinit var viewModel: NoteBackupPageVM

    private val expectedUpload = Result.success(Unit)
    private val exceptionUpload = Exception("Failed to Upload the DB")

    private val expectedSynchronized = Result.success(Unit)
    private val exceptionSynchronized = Exception("Failed to Sync the DB")

    private val expectedLogoutResult: Task<Void> = mock()
    private val expectedLogout = Result.success(Unit)
    private val exceptionLogout = Exception("Failed to Logout")

    private val intKey = "maxSyncAttemptKey"
    private val savedDayKey = "savedDay"
    private val intValue = 4
    private val longValue = 1000L

    @Before
    fun setup() {
        viewModel = NoteBackupPageVM(repository, googleSignInClient, dataStoreHelper)
    }

    /**
     * Test suite for backup db to google drive
     * */
    @Test
    fun uploadDBtoGDriveFromRepository() = runTest {
        mockSuccessfulBackupDBCase()

        val result = viewModel.backupDB(googleCredential)

        verify(repository, times(1)).uploadDBtoDrive(googleCredential)
        assertEquals(expectedUpload, result)
    }

    @Test
    fun emitsErrorWhenUploadDBtoGDriveReceiveError() = runTest {
        mockFailedBackupDBCase()

        val result = viewModel.backupDB(googleCredential)

        assertEquals(exceptionUpload.message, result.exceptionOrNull()?.message)
    }

    @Test
    fun showLoaderWhileBackupDB() = runTest {
        mockSuccessfulBackupDBCase()

        viewModel.loader.captureValues {
            viewModel.backupDB(googleCredential)

            assertEquals(true, values.first())
        }
    }

    @Test
    fun closeLoaderAfterBackupDBSuccessOrError() = runTest {
        mockSuccessfulBackupDBCase()

        viewModel.loader.captureValues {
            viewModel.backupDB(googleCredential)

            assertEquals(false, values.last())
        }

        mockFailedBackupDBCase()

        viewModel.loader.captureValues {
            viewModel.backupDB(googleCredential)

            assertEquals(false, values.last())
        }
    }

    private fun mockSuccessfulBackupDBCase() = runBlocking {
        whenever(repository.uploadDBtoDrive(googleCredential)).thenReturn(
            flowOf(expectedUpload)
        )
    }

    private fun mockFailedBackupDBCase() = runBlocking {
        whenever(repository.uploadDBtoDrive(googleCredential)).thenReturn(
            flowOf(Result.failure(exceptionUpload))
        )
    }

    /**
     * Test suite for sync db to google drive
     * */
    @Test
    fun syncDBFromGDriveFromRepository() = runTest {
        mockSuccessfulSyncDBCase()

        val result = viewModel.syncDB(googleCredential)

        verify(repository, times(1)).syncDBFromDrive(googleCredential)
        assertEquals(expectedUpload, result)
    }

    @Test
    fun emitsErrorWhenSyncDBFromGDriveReceiveError() = runTest {
        mockFailedSyncDBCase()

        val result = viewModel.syncDB(googleCredential)

        assertEquals(exceptionSynchronized.message, result.exceptionOrNull()?.message)
    }

    @Test
    fun showLoaderWhileSyncDB() = runTest {
        mockSuccessfulSyncDBCase()

        viewModel.loader.captureValues {
            viewModel.syncDB(googleCredential)

            assertEquals(true, values.first())
        }
    }

    @Test
    fun closeLoaderAfterSyncDBSuccessOrError() = runTest {
        mockSuccessfulSyncDBCase()

        viewModel.loader.captureValues {
            viewModel.syncDB(googleCredential)

            assertEquals(false, values.last())
        }

        mockFailedSyncDBCase()

        viewModel.loader.captureValues {
            viewModel.syncDB(googleCredential)

            assertEquals(false, values.last())
        }
    }

    private fun mockSuccessfulSyncDBCase() = runBlocking {
        whenever(repository.syncDBFromDrive(googleCredential)).thenReturn(
            flowOf(expectedSynchronized)
        )
    }

    private fun mockFailedSyncDBCase() = runBlocking {
        whenever(repository.syncDBFromDrive(googleCredential)).thenReturn(
            flowOf(Result.failure(exceptionSynchronized))
        )
    }

    /**
     * Test suite for logout from device
     * */
    @Test
    fun verifyCallSignOutFromGoogleSignInClient() = runTest {
        mockSuccessfulLogoutCase()

        val result = viewModel.logout()

        verify(googleSignInClient, times(1)).signOut()
        assertEquals(expectedLogout, result)
    }

    @Test
    fun emitsErrorWhenLogoutReceiveError() = runTest {
        mockFailedLogoutCase()

        try {
            viewModel.logout()
        } catch (e: Exception) {
            assertEquals(exceptionUpload.message, e.message)
        }
    }

    @Test
    fun showLoaderWhileLogout() = runTest {
        mockSuccessfulLogoutCase()

        viewModel.loader.captureValues {
            viewModel.backupDB(googleCredential)

            assertEquals(true, values.first())
        }
    }

    @Test
    fun clearGoogleAccountStateWhenSuccessfulLogout() = runTest {
        mockSuccessfulLogoutCase()

        viewModel.googleAccount.captureValues {
            viewModel.logout()

            assertEquals(null, values.first())
        }
    }

    @Test
    fun closeLoaderAfterLogoutSuccessOrError() = runTest {
        mockSuccessfulLogoutCase()

        viewModel.loader.captureValues {
            viewModel.logout()

            assertEquals(false, values.last())
        }

        mockFailedLogoutCase()

        viewModel.loader.captureValues {
            viewModel.logout()

            assertEquals(false, values.last())
        }
    }

    private fun mockSuccessfulLogoutCase() = runBlocking {
        whenever(googleSignInClient.signOut()).thenReturn(expectedLogoutResult)
    }

    private fun mockFailedLogoutCase() = runBlocking {
        whenever(googleSignInClient.signOut()).thenAnswer { throw exceptionLogout }
    }

    /**
     * Test suite for SetMaxSyncAttempt
     * */
    @Test
    fun verifySetMaxSyncAttemptCallSetIntDataFromDataStoreHelper() = runTest {
        val expectedResult = Result.success(Unit)

        whenever(dataStoreHelper.setIntData(intKey, intValue)).thenAnswer { }

        val result = viewModel.setMaxSyncAttempt(intValue)

        assertEquals(expectedResult, result)
        verify(dataStoreHelper, times(1)).setIntData(intKey, intValue)
    }

    @Test
    fun assertSetMaxSyncAttemptError() = runTest {
        val exception = Exception("Failed to set int")

        whenever(dataStoreHelper.setIntData(intKey, intValue)).thenAnswer { throw exception }

        val result = viewModel.setMaxSyncAttempt(intValue)

        assertEquals(Result.failure<Unit>(exception), result)
    }

    /**
     * Test suite for setSavedDay
     * */
    @Test
    fun setSavedDayWillCallDataStoreHelperWhenElapsedTimeIsEmpty() = runTest {
        val previousDay = 0L

        val result = viewModel.setSavedDay(previousDay, longValue)

        assertTrue(result.isSuccess)
        verify(dataStoreHelper, times(1)).setLongData(savedDayKey, longValue)
        verify(dataStoreHelper, times(1)).setIntData(intKey, intValue)
    }

    @Test
    fun setSavedDayWillCallDataStoreHelperWhenElapsedTimeIsMoreThanOneHour() = runTest {
        val previousDay = System.currentTimeMillis() - 2 * 60 * 60 * 1000

        val result = viewModel.setSavedDay(previousDay, longValue)

        assertTrue(result.isSuccess)
        verify(dataStoreHelper, times(1)).setLongData(savedDayKey, longValue)
        verify(dataStoreHelper, times(1)).setIntData(intKey, intValue)
    }

    @Test
    fun setSavedDayNotCallDataStoreHelperWhenElapsedTimeIsLessThanOneHour() = runTest {
        val previousDay = System.currentTimeMillis() - 30 * 60 * 1000

        val result = viewModel.setSavedDay(previousDay, longValue)

        assertTrue(result.isSuccess)
        verify(dataStoreHelper, never()).setLongData(savedDayKey, longValue)
        verify(dataStoreHelper, never()).setIntData(intKey, intValue)
    }

    @Test
    fun setSavedDayReturnsFailureWhenAnExceptionOccursDuringDataStoreUpdate() = runTest {
        val previousDay = 0L

        whenever(dataStoreHelper.setLongData(savedDayKey, longValue)).thenAnswer {
            throw Exception("Data store update failed")
        }

        val result = viewModel.setSavedDay(previousDay, longValue)

        assertTrue(result.isFailure)
        verify(dataStoreHelper, times(1)).setLongData(savedDayKey, longValue)
        verify(dataStoreHelper, never()).setIntData(intKey, intValue)
    }

    @Test
    fun savedDayEmitsExpectedValueFromDataStoreHelper() = runTest {
        val expectedSavedDay = 123456789L
        whenever(dataStoreHelper.getLongData(savedDayKey)).thenReturn(flowOf(expectedSavedDay))

        val savedDay = viewModel.savedDay.getValueForTest()

        assertEquals(savedDay, expectedSavedDay)
    }

    @Test
    fun maxAttemptsEmitsExpectedValueFromDataStoreHelper() = runTest {
        val expectedMaxAttempts = 4
        whenever(dataStoreHelper.getIntData(intKey)).thenReturn(flowOf(expectedMaxAttempts))

        val maxAttempt = viewModel.maxAttempts.getValueForTest()

        assertEquals(maxAttempt, expectedMaxAttempts)

    }
}