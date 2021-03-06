package ro.ande.dekont.ui

import ro.ande.dekont.vo.Category
import ro.ande.dekont.vo.Transaction

interface TransactionListAdapter {
    fun setCategories(categories: List<Category>)
    fun setTransactions(transactions: List<Transaction>)
    fun appendTransactions(newTransactions: List<Transaction>)

    /** To be used by [HeaderItemDecoration] */
    fun isItemHeader(position: Int): Boolean = false

    fun interface OnTransactionClickListener {
        fun onClick(transactionId: Int)
    }

    var onTransactionClickListener: OnTransactionClickListener?

    fun interface OnTransactionLongClickListener {
        fun onLongClick(transactionId: Int)
    }

    var onTransactionLongClickListener: OnTransactionLongClickListener?
}
