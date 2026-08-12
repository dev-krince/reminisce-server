package com.krince.reminisce.application.service.wordbook

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.wordbook.command.GetWordbookCommand
import com.krince.reminisce.application.port.`in`.wordbook.command.SaveWordCommand
import com.krince.reminisce.application.port.`in`.wordbook.result.SavedWordResult
import com.krince.reminisce.application.port.`in`.wordbook.usecase.GetWordbookUseCase
import com.krince.reminisce.application.port.`in`.wordbook.usecase.SaveWordUseCase
import com.krince.reminisce.application.port.out.wordbook.CommandSavedWordPort
import com.krince.reminisce.application.port.out.wordbook.LoadSavedWordPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.domain.model.wordbook.SavedWord
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WordbookApplicationService(
    private val loadSavedWordPort: LoadSavedWordPort,
    private val commandSavedWordPort: CommandSavedWordPort,
    private val childAccessPort: ChildAccessPort,
) : SaveWordUseCase, GetWordbookUseCase {

    @Transactional
    override fun execute(command: SaveWordCommand): SavedWordResult {
        val childId = ChildId(command.childId)
        verifyOwnership(childId, UserId(command.guardianId))

        val savedWord: SavedWord = SavedWord.create(
            childId = childId,
            word = command.word,
            meaning = command.meaning,
            sourceSceneId = command.sourceSceneId,
        )

        return SavedWordResult.from(commandSavedWordPort.save(savedWord))
    }

    @Transactional(readOnly = true)
    override fun execute(command: GetWordbookCommand): List<SavedWordResult> {
        val childId = ChildId(command.childId)
        verifyOwnership(childId, UserId(command.guardianId))

        return loadSavedWordPort.findAllByChildId(childId).map { SavedWordResult.from(it) }
    }

    private fun verifyOwnership(childId: ChildId, guardianId: UserId) {
        val ownerId: UserId? = childAccessPort.findGuardianId(childId)
        if (ownerId == null || ownerId != guardianId) {
            throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        }
    }
}
