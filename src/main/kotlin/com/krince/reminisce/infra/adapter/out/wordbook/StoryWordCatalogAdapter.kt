package com.krince.reminisce.infra.adapter.out.wordbook

import com.krince.reminisce.application.port.out.wordbook.LoadStoryWordPort
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.wordbook.StoryWord
import com.krince.reminisce.domain.model.wordbook.StoryWordGroup
import org.springframework.stereotype.Component

@Component
class StoryWordCatalogAdapter : LoadStoryWordPort {

    companion object {
        private const val BANGGUI_STORY_ID = "s_banggui_daughter_in_law_001"
    }

    override fun findAllGroups(): List<StoryWordGroup> = listOf(bangguiGroup())

    private fun bangguiGroup(): StoryWordGroup = StoryWordGroup(
        storyId = StoryId(BANGGUI_STORY_ID),
        storyTitle = "방귀 뀌는 며느리",
        words = listOf(
            StoryWord(
                word = "방귀를 참다",
                meaning = "나오려는 방귀를 뀌지 않고 꾹 견디는 것",
                imageUrl = "/files/banggui-word-01.png",
            ),
            StoryWord(
                word = "부끄럽다",
                meaning = "다른 사람이 나를 어떻게 볼지 걱정되어 숨고 싶은 마음",
                imageUrl = "/files/banggui-word-02.png",
            ),
            StoryWord(
                word = "특별하다",
                meaning = "다른 것과 달라서 더욱 소중하거나 눈에 띄는 것",
                imageUrl = "/files/banggui-word-03.png",
            ),
            StoryWord(
                word = "안전하다",
                meaning = "다치거나 위험한 일이 생기지 않도록 조심하는 것",
                imageUrl = "/files/banggui-word-04.png",
            ),
            StoryWord(
                word = "도움이 되다",
                meaning = "다른 사람이 일을 더 쉽고 잘할 수 있게 해 주는 것",
                imageUrl = "/files/banggui-word-05.png",
            ),
            StoryWord(
                word = "사과하다",
                meaning = "잘못을 인정하고 미안한 마음을 전하는 것",
                imageUrl = "/files/banggui-word-06.png",
            ),
        ),
    )
}
