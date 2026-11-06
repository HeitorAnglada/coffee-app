package com.heitoranglada.coffeeapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.heitoranglada.coffeeapp.CoffeeApplication
import com.heitoranglada.coffeeapp.data.CoffeeRepository

/** Cria uma factory de ViewModel que recebe o repositório (injeção de dependência manual). */
inline fun <reified VM : ViewModel> repoViewModelFactory(
    crossinline create: (CoffeeRepository) -> VM,
): ViewModelProvider.Factory = viewModelFactory {
    initializer { create((this[APPLICATION_KEY] as CoffeeApplication).repository) }
}
