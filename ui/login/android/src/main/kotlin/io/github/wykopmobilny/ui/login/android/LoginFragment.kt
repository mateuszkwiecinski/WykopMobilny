package io.github.wykopmobilny.ui.login.android

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.github.wykopmobilny.ui.login.Login
import io.github.wykopmobilny.ui.login.LoginDependencies
import io.github.wykopmobilny.ui.login.android.databinding.FragmentLoginBinding
import io.github.wykopmobilny.utils.bindings.collectErrorDialog
import io.github.wykopmobilny.utils.requireDependency
import io.github.wykopmobilny.utils.viewBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

fun loginFragment(): Fragment = LoginFragment()

internal class LoginFragment : Fragment(R.layout.fragment_login) {

    private val binding by viewBinding(FragmentLoginBinding::bind)
    private lateinit var login: Login

    override fun onAttach(context: Context) {
        login = context.requireDependency<LoginDependencies>().login()
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(binding.webView, true)
        }
        @SuppressLint("SetJavaScriptEnabled")
        binding.webView.settings.javaScriptEnabled = true

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            val sharedFlow = login().stateIn(this)

            binding.webView.webViewClient = object : WebViewClient() {

                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    sharedFlow.value.parseUrlAction(request.url.toString())
                    return super.shouldOverrideUrlLoading(view, request)
                }
            }

            launch {
                sharedFlow.map { it.urlToLoad }
                    .distinctUntilChanged()
                    .collect { binding.webView.loadUrl(it) }
            }
            launch {
                sharedFlow.map { it.isLoading }
                    .distinctUntilChanged()
                    .collect { binding.fullScreenProgress.isVisible = it }
            }
            launch { sharedFlow.map { it.errorDialog }.collectErrorDialog(requireContext()) }
        }
    }
}
