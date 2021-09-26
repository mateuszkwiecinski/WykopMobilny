package io.github.wykopmobilny.ui.profile

import androidx.paging.PagingData
import io.github.wykopmobilny.ui.base.Query
import io.github.wykopmobilny.ui.components.widgets.ListElementUi

interface GetProfileActions : Query<PagingData<ListElementUi>>
