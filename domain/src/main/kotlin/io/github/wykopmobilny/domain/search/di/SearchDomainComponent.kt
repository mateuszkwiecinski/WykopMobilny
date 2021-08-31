package io.github.wykopmobilny.domain.search.di

import dagger.Subcomponent
import io.github.wykopmobilny.ui.search.SearchDependencies

@SearchScope
@Subcomponent(modules = [SearchModule::class])
interface SearchDomainComponent : SearchDependencies
