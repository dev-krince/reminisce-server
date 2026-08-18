package com.krince.reminisce.application.service.wordbook

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.wordbook.command.GetStoryWordsCommand
import com.krince.reminisce.application.port.`in`.wordbook.result.StoryWordGroupResult
import com.krince.reminisce.application.port.`in`.wordbook.result.StoryWordResult
import com.krince.reminisce.application.port.`in`.wordbook.usecase.GetStoryWordsUseCase
import com.krince.reminisce.application.port.out.tts.NARRATOR_VOICE_PROFILE
import com.krince.reminisce.application.port.out.tts.TtsPort
import com.krince.reminisce.application.port.out.wordbook.LoadStoryWordPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.domain.model.wordbook.StoryWord
import com.krince.reminisce.domain.model.wordbook.StoryWordGroup
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetStoryWordsApplicationService(
    private val loadStoryWordPort: LoadStoryWordPort,
    private val childAccessPort: ChildAccessPort,
    private val ttsPort: TtsPort,
) : GetStoryWordsUseCase {

    @Transactional(readOnly = true)
    override fun execute(command: GetStoryWordsCommand): List<StoryWordGroupResult> {
        verifyOwnership(ChildId(command.childId), UserId(command.guardianId))

        return loadStoryWordPort.findAllGroups().map { toResult(it) }
    }

    private fun toResult(group: StoryWordGroup): StoryWordGroupResult = StoryWordGroupResult(
        storyId = group.storyId.value,
        storyTitle = group.storyTitle,
        words = group.words.map { toResult(it) },
    )

    private fun toResult(storyWord: StoryWord): StoryWordResult = StoryWordResult(
        word = storyWord.word,
        meaning = storyWord.meaning,
        imageUrl = storyWord.imageUrl,
        audioUrl = ttsPort.synthesize(storyWord.word, NARRATOR_VOICE_PROFILE),
    )

    private fun verifyOwnership(childId: ChildId, guardianId: UserId) {
        val ownerId: UserId? = childAccessPort.findGuardianId(childId)
        if (ownerId == null || ownerId != guardianId) {
            throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        }
    }
}
