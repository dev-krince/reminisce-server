package com.krince.reminisce.domain.model.profileinterview.vo

enum class InterviewStage(val purpose: String, val targetChildTurns: Int) {
    FREE_TALK("관심사 + 자발적 말하기", 3),
    EXPERIENCE("경험 회상 + 이야기 구성", 4),
    STORY_LISTENING("이야기 이해 + 순서 파악", 2),
    CHARACTER_FEELING("감정 이해 + 이유 설명", 4),
    STORY_CONTINUATION("이야기 구성 + 상상", 3),
    CHILD_QUESTION("자발적인 질문", 2),
    CLOSING("교육적 마무리", 0),
    ;

    fun next(): InterviewStage? = entries.getOrNull(ordinal + 1)
}
