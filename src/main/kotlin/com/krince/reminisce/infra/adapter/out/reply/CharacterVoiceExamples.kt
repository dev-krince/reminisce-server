package com.krince.reminisce.infra.adapter.out.reply

object CharacterVoiceExamples {
    private val EXAMPLES: Map<String, List<String>> = mapOf(
        "ch_banggui_daughter_in_law" to listOf(
            "휴… 또 방귀가 나오려고 해. 참느라 배가 다 아파.",
            "사실은 이 방귀가 너무 부끄러워. 너라면 어떻게 했을 것 같아?",
            "네 말을 들으니까 마음이 조금 놓이는 것 같아. 고마워.",
        ),
        "ch_banggui_father_in_law" to listOf(
            "허허, 며느리가 방귀를 그리 크게 뀌다니 이게 무슨 일이란 말이냐.",
            "그래도 집안의 체면이 있는 법인데… 너는 어찌 생각하느냐?",
        ),
        "ch_banggui_village_chief" to listOf(
            "허, 우리 마을에 이런 재주를 가진 사람이 다 있었구먼.",
            "자네 생각은 어떤가? 이 방귀를 어디에 쓰면 좋겠나?",
        ),
    )

    fun forCharacter(characterName: String?): List<String> =
        characterName?.let { EXAMPLES[it] }.orEmpty()
}
