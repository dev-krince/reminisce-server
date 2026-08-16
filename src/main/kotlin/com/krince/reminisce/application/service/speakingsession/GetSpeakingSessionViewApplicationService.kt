package com.krince.reminisce.application.service.speakingsession

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.`in`.speakingsession.command.GetSpeakingSessionViewCommand
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionViewResult
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.GetSpeakingSessionViewUseCase
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.application.port.out.tts.NARRATOR_VOICE_PROFILE
import com.krince.reminisce.application.port.out.tts.TtsPort
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.Scene
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetSpeakingSessionViewApplicationService(
    private val loadSpeakingSessionPort: LoadSpeakingSessionPort,
    private val childAccessPort: ChildAccessPort,
    private val storyAccessPort: StoryAccessPort,
    private val ttsPort: TtsPort,
) : GetSpeakingSessionViewUseCase {

    @Transactional(readOnly = true)
    override fun execute(command: GetSpeakingSessionViewCommand): SpeakingSessionViewResult {
        val session: SpeakingSession = loadOwnedSession(command.sessionId, command.guardianId)
        val storyId: StoryId = session.storyId
        val currentSceneId: String = session.currentSceneId ?: return introView(storyId)

        return sceneView(session, currentSceneId)
    }

    private fun loadOwnedSession(sessionId: String, guardianId: String): SpeakingSession {
        val session: SpeakingSession = loadSpeakingSessionPort.findById(SpeakingSessionId(sessionId))
            ?: throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        verifyOwnership(session, UserId(guardianId))

        return session
    }

    private fun verifyOwnership(session: SpeakingSession, guardianId: UserId) {
        val ownerId: UserId? = childAccessPort.findGuardianId(session.childId)
        if (ownerId == null || ownerId != guardianId) {
            throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        }
    }

    private fun introView(storyId: StoryId): SpeakingSessionViewResult {
        val intro: String = storyAccessPort.findIntro(storyId)
            ?: throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        val introAudio: String? = ttsPort.synthesize(intro, NARRATOR_VOICE_PROFILE)

        return SpeakingSessionViewResult.intro(intro, introAudio)
    }

    private fun sceneView(session: SpeakingSession, sceneId: String): SpeakingSessionViewResult {
        val scene: Scene = storyAccessPort.findScene(session.storyId, sceneId)
            ?.personalizedFor(childAccessPort.findChildName(session.childId))
            ?: throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        val openingAudio: String? = scene.characterOpening?.let { ttsPort.synthesize(it, scene.characterVoice?.voiceProfile) }
        val closingAudio: String? = scene.characterClosing?.let { ttsPort.synthesize(it, scene.characterVoice?.voiceProfile) }
        val narrationAudio: String? = narrationAudio(scene)
        val missionExplanationAudio: String? = missionExplanationAudio(scene)

        return SpeakingSessionViewResult.scene(scene, openingAudio, closingAudio, narrationAudio, missionExplanationAudio)
    }

    private fun narrationAudio(scene: Scene): String? =
        if (scene.sceneType == SceneType.NARRATION) {
            ttsPort.synthesize(scene.sceneDescription, NARRATOR_VOICE_PROFILE)
        } else {
            null
        }

    private fun missionExplanationAudio(scene: Scene): String? =
        scene.mission?.explanationText()?.let { ttsPort.synthesize(it, NARRATOR_VOICE_PROFILE) }
}
