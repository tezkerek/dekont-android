package ro.ande.dekont.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ro.ande.dekont.viewmodel.*

@Module
abstract class ViewModelModule {
    @Binds @IntoMap
    @ViewModelKey(LoginViewModel::class)
    internal abstract fun bindLoginViewModel(loginViewModel: LoginViewModel): ViewModel

    @Binds @IntoMap
    @ViewModelKey(MainViewModel::class)
    internal abstract fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel

    @Binds @IntoMap
    @ViewModelKey(TransactionListViewModel::class)
    internal abstract fun bindTransactionListViewModel(transactionListViewModel: TransactionListViewModel): ViewModel

    @Binds @IntoMap
    @ViewModelKey(TransactionEditorViewModel::class)
    internal abstract fun bindTransactionEditorViewModel(transactionEditorViewModel: TransactionEditorViewModel): ViewModel

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}