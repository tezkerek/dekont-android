package ro.ande.dekont.di

import dagger.Binds
import dagger.Module
import ro.ande.dekont.repo.UserRepository

@Module
abstract class RepositoryModule {
    @Binds
    abstract fun bindsUserRepository(userRepository: UserRepository): UserRepository
}