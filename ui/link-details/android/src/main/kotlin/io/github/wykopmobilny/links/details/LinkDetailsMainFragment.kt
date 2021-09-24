package io.github.wykopmobilny.links.details

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import io.github.wykopmobilny.ui.link_details.android.R
import io.github.wykopmobilny.ui.link_details.android.databinding.FragmentLinkDetailsBinding
import io.github.wykopmobilny.utils.destroyKeyedDependency
import io.github.wykopmobilny.utils.requireKeyedDependency
import io.github.wykopmobilny.utils.stringArgument
import io.github.wykopmobilny.utils.stringArgumentNullable
import io.github.wykopmobilny.utils.viewBinding

fun linkDetailsFragment(linkId: String, commentId: String?): Fragment =
    LinkDetailsMainFragment()
        .apply {
            this.linkId = linkId
            this.commentId = commentId
        }

internal class LinkDetailsMainFragment : Fragment(R.layout.fragment_link_details) {

    var linkId by stringArgument("userId")
    var commentId by stringArgumentNullable("commenetID")

    private lateinit var getLinkDetails: GetLinkDetails

    private val binding by viewBinding(FragmentLinkDetailsBinding::bind)

    override fun onAttach(context: Context) {
        getLinkDetails = context.requireKeyedDependency<LinkDetailsDependencies>(linkId).getLinkDetails()
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().destroyKeyedDependency<LinkDetailsDependencies>(linkId)
    }
}

