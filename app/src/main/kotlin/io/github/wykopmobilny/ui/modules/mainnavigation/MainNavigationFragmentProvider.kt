package io.github.wykopmobilny.ui.modules.mainnavigation

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.github.wykopmobilny.ui.fragments.entries.EntriesFragmentProvider
import io.github.wykopmobilny.ui.modules.favorite.FavoriteFragment
import io.github.wykopmobilny.ui.modules.favorite.FavoriteFragmentProvider
import io.github.wykopmobilny.ui.modules.links.hits.HitsFragment
import io.github.wykopmobilny.ui.modules.links.hits.HitsModule
import io.github.wykopmobilny.ui.modules.links.promoted.PromotedFragment
import io.github.wykopmobilny.ui.modules.links.promoted.PromotedFragmentModule
import io.github.wykopmobilny.ui.modules.links.upcoming.UpcomingFragment
import io.github.wykopmobilny.ui.modules.links.upcoming.UpcomingModule
import io.github.wykopmobilny.ui.modules.mikroblog.feed.hot.HotFragment
import io.github.wykopmobilny.ui.modules.mikroblog.feed.hot.HotFragmentModule
import io.github.wykopmobilny.ui.modules.mywykop.MyWykopFragment
import io.github.wykopmobilny.ui.modules.mywykop.MyWykopFragmentProvider
import io.github.wykopmobilny.ui.modules.notificationslist.hashtags.HashTagsNotificationsListFragment
import io.github.wykopmobilny.ui.modules.notificationslist.hashtags.HashTagsNotificationsListFragmentModule
import io.github.wykopmobilny.ui.modules.notificationslist.notification.NotificationsListFragment
import io.github.wykopmobilny.ui.modules.notificationslist.notification.NotificationsListFragmentModule
import io.github.wykopmobilny.ui.modules.pm.conversationslist.ConversationsListFragment
import io.github.wykopmobilny.ui.modules.pm.conversationslist.ConversationsListFragmentModule
import io.github.wykopmobilny.ui.modules.search.SearchFragment
import io.github.wykopmobilny.ui.modules.search.SearchFragmentProvider

@Module
abstract class MainNavigationFragmentProvider {
    @ContributesAndroidInjector(modules = [PromotedFragmentModule::class])
    abstract fun providePromotedFragment(): PromotedFragment

    @ContributesAndroidInjector(modules = [HotFragmentModule::class, EntriesFragmentProvider::class])
    abstract fun provideHotFragment(): HotFragment

    @ContributesAndroidInjector(modules = [HashTagsNotificationsListFragmentModule::class])
    abstract fun provideHashTagsNotificationsListFragment(): HashTagsNotificationsListFragment

    @ContributesAndroidInjector(modules = [NotificationsListFragmentModule::class])
    abstract fun provideNotificationsListFragment(): NotificationsListFragment

    @ContributesAndroidInjector(modules = [ConversationsListFragmentModule::class])
    abstract fun provideConversationListFragment(): ConversationsListFragment

    @ContributesAndroidInjector(modules = [MyWykopFragmentProvider::class])
    abstract fun provideMyWykopFragment(): MyWykopFragment

    @ContributesAndroidInjector(modules = [FavoriteFragmentProvider::class])
    abstract fun provideFavoriteFragment(): FavoriteFragment

    @ContributesAndroidInjector(modules = [SearchFragmentProvider::class])
    abstract fun provideSearchFragment(): SearchFragment

    @ContributesAndroidInjector(modules = [HitsModule::class])
    abstract fun provideHitsFragment(): HitsFragment

    @ContributesAndroidInjector(modules = [UpcomingModule::class])
    abstract fun provideUpcomingFragment(): UpcomingFragment
}
