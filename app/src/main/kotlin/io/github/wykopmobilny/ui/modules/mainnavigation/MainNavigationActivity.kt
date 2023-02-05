package io.github.wykopmobilny.ui.modules.mainnavigation

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.os.postDelayed
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.internal.NavigationMenuView
import com.google.android.material.navigation.NavigationView
import io.github.wykopmobilny.R
import io.github.wykopmobilny.api.patrons.PatronsApi
import io.github.wykopmobilny.base.BaseActivity
import io.github.wykopmobilny.base.BaseNavigationView
import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.databinding.ActivityNavigationBinding
import io.github.wykopmobilny.databinding.AppAboutBottomsheetBinding
import io.github.wykopmobilny.databinding.DrawerHeaderViewLayoutBinding
import io.github.wykopmobilny.databinding.PatronListItemBinding
import io.github.wykopmobilny.databinding.PatronsBottomsheetBinding
import io.github.wykopmobilny.storage.api.SettingsPreferencesApi
import io.github.wykopmobilny.kotlin.AppDispatchers
import io.github.wykopmobilny.ui.dialogs.confirmationDialog
import io.github.wykopmobilny.ui.modules.NewNavigator
import io.github.wykopmobilny.ui.modules.favorite.FavoriteFragment
import io.github.wykopmobilny.ui.modules.links.hits.HitsFragment
import io.github.wykopmobilny.ui.modules.links.promoted.PromotedFragment
import io.github.wykopmobilny.ui.modules.links.upcoming.UpcomingFragment
import io.github.wykopmobilny.ui.modules.mikroblog.feed.hot.HotFragment
import io.github.wykopmobilny.ui.modules.mywykop.MyWykopFragment
import io.github.wykopmobilny.ui.modules.notificationslist.NotificationsListActivity
import io.github.wykopmobilny.ui.modules.notificationslist.notification.NotificationsListFragment
import io.github.wykopmobilny.ui.modules.pm.conversationslist.ConversationsListFragment
import io.github.wykopmobilny.ui.modules.profile.ProfileActivity
import io.github.wykopmobilny.ui.modules.search.SearchFragment
import io.github.wykopmobilny.ui.widgets.BadgeDrawerDrawable
import io.github.wykopmobilny.ui.widgets.drawerheaderview.DrawerHeaderWidget
import io.github.wykopmobilny.utils.linkhandler.WykopLinkHandler
import io.github.wykopmobilny.utils.openBrowser
import io.github.wykopmobilny.utils.shortcuts.ShortcutsDispatcher
import io.github.wykopmobilny.utils.usermanager.UserManagerApi
import io.github.wykopmobilny.utils.usermanager.isUserAuthorized
import io.github.wykopmobilny.utils.viewBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface MainNavigationInterface {
    val activityToolbar: Toolbar
    fun openFragment(fragment: Fragment)
    val floatingButton: View
    fun forceRefreshNotifications()
}

class MainNavigationActivity :
    BaseActivity(),
    MainNavigationView,
    NavigationView.OnNavigationItemSelectedListener,
    MainNavigationInterface {

    companion object {
        const val TARGET_FRAGMENT_KEY = "TARGET_FRAGMENT"
        const val TARGET_NOTIFICATIONS = "TARGET_NOTIFICATIONS"

        fun getIntent(context: Context, targetFragment: String? = null): Intent {
            val intent = Intent(context, MainNavigationActivity::class.java)
            targetFragment?.let {
                intent.putExtra(TARGET_FRAGMENT_KEY, targetFragment)
            }
            return intent
        }
    }

    @Inject
    lateinit var patronsApi: PatronsApi

    @Inject
    lateinit var appStorage: AppStorage

    @Inject
    lateinit var settingsPreferencesApi: SettingsPreferencesApi

    private val binding by viewBinding(ActivityNavigationBinding::inflate)

    override val activityToolbar: Toolbar get() = binding.toolbar.toolbar
    var tapDoubleClickedMillis = 0L

    private val navHeader by lazy { binding.navigationView.getHeaderView(0) as DrawerHeaderWidget }
    private val navHeaderBinding by lazy { DrawerHeaderViewLayoutBinding.bind(navHeader) }
    private val actionBarToggle by lazy {
        ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar.toolbar,
            R.string.nav_drawer_open,
            R.string.nav_drawer_closed,
        )
    }
    private val badgeDrawable by lazy { BadgeDrawerDrawable(supportActionBar!!.themedContext) }

    override val floatingButton: View
        get() = binding.fab

    @Inject
    lateinit var presenter: MainNavigationPresenter

    @Inject
    lateinit var settingsApi: SettingsPreferencesApi

    @Inject
    lateinit var shortcutsDispatcher: ShortcutsDispatcher

    @Inject
    lateinit var navigator: NewNavigator

    @Inject
    lateinit var userManagerApi: UserManagerApi

    @Inject
    lateinit var linkHandler: WykopLinkHandler

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_mikroblog -> openFragment(HotFragment.newInstance())
            R.id.login -> navigator.openLoginScreen()
            R.id.messages -> openFragment(ConversationsListFragment.newInstance())
            R.id.nav_settings -> navigator.openSettingsActivity()
            R.id.nav_mojwykop -> openFragment(MyWykopFragment.newInstance())
            R.id.nav_home -> openFragment(PromotedFragment.newInstance())
            R.id.search -> openFragment(SearchFragment.newInstance())
            R.id.favourite -> openFragment(FavoriteFragment.newInstance())
            R.id.your_profile -> startActivity(ProfileActivity.createIntent(this, userManagerApi.getUserCredentials()!!.login))
            R.id.nav_wykopalisko -> openFragment(UpcomingFragment.newInstance())
            R.id.hits -> openFragment(HitsFragment.newInstance())
            R.id.about -> openAboutSheet()
            R.id.logout -> {
                confirmationDialog(this) {
                    lifecycleScope.launch {
                        withContext(AppDispatchers.IO) {
                            appStorage.blacklistQueries.transaction {
                                appStorage.blacklistQueries.deleteAll()
                            }
                            userManagerApi.logoutUser()
                        }
                        restartActivity()
                    }
                }.show()
            }
            else -> showNotImplementedToast()
        }

        item.isChecked = true
        binding.drawerLayout.closeDrawers()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar.toolbar)

        if (!presenter.isSubscribed) {
            presenter.subscribe(this)
        }

        Handler(Looper.getMainLooper()).postDelayed(333) {
            presenter.startListeningForNotifications()
        }

        (binding.navigationView.getChildAt(0) as NavigationMenuView).isVerticalScrollBarEnabled = false

        actionBarToggle.drawerArrowDrawable = badgeDrawable
        binding.toolbar.toolbar.tag = binding.toolbar.toolbar.overflowIcon // We want to save original overflow icon drawable into memory.
        navHeader.showDrawerHeader(userManagerApi.isUserAuthorized(), userManagerApi.getUserCredentials())
        showUsersMenu(userManagerApi.isUserAuthorized())

        if (savedInstanceState == null) {
            if (intent.hasExtra(TARGET_FRAGMENT_KEY)) {
                when (intent.getStringExtra(TARGET_FRAGMENT_KEY)) {
                    TARGET_NOTIFICATIONS -> openFragment(NotificationsListFragment.newInstance())
                }
            } else {
                openMainFragment()
            }
        }
        setupNavigation()
        shortcutsDispatcher.dispatchIntent(
            intent = intent,
            startFragment = ::openFragment,
            startActivity = navigator::openLoginScreen,
            isUserAuthorized = userManagerApi.isUserAuthorized(),
        )
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        actionBarToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        actionBarToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        if (!presenter.isSubscribed) {
            presenter.subscribe(this)

            Handler(Looper.getMainLooper()).postDelayed(333) {
                presenter.startListeningForNotifications()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        presenter.unsubscribe()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (presenter.isSubscribed) presenter.unsubscribe()
    }

    private fun setupNavigation() {
        binding.drawerLayout.addDrawerListener(actionBarToggle)
        binding.navigationView.setNavigationItemSelectedListener(this)
    }

    override fun showUsersMenu(value: Boolean) {
        binding.navigationView.menu.apply {
            setGroupVisible(R.id.nav_user, value)
            findItem(R.id.nav_mojwykop).isVisible = value
            findItem(R.id.login).isVisible = !value
            findItem(R.id.logout).isVisible = value
        }

        navHeader.isVisible = value
        navHeader.apply {
            navHeaderBinding.navNotificationsTag.setOnClickListener {
                deselectItems()
                navigator.openNotificationsListActivity(NotificationsListActivity.PRESELECT_HASHTAGS)
            }

            navHeaderBinding.navNotifications.setOnClickListener {
                deselectItems()
                navigator.openNotificationsListActivity(NotificationsListActivity.PRESELECT_NOTIFICATIONS)
            }
        }
    }

    private fun openMainFragment() {
        when (settingsApi.defaultScreen) {
            "mainpage", null -> openFragment(PromotedFragment.newInstance())
            "mikroblog" -> openFragment(HotFragment.newInstance())
            "mywykop" -> openFragment(MyWykopFragment.newInstance())
            "hits" -> openFragment(HitsFragment.newInstance())
        }
    }

    override fun openFragment(fragment: Fragment) {
        supportActionBar?.subtitle = null
        binding.fab.isVisible = false
        binding.fab.setOnClickListener(null)
        binding.fab.isVisible = fragment is BaseNavigationView && userManagerApi.isUserAuthorized()
        val ft = supportFragmentManager.beginTransaction()
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
        ft.replace(R.id.contentView, fragment)
        ft.commit()
        closeDrawer()
    }

    private fun closeDrawer() = binding.drawerLayout.closeDrawers()

    private fun deselectItems() {
        val menu = binding.navigationView.menu
        for (i in 0 until menu.size()) {
            menu.getItem(i).isChecked = false
        }
    }

    override fun showNotificationsCount(notifications: Int) {
        badgeDrawable.text = if (notifications > 0) notifications.toString() else null
        navHeader.notificationCount = notifications
    }

    override fun showHashNotificationsCount(hashNotifications: Int) {
        navHeader.hashTagsNotificationsCount = hashNotifications
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeDrawer()
        } else {
            if (settingsApi.disableExitConfirmation || tapDoubleClickedMillis + 2000L > System.currentTimeMillis()) {
                super.onBackPressed()
                return
            } else {
                Toast.makeText(this, R.string.doubleback_to_exit, Toast.LENGTH_SHORT).show()
            }
            tapDoubleClickedMillis = System.currentTimeMillis()
        }
    }

    override fun restartActivity() {
        navigator.openMainActivity()
        finish()
    }

    override fun showNotImplementedToast() {
        Toast.makeText(this, "Nie zaimplementowano", Toast.LENGTH_SHORT).show()
        deselectItems()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == NewNavigator.STARTED_FROM_NOTIFICATIONS_CODE) {
            if (!presenter.isSubscribed) {
                presenter.subscribe(this)
                presenter.startListeningForNotifications()
            }
            presenter.checkNotifications(true)
        }
    }

    private fun openAboutSheet() {
        val dialog = BottomSheetDialog(this)
        val bottomSheetView = AppAboutBottomsheetBinding.inflate(layoutInflater)
        dialog.setContentView(bottomSheetView.root)

        bottomSheetView.apply {
            val versionName = packageManager.getPackageInfo(packageName, 0).versionName
            appVersionTextview.text = getString(R.string.app_version, versionName)
            appVersion.setOnClickListener {
                openBrowser("https://github.com/otwarty-wykop-mobilny/wykop-android")
                dialog.dismiss()
            }

            appReportBug.setOnClickListener {
                navigator.openTagActivity("owmbugi")
                dialog.dismiss()
            }

            appObserveTag.setOnClickListener {
                navigator.openTagActivity("otwartywykopmobilny2")
                dialog.dismiss()
            }

            appPatrons.setOnClickListener {
                dialog.dismiss()
                showAppPatronsDialog()
            }

            license.setOnClickListener {
                openBrowser("https://github.com/otwarty-wykop-mobilny/wykop-android/blob/master/LICENSE")
                dialog.dismiss()
            }

            privacyPolicy.setOnClickListener {
                openBrowser("https://sites.google.com/view/otwarty-wykop-mobilny-v2")
                dialog.dismiss()
            }
        }

        val mBehavior = BottomSheetBehavior.from(bottomSheetView.root.parent as View)
        dialog.setOnShowListener {
            mBehavior.peekHeight = bottomSheetView.root.height
        }
        dialog.show()
    }

    private fun showAppPatronsDialog() {
        val patronsDialog = BottomSheetDialog(this)
        val badgesDialogView2 = PatronsBottomsheetBinding.inflate(layoutInflater)
        patronsDialog.setContentView(badgesDialogView2.root)

        for (badge in patronsApi.patrons.filter { patron -> patron.listMention }) {
            val item = PatronListItemBinding.inflate(layoutInflater)
            item.root.setOnClickListener {
                patronsDialog.dismiss()
                linkHandler.handleUrl("https://wykop.pl/ludzie/" + badge.username)
            }
            item.nickname.text = badge.username
            item.tierTextView.text = when (badge.tier) {
                "patron50" -> "Patron próg \"Białkowy\""
                "patron25" -> "Patron próg \"Bordowy\""
                "patron10" -> "Patron próg \"Pomaranczowy\""
                "patron5" -> "Patron próg \"Zielony\""
                else -> "Patron"
            }
            badgesDialogView2.patronsList.addView(item.root)
        }
        patronsDialog.show()
    }

    override fun forceRefreshNotifications() {
        presenter.checkNotifications(true)
    }
}
