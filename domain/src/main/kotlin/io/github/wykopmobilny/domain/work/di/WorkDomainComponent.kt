package io.github.wykopmobilny.domain.work.di

import dagger.Subcomponent
import io.github.wykopmobilny.work.WorkDependencies

@WorkScope
@Subcomponent(modules = [WorkModule::class])
interface WorkDomainComponent : WorkDependencies
