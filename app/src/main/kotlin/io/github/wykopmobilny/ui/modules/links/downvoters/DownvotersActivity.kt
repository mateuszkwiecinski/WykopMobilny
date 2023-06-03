package io.github.wykopmobilny.ui.modules.links.downvoters

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.github.wykopmobilny.R
import io.github.wykopmobilny.base.BaseActivity
import io.github.wykopmobilny.databinding.ActivityVoterslistBinding
import io.github.wykopmobilny.models.dataclass.Downvoter
import io.github.wykopmobilny.models.fragments.DataFragment
import io.github.wykopmobilny.models.fragments.getDataFragmentInstance
import io.github.wykopmobilny.models.fragments.removeDataFragment
import io.github.wykopmobilny.ui.adapters.DownvoterListAdapter
import io.github.wykopmobilny.utils.prepare
import io.github.wykopmobilny.utils.viewBinding
import javax.inject.Inject

class DownvotersActivity : BaseActivity(), SwipeRefreshLayout.OnRefreshListener, DownvotersView {

    companion object {
        const val DATA_FRAGMENT_TAG = "DOWNVOTERS_LIST"
        const val EXTRA_LINKID = "LINK_ID_EXTRA"

        fun createIntent(linkId: Long, activity: Activity) = Intent(activity, DownvotersActivity::class.java).apply {
            putExtra(EXTRA_LINKID, linkId)
        }
    }

    @Inject
    lateinit var presenter: DownvotersPresenter

    override val enableSwipeBackLayout = true

    @Inject
    lateinit var downvotersAdapter: DownvoterListAdapter
    private lateinit var downvotersDataFragment: DataFragment<List<Downvoter>>

    private val binding by viewBinding(ActivityVoterslistBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        downvotersDataFragment = supportFragmentManager.getDataFragmentInstance(DATA_FRAGMENT_TAG)
        setSupportActionBar(binding.toolbar.toolbar)
        supportActionBar?.title = resources.getString(R.string.downvoters)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.swiperefresh.setOnRefreshListener(this)
        binding.recyclerView.apply {
            prepare()
            adapter = downvotersAdapter
        }
        binding.swiperefresh.isRefreshing = false
        presenter.linkId = intent.getLongExtra(EXTRA_LINKID, -1)
        presenter.subscribe(this)

        if (downvotersDataFragment.data == null) {
            binding.loadingView.isVisible = true
            onRefresh()
        } else {
            downvotersAdapter.items.addAll(downvotersDataFragment.data!!)
            binding.loadingView.isVisible = false
        }
    }

    override fun showDownvoters(downvoters: List<Downvoter>) {
        binding.loadingView.isVisible = false
        binding.swiperefresh.isRefreshing = false
        downvotersAdapter.apply {
            items.clear()
            items.addAll(downvoters)
            notifyDataSetChanged()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRefresh() = presenter.getDownvoters()

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        downvotersDataFragment.data = downvotersAdapter.items
    }

    override fun onDestroy() {
        presenter.unsubscribe()
        super.onDestroy()
    }

    override fun onPause() {
        if (isFinishing) supportFragmentManager.removeDataFragment(downvotersDataFragment)
        super.onPause()
    }
}
