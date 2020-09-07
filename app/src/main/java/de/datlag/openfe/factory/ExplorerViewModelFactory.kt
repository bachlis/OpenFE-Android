package de.datlag.openfe.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.datlag.openfe.fragments.ExplorerFragmentArgs
import de.datlag.openfe.viewmodel.AppsViewModel
import de.datlag.openfe.viewmodel.ExplorerViewModel

class ExplorerViewModelFactory (
    private val explorerArgs: ExplorerFragmentArgs,
    private val appsViewModel: AppsViewModel
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(
        modelClass: Class<T>
    ) = ExplorerViewModel(explorerArgs, appsViewModel) as T

}