package io.github.wykopmobilny.ui.twofactor.android

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.github.wykopmobilny.ui.two_factor.android.R
import io.github.wykopmobilny.ui.two_factor.android.databinding.FragmentTwoFactorBinding
import io.github.wykopmobilny.ui.twofactor.TwoFactorAuthDependencies
import io.github.wykopmobilny.utils.InjectableViewModel
import io.github.wykopmobilny.utils.viewModelWrapperFactory

fun twoFactorMainFragment(): Fragment = TwoFactorMainFragment()

internal class TwoFactorMainFragment : Fragment(R.layout.fragment_two_factor) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel by viewModels<InjectableViewModel<TwoFactorAuthDependencies>> {
            viewModelWrapperFactory<Unit, TwoFactorAuthDependencies>(key = Unit)
        }
//        val getLinkDetails = viewModel.dependency.getLinkDetails()

        val binding = FragmentTwoFactorBinding.bind(view)
        binding.toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
    }
}
