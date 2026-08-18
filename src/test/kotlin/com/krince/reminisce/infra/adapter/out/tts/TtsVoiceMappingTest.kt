package com.krince.reminisce.infra.adapter.out.tts

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TtsVoiceMappingTest : FunSpec({

    test("알려진 voiceProfile은 지정한 OpenAI 목소리로 매핑한다") {
        mapOpenAiVoice("young_woman_gentle", "nova") shouldBe "nova"
        mapOpenAiVoice("elderly_man_stern", "nova") shouldBe "onyx"
        mapOpenAiVoice("elderly_man_warm", "nova") shouldBe "echo"
    }

    test("대소문자·앞뒤 공백은 관대하게 처리한다") {
        mapOpenAiVoice("  ELDERLY_MAN_STERN ", "nova") shouldBe "onyx"
    }

    test("모르는 프로파일은 성별·연령 키워드로 추정한다") {
        mapOpenAiVoice("some_woman_voice", "alloy") shouldBe "nova"
        mapOpenAiVoice("random_male_guy", "alloy") shouldBe "onyx"
        mapOpenAiVoice("a_child_voice", "alloy") shouldBe "fable"
    }

    test("null·빈 값·추정 불가는 기본 목소리를 쓴다") {
        mapOpenAiVoice(null, "nova") shouldBe "nova"
        mapOpenAiVoice("   ", "nova") shouldBe "nova"
        mapOpenAiVoice("xyz", "nova") shouldBe "nova"
    }

    test("알려진 voiceProfile은 역할에 맞는 연출 지시를 준다") {
        voiceInstructions("narrator_female").contains("동화 구연가") shouldBe true
        voiceInstructions("qumi_child_friendly").contains("큐미") shouldBe true
        voiceInstructions("young_woman_gentle").contains("며느리") shouldBe true
        voiceInstructions("elderly_man_stern").contains("시아버지") shouldBe true
        voiceInstructions("elderly_man_warm").contains("이장님") shouldBe true
    }

    test("모르는 프로파일·null은 기본 연출 지시를 준다") {
        voiceInstructions(null).contains("동화 음성") shouldBe true
        voiceInstructions("xyz").contains("동화 음성") shouldBe true
    }
})
