package com.krince.reminisce.domain.model.story

import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("ChildNamePersonalizer 단위테스트")
class ChildNamePersonalizerTest : FunSpec({

    context("호격 조사 아/야 치환") {
        test("받침 있는 이름은 '아'를 붙인다") {
            ChildNamePersonalizer.personalize("ㅇㅇ아, 안녕?", "민준") shouldBe "민준아, 안녕?"
        }

        test("받침 없는 이름은 '야'를 붙인다") {
            ChildNamePersonalizer.personalize("ㅇㅇ아, 안녕?", "지우") shouldBe "지우야, 안녕?"
        }
    }

    context("주격 보조 이/생략 치환") {
        test("받침 있는 이름은 '이'를 붙인다") {
            ChildNamePersonalizer.personalize("ㅇㅇ이 덕분에 좋았어.", "민준") shouldBe "민준이 덕분에 좋았어."
        }

        test("받침 없는 이름은 조사를 생략한다") {
            ChildNamePersonalizer.personalize("ㅇㅇ이 덕분에 좋았어.", "지우") shouldBe "지우 덕분에 좋았어."
        }
    }

    context("자리표시자가 없거나 이름이 비면 안전하게 처리한다") {
        test("ㅇㅇ가 없으면 원문을 그대로 반환한다") {
            ChildNamePersonalizer.personalize("여는 대사", "지우") shouldBe "여는 대사"
        }

        test("이름이 null이면 '친구'로 대체한다") {
            ChildNamePersonalizer.personalize("ㅇㅇ아, 안녕?", null) shouldBe "친구야, 안녕?"
        }

        test("이름이 공백이면 '친구'로 대체한다") {
            ChildNamePersonalizer.personalize("ㅇㅇ아, 안녕?", "   ") shouldBe "친구야, 안녕?"
        }
    }

    context("받침 유무 판정 케이스") {
        test("여러 이름의 호격 조사를 올바르게 선택한다") {
            forAll(
                row("서준", "서준아"),
                row("하윤", "하윤아"),
                row("시우", "시우야"),
                row("아라", "아라야"),
                row("별", "별아"),
            ) { name, expected ->
                ChildNamePersonalizer.personalize("ㅇㅇ아", name) shouldBe expected
            }
        }
    }
})
