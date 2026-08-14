package com.krince.reminisce.domain.model.story

object ChildNamePersonalizer {
    private const val PLACEHOLDER = "ㅇㅇ"
    private const val FALLBACK_NAME = "친구"
    private const val HANGUL_BASE = 0xAC00
    private const val HANGUL_LAST = 0xD7A3
    private const val JONGSEONG_COUNT = 28

    fun personalize(text: String, childName: String?): String {
        if (!text.contains(PLACEHOLDER)) {
            return text
        }
        val name: String = childName?.trim()?.takeIf { it.isNotEmpty() } ?: FALLBACK_NAME

        return text
            .replace("${PLACEHOLDER}아", name + vocativeParticle(name))
            .replace("${PLACEHOLDER}이", name + subjectSuffix(name))
            .replace(PLACEHOLDER, name)
    }

    private fun vocativeParticle(name: String): String =
        if (endsWithFinalConsonant(name)) "아" else "야"

    private fun subjectSuffix(name: String): String =
        if (endsWithFinalConsonant(name)) "이" else ""

    private fun endsWithFinalConsonant(name: String): Boolean {
        val last: Char = name.lastOrNull() ?: return false
        val code: Int = last.code
        if (code !in HANGUL_BASE..HANGUL_LAST) {
            return false
        }

        return (code - HANGUL_BASE) % JONGSEONG_COUNT != 0
    }
}
