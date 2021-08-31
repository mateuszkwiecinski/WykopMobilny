package io.github.wykopmobilny.screenshots

import com.thedeanda.lorem.LoremIpsum

fun loremIpsum(count: Int = 20): String = LoremIpsum(count.toLong()).getWords(count)
