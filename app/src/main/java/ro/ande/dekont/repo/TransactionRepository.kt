package ro.ande.dekont.repo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import ro.ande.dekont.api.*
import ro.ande.dekont.db.TransactionDao
import ro.ande.dekont.util.CachedNetworkData
import ro.ande.dekont.util.NetworkErrorState
import ro.ande.dekont.util.NetworkLoadingState
import ro.ande.dekont.util.NetworkSuccessState
import ro.ande.dekont.vo.Resource
import ro.ande.dekont.vo.ResourceDeletion
import ro.ande.dekont.vo.Transaction
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

class TransactionRepository
@Inject constructor(
    private val transactionDao: TransactionDao,
    private val dekontService: DekontService
) {
    /** Retrieve the cached page, and simultaneously fetch changes from the server. */
    fun loadTransactions(page: Int, users: List<Int>?): CachedNetworkData<List<Transaction>> {
        val cachedData =
            transactionDao
                .retrievePartial((page - 1) * PAGE_SIZE, PAGE_SIZE)
                .distinctUntilChanged()

        val networkState = flow {
            // Emit loading state
            emit(NetworkLoadingState())

            // Retrieve data from server
            val response = dekontService.listTransactions(page, users)
            when (response) {
                is ApiSuccessResponse -> {
                    val isDataExhausted = response.body.page == response.body.pageCount
                    emit(NetworkSuccessState(isExhausted = isDataExhausted))

                    // The DB source will refresh automatically after insertion
                    transactionDao.insertAndReplace(response.body.data)
                }
                is ApiErrorResponse -> {
                    emit(NetworkErrorState(response.getFirstError()))
                }
                is ApiEmptyResponse -> {
                }
            }
        }.distinctUntilChanged()

        return CachedNetworkData(cachedData, networkState)
    }

    suspend fun retrieveTransactionById(id: Int): Transaction? {
        return transactionDao.retrieveById(id)
    }

    suspend fun createTransaction(transaction: Transaction): Resource<Transaction> =
        withContext(Dispatchers.IO) {
            // Attempt to insert on server.
            val response = dekontService.createTransaction(transaction)

            when (response) {
                is ApiSuccessResponse -> {
                    // Insert locally.
                    val newTransaction = response.body
                    transactionDao.insert(newTransaction)

                    Resource.success(newTransaction)
                }
                is ApiErrorResponse -> Resource.error(response.getFirstError(), null)
                else -> Resource.error("Unexpected error: response is empty", null)
            }
        }

    suspend fun updateTransaction(transaction: Transaction): Transaction =
        TODO("Not implemented")

    suspend fun deleteTransaction(id: Int): ResourceDeletion =
        withContext(Dispatchers.IO) {
            // Attempt to delete on server.
            val response = dekontService.deleteTransaction(id)

            // Delete locally even if the resource was not found on the server
            val shouldDeleteLocally =
                response is ApiErrorResponse && response.type == ApiErrorType.NOT_FOUND

            if (response.isSuccess() || shouldDeleteLocally) {
                // Delete locally
                transactionDao.delete(id)
                ResourceDeletion.success()
            } else {
                response as ApiErrorResponse
                ResourceDeletion.error(response.getFirstError())
            }
        }

    fun mockTransactions(count: Int): Flow<List<Transaction>> =
        flow<List<Transaction>> {
            val list = mutableListOf<Transaction>()
            (1..count).forEach { mockId ->
                list.add(
                    Transaction(
                        mockId,
                        0,
                        LocalDate.now().minusDays(mockId * 3L),
                        BigDecimal(9000 + mockId),
                        Currency.getInstance(Locale.getDefault()),
                        null,
                        "Mocked transaction",
                        "",
                        "",
                        "",
                        Transaction.PENDING
                    )
                )
            }
            emit(list)
        }

    companion object {
        const val PAGE_SIZE = 15
    }
}