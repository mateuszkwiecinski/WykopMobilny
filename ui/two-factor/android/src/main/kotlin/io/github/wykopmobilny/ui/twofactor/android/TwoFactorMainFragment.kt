package io.github.wykopmobilny.ui.twofactor.android

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.wykopmobilny.ui.two_factor.android.R
import io.github.wykopmobilny.ui.two_factor.android.databinding.FragmentTwoFactorBinding
import io.github.wykopmobilny.ui.twofactor.TwoFactorAuthDependencies
import io.github.wykopmobilny.utils.InjectableViewModel
import io.github.wykopmobilny.utils.bindings.bindBackButton
import io.github.wykopmobilny.utils.bindings.collectErrorDialog
import io.github.wykopmobilny.utils.bindings.collectProgressInput
import io.github.wykopmobilny.utils.bindings.collectUserInput
import io.github.wykopmobilny.utils.viewModelWrapperFactory
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

fun twoFactorMainFragment(): Fragment = TwoFactorMainFragment()

internal class TwoFactorMainFragment : Fragment(R.layout.fragment_two_factor) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel by viewModels<InjectableViewModel<TwoFactorAuthDependencies>> {
            viewModelWrapperFactory<TwoFactorAuthDependencies>()
        }
        val getTwoFactorAuthDetails = viewModel.dependency.getTwoFactorAuthDetails()

        val binding = FragmentTwoFactorBinding.bind(view)
        binding.toolbar.bindBackButton(activity = activity)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                val shared = getTwoFactorAuthDetails().stateIn(this)

                launch { shared.map { it.code }.collectUserInput(binding.code) }
                launch { shared.map { it.verifyButton }.collectProgressInput(binding.buttonVerify, binding.progress) }
                launch { shared.map { it.authenticatorButton }.collectProgressInput(binding.butonOpenApp, progress = null) }
                launch { shared.map { it.errorDialog }.collectErrorDialog(binding.root.context) }
            }
        }
    }
}
