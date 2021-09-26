package io.github.wykopmobilny.ui.modules.pm.conversation

import android.app.Activity
import dagger.Binds
import dagger.Module

@Module
abstract class ConversationActivityModule {

    @Binds
    abstract fun ConversationActivity.activity(): Activity
}
