package com.krince.reminisce.infra.adapter.out.interviewreply

import com.krince.reminisce.application.port.out.profileinterview.InterviewReplyContext
import com.krince.reminisce.application.port.out.profileinterview.InterviewReplyPort
import com.krince.reminisce.domain.model.profileinterview.vo.InterviewStage
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["analysis.engine"], havingValue = "stub", matchIfMissing = true)
class InterviewReplyStubAdapter : InterviewReplyPort {

    override fun generate(context: InterviewReplyContext): String = when (context.stage) {
        InterviewStage.FREE_TALK -> "그렇구나! 그거의 어떤 점이 좋아?"
        InterviewStage.EXPERIENCE -> "그걸 실제로 본 적 있어? 그때 이야기를 들려줘."
        InterviewStage.STORY_LISTENING ->
            "이번에는 큐미가 짧은 이야기를 들려줄게. 잘 들어봐! " +
                "어느 날 토끼가 숲에서 놀고 있었어. 그런데 여우가 찾아와서 말했어. " +
                "\"토끼야! 우리 내일 여기서 다시 만나자.\" 토끼는 여우와 약속을 하고 집으로 돌아갔어. " +
                "토끼와 여우는 무엇을 약속했을까?"
        InterviewStage.CHARACTER_FEELING -> "토끼는 어떤 기분일까?"
        InterviewStage.STORY_CONTINUATION -> "그다음에는 무슨 일이 생길까?"
        InterviewStage.CHILD_QUESTION -> "이제 네가 큐미에게 물어볼 차례야! 궁금한 게 있으면 뭐든 물어봐."
        InterviewStage.CLOSING -> qumiClosingLine(context.childName)
    }
}
