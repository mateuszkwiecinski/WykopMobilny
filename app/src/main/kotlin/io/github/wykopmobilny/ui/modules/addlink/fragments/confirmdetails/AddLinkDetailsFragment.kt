package io.github.wykopmobilny.ui.modules.addlink.fragments.confirmdetails

import android.os.Bundle
import android.view.View
import androidx.core.view.children
import androidx.core.view.isVisible
import io.github.wykopmobilny.R
import io.github.wykopmobilny.api.responses.AddLinkPreviewImage
import io.github.wykopmobilny.base.BaseFragment
import io.github.wykopmobilny.databinding.AddlinkDetailsFragmentBinding
import io.github.wykopmobilny.databinding.AddlinkPreviewImageBinding
import io.github.wykopmobilny.models.dataclass.Link
import io.github.wykopmobilny.ui.modules.NavigatorApi
import io.github.wykopmobilny.ui.modules.addlink.AddlinkActivity
import io.github.wykopmobilny.utils.api.stripImageCompression
import io.github.wykopmobilny.utils.loadImage
import io.github.wykopmobilny.utils.viewBinding
import javax.inject.Inject

class AddLinkDetailsFragment : BaseFragment(R.layout.addlink_details_fragment), AddLinkDetailsFragmentView {

    companion object {
        fun newInstance() = AddLinkDetailsFragment()
    }

    @Inject
    lateinit var presenter: AddLinkDetailsFragmentPresenter

    @Inject
    lateinit var navigator: NavigatorApi

    private val binding by viewBinding(AddlinkDetailsFragmentBinding::bind)

    private var selectedImage: String? = null
    private val draftInformation
        get() = (activity as? AddlinkActivity)?.draft?.data

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.subscribe(this)
        draftInformation?.apply {
            binding.inputDescription.setText(description)
            binding.inputLinkTitle.setText(title)
            presenter.getImages(key)
        }

        binding.submit.setOnClickListener { validate() }
    }

    override fun onDestroyView() {
        presenter.unsubscribe()
        super.onDestroyView()
    }

    override fun openLinkScreen(link: Link) {
        navigator.openLinkDetailsActivity(requireActivity(), link)
        requireActivity().finish()
    }

    override fun showImages(images: List<AddLinkPreviewImage>) {
        binding.imagesList.removeAllViews()
        selectedImage = images.firstOrNull()?.key
        images.forEachIndexed { index, image ->
            val imageBinding = AddlinkPreviewImageBinding.inflate(layoutInflater, binding.imagesList, true)
            imageBinding.previewImage.loadImage(image.sourceUrl.stripImageCompression())
            imageBinding.tick.isVisible = index == 0

            imageBinding.root.setOnClickListener {
                selectedImage = image.key
                binding.imagesList.children
                    .map(AddlinkPreviewImageBinding::bind)
                    .forEach { image -> image.tick.isVisible = false }
                imageBinding.tick.isVisible = true
            }
        }
    }

    private fun validate() {
        if (binding.inputDescription.text.isEmpty()) {
            binding.inputDescriptionLayout.error = getString(R.string.addlink_no_description)
            return
        }

        if (binding.inputLinkTitle.text.isEmpty()) {
            binding.inputLinkTitleLayout.error = getString(R.string.add_link_no_title)
            return
        }

        if (binding.inputTags.text.isEmpty()) {
            binding.inputTagsLayout.error = getString(R.string.add_link_no_tags)
            return
        }
        val draftInformation = draftInformation
        if (draftInformation == null) {
            binding.inputTagsLayout.error = "Coś poszło nie tak"
            return
        }

        presenter.publishLink(
            key = draftInformation.key,
            title = binding.inputLinkTitle.text.toString(),
            sourceUrl = draftInformation.sourceUrl,
            description = binding.inputDescription.text.toString(),
            tags = binding.inputTags.text.toString(),
            plus18 = binding.plus18Checkbox.isChecked,
            imageKey = selectedImage,
        )
    }

    override fun showImagesLoading(visibility: Boolean) {
        binding.imagesLoadingView.isVisible = visibility
    }

    override fun showLinkUploading(visibility: Boolean) {
        binding.previewTitle.isVisible = !visibility
        binding.inputTags.isVisible = !visibility
        binding.inputLinkTitle.isVisible = !visibility
        binding.inputDescription.isVisible = !visibility
        binding.progressBar.isVisible = visibility
        binding.progressBarTitle.isVisible = visibility
        binding.imagesLoadingView.isVisible = !visibility
    }
}
