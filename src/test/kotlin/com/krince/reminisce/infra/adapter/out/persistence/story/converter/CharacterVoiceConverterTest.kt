package com.krince.reminisce.infra.adapter.out.persistence.story.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.krince.reminisce.domain.model.story.CharacterVoice
import com.krince.reminisce.domain.model.story.VoiceAgeGroup
import com.krince.reminisce.domain.model.story.VoiceGender
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("CharacterVoiceConverter 단위테스트")
class CharacterVoiceConverterTest : FunSpec({

    val converter = CharacterVoiceConverter()
    val objectMapper = ObjectMapper()

    fun characterVoice(): CharacterVoice = CharacterVoice(
        gender = VoiceGender.FEMALE,
        ageGroup = VoiceAgeGroup.ADULT,
        voiceProfile = "young_woman_gentle",
    )

    context("convertToDatabaseColumn") {
        test("DB 정본 JSON 키(gender·ageGroup·voiceProfile)로 직렬화한다") {
            val dbData = converter.convertToDatabaseColumn(characterVoice())

            dbData.shouldNotBeNull()
            val rootNode = objectMapper.readTree(dbData)
            rootNode.path("gender").asText() shouldBe "FEMALE"
            rootNode.path("ageGroup").asText() shouldBe "ADULT"
            rootNode.path("voiceProfile").asText() shouldBe "young_woman_gentle"
        }

        test("null이면 null을 반환한다") {
            converter.convertToDatabaseColumn(null) shouldBe null
        }
    }

    context("convertToEntityAttribute") {
        test("DB 정본 예시 JSON을 도메인 값 객체로 복원한다") {
            val dbData = """
                {
                  "gender": "FEMALE",
                  "ageGroup": "ADULT",
                  "voiceProfile": "young_woman_gentle"
                }
            """.trimIndent()

            val restored = converter.convertToEntityAttribute(dbData)

            restored shouldBe characterVoice()
        }

        test("null이면 null을 반환한다") {
            converter.convertToEntityAttribute(null) shouldBe null
        }
    }

    context("왕복") {
        test("직렬화 후 복원하면 같은 값 객체가 된다") {
            val restored = converter.convertToEntityAttribute(converter.convertToDatabaseColumn(characterVoice()))

            restored shouldBe characterVoice()
        }

        test("MALE·ELDER·elderly_man_stern도 왕복 보존된다") {
            val elderVoice = CharacterVoice(
                gender = VoiceGender.MALE,
                ageGroup = VoiceAgeGroup.ELDER,
                voiceProfile = "elderly_man_stern",
            )

            val restored = converter.convertToEntityAttribute(converter.convertToDatabaseColumn(elderVoice))

            restored shouldBe elderVoice
        }
    }
})
