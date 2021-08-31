package io.github.wykopmobilny.ui.settings.android

import io.github.wykopmobilny.screenshots.BaseScreenshotTest
import io.github.wykopmobilny.screenshots.unboundedHeight
import io.github.wykopmobilny.ui.settings.AppearancePreferencesUi
import io.github.wykopmobilny.ui.settings.FontSizeUi
import io.github.wykopmobilny.ui.settings.LinkImagePositionUi
import io.github.wykopmobilny.ui.settings.MainScreenUi
import io.github.wykopmobilny.ui.settings.MikroblogScreenUi
import org.junit.Test

internal class GeneralPreferencesFragmentTest : BaseScreenshotTest() {

    override fun createFragment() = AppearancePreferencesFragment()

    @Test
    fun default() {
        registerSettings(
            appearance = {
                AppearancePreferencesUi(
                    appearance = AppearancePreferencesUi.AppearanceSectionUi(
                        useDarkTheme = stubSetting(value = true),
                        useAmoledTheme = stubSetting(value = true),
                        startScreen = stubListSetting(value = MainScreenUi.Mikroblog),
                        fontSize = stubListSetting(value = FontSizeUi.Large),
                        disableEdgeSlide = stubSetting(value = true),
                    ),
                    mediaPlayerSection = AppearancePreferencesUi.MediaPlayerSectionUi(
                        enableYoutubePlayer = stubSetting(value = true),
                        enableEmbedPlayer = stubSetting(value = true),
                    ),
                    mikroblogSection = AppearancePreferencesUi.MikroblogSectionUi(
                        mikroblogScreen = stubListSetting(value = MikroblogScreenUi.TwelveHours),
                        cutLongEntries = stubSetting(value = true),
                        openSpoilersInDialog = stubSetting(value = true),
                    ),
                    linksSection = AppearancePreferencesUi.LinksSectionUi(
                        useSimpleList = stubSetting(value = true),
                        showLinkThumbnail = stubSetting(value = true),
                        imagePosition = stubListSetting(value = LinkImagePositionUi.Left),
                        showAuthor = stubSetting(value = true),
                        cutLinkComments = stubSetting(value = true),
                    ),
                    imagesSection = AppearancePreferencesUi.ImagesSectionUi(
                        showMinifiedImages = stubSetting(value = true),
                        cutImages = stubSetting(value = true),
                        cutImagesProportion = stubSliderSetting(
                            value = 70,
                            range = 30..150,
                        ),
                    ),
                )
            },
        )
        record(size = unboundedHeight())
    }
}
