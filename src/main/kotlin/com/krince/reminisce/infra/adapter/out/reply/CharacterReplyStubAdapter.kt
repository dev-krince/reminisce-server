package com.krince.reminisce.infra.adapter.out.reply

import com.krince.reminisce.application.port.out.reply.CharacterReplyContext
import com.krince.reminisce.application.port.out.reply.CharacterReplyPort
import com.krince.reminisce.domain.model.speakingsession.vo.ResponseMode
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["analysis.engine"], havingValue = "stub", matchIfMissing = true)
class CharacterReplyStubAdapter : CharacterReplyPort {

    private val normalTemplate: String = "%s: 네 이야기를 잘 들었어. 나도 그렇게 느꼈어."
    private val guidedTemplate: String = "%s: 그런데 %s에 대해서는 어떻게 생각해?"
    private val guidedFallbackTemplate: String = "%s: 조금만 더 자세히 이야기해 줄래?"

    override fun generate(context: CharacterReplyContext): String {
        if (context.mode == ResponseMode.GUIDED) {
            return guidedReply(context)
        }

        return normalTemplate.format(context.characterDisplayName)
    }

    private fun guidedReply(context: CharacterReplyContext): String {
        val target = context.guidanceTarget
            ?: return guidedFallbackTemplate.format(context.characterDisplayName)

        return guidedTemplate.format(context.characterDisplayName, target.name)
    }
}
