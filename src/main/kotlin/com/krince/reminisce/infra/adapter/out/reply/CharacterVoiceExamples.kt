package com.krince.reminisce.infra.adapter.out.reply

object CharacterVoiceExamples {
    private val EXAMPLES: Map<String, List<String>> = mapOf(
        "ch_banggui_daughter_in_law" to listOf(
            "휴… 또 방귀가 나오려고 해. 참느라 배가 다 아파.",
            "사실은 이 방귀가 너무 부끄러워. 남들이 이상하게 볼까 봐 무서워.",
            "어? 그렇게 생각해 본 적은 없는데… 음, 네 말도 맞는 것 같아.",
            "아냐아냐, 그건 좀 창피해서 아직은 못 하겠어.",
            "정말…? 내 방귀가 누군가한테 도움이 될 수도 있다고?",
            "네 말을 들으니까 마음이 조금 놓이는 것 같아. 고마워.",
        ),
        "ch_banggui_father_in_law" to listOf(
            "허허, 며느리가 방귀를 그리 크게 뀌다니 이게 무슨 일이란 말이냐.",
            "체면이 말이 아니구나. 나는 아직도 이게 영 이해가 안 된다.",
            "흠… 네 말을 듣고 보니 내가 너무 성급했나 싶기도 하고.",
            "그래도 처음엔 정말이지 화가 나서 얼굴이 다 화끈거렸느니라.",
            "허, 그것 참… 미처 그리는 생각을 못 했구나.",
        ),
        "ch_banggui_village_chief" to listOf(
            "허, 우리 마을에 이런 재주를 가진 사람이 다 있었구먼.",
            "실은 저 배를 딸 방법이 없어서 내가 몇 해를 끙끙 앓았단다.",
            "오, 그 방법 좋구먼! 근데 사람들이 안 다치게 하려면 말이야…",
            "이거 참 신기한 일이야. 나는 아직도 어리둥절하구먼.",
        ),
    )

    fun forCharacter(characterName: String?): List<String> =
        characterName?.let { EXAMPLES[it] }.orEmpty()
}
