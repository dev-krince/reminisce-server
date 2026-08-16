package com.krince.reminisce.infra.adapter.out.mission

import com.krince.reminisce.application.port.out.mission.MissionJudgement

data class MissionJudgeLlmResult(
    val passed: Boolean? = null,
    val coveredCriteria: List<String> = emptyList(),
    val missingCriteria: List<String> = emptyList(),
    val hint: String? = null,
) {
    fun toMissionJudgement(): MissionJudgement {
        val decided: Boolean = passed ?: false
        if (decided) {
            return MissionJudgement(passed = true, hint = null)
        }

        return MissionJudgement(passed = false, hint = resolveHint())
    }

    private fun resolveHint(): String {
        val cleanHint: String? = hint?.trim()?.takeIf { it.isNotBlank() }
        if (cleanHint != null) {
            return cleanHint
        }

        val cleanMissing: List<String> = missingCriteria.mapNotNull { it.trim().takeIf { item -> item.isNotBlank() } }
        if (cleanMissing.isNotEmpty()) {
            return MISSING_HINT_PREFIX + cleanMissing.joinToString(MISSING_HINT_SEPARATOR) + MISSING_HINT_SUFFIX
        }

        return DEFAULT_HINT
    }

    companion object {
        const val DEFAULT_HINT = "조금 더 자세히 이야기해 보세요."
        const val MISSING_HINT_PREFIX = "이번에는 "
        const val MISSING_HINT_SEPARATOR = ", "
        const val MISSING_HINT_SUFFIX = "에 대해서도 이야기해 볼까요?"
    }
}
