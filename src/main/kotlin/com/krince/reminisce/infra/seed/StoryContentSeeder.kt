package com.krince.reminisce.infra.seed

import com.krince.reminisce.domain.model.story.CharacterVoice
import com.krince.reminisce.domain.model.story.Mission
import com.krince.reminisce.domain.model.story.Scene
import com.krince.reminisce.domain.model.story.Story
import com.krince.reminisce.domain.model.story.VoiceAgeGroup
import com.krince.reminisce.domain.model.story.VoiceGender
import com.krince.reminisce.domain.model.story.vo.Difficulty
import com.krince.reminisce.domain.model.story.vo.PostActivityConfig
import com.krince.reminisce.domain.model.story.vo.SceneId
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.story.vo.StoryGenre
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.StoryStatus
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.infra.adapter.out.persistence.story.SceneRepository
import com.krince.reminisce.infra.adapter.out.persistence.story.StoryRepository
import com.krince.reminisce.infra.adapter.out.persistence.story.StoryTopicRepository
import com.krince.reminisce.infra.adapter.out.persistence.story.dto.StoryAggregateEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.mapper.StoryMapper
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!prod & !test & !localtest")
class StoryContentSeeder(
    private val storyRepository: StoryRepository,
    private val sceneRepository: SceneRepository,
    private val storyTopicRepository: StoryTopicRepository,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        val aggregateEntity: StoryAggregateEntity = StoryMapper.toEntity(bangguiStory())

        val existing: StoryOrmEntity? = storyRepository.findById(BANGGUI_STORY_ID).orElse(null)
        if (existing != null) {
            backfillGenre(existing, aggregateEntity.storyOrmEntity)
            return
        }

        storyRepository.save(aggregateEntity.storyOrmEntity)
        sceneRepository.saveAll(aggregateEntity.sceneOrmEntities)
        storyTopicRepository.saveAll(aggregateEntity.storyTopicOrmEntities)
    }

    private fun backfillGenre(existing: StoryOrmEntity, seeded: StoryOrmEntity) {
        if (existing.storyGenre != null) {
            return
        }

        storyRepository.save(withGenre(existing, seeded.storyGenre))
    }

    private fun withGenre(source: StoryOrmEntity, storyGenre: String?): StoryOrmEntity = StoryOrmEntity(
        storyId = source.storyId,
        title = source.title,
        summary = source.summary,
        intro = source.intro,
        situation = source.situation,
        childRole = source.childRole,
        difficulty = source.difficulty,
        estimatedMinutes = source.estimatedMinutes,
        representativeImageUrl = source.representativeImageUrl,
        status = source.status,
        storyGenre = storyGenre,
        postActivityConfig = source.postActivityConfig,
    )

    private fun bangguiPostActivityConfig(): PostActivityConfig = PostActivityConfig(
        cards = listOf(
            PostActivityConfig.Card(id = "card_banggui_1", text = "며느리가 방귀를 꾹 참는 모습", correctOrder = 1),
            PostActivityConfig.Card(id = "card_banggui_2", text = "며느리의 방귀로 시아버지의 갓이 날아가는 모습", correctOrder = 2),
            PostActivityConfig.Card(id = "card_banggui_3", text = "마을 사람들이 높은 배나무 때문에 고민하는 모습", correctOrder = 3),
            PostActivityConfig.Card(id = "card_banggui_4", text = "며느리가 방귀로 배를 떨어뜨려 사람들을 돕는 모습", correctOrder = 4),
        ),
        retellingKeywords = listOf("며느리", "방귀", "시아버지", "배나무", "특별한 힘"),
    )

    private fun bangguiStory(): Story = Story(
        storyId = StoryId(BANGGUI_STORY_ID),
        title = "방귀 뀌는 며느리",
        summary = "큰 방귀를 부끄러워하던 며느리가 자신의 다름을 장점으로 바꾸는 이야기",
        intro = BANGGUI_INTRO,
        situation = "큰 방귀 때문에 며느리가 집에서 쫓겨날 위기에 놓였어요.",
        childRole = "며느리의 방귀가 특별한 장점이 될 수 있도록 도와주세요.",
        difficulty = Difficulty("보통"),
        estimatedMinutes = 20,
        representativeImageUrl = BANGGUI_COVER_IMAGE_URL,
        status = StoryStatus.PUBLISHED,
        postActivityConfig = bangguiPostActivityConfig(),
        topics = listOf("다름", "자기이해", "장점 발견"),
        genre = StoryGenre.FOLKTALE,
        scenes = bangguiScenes(),
    )

    private fun bangguiScenes(): List<Scene> = listOf(
        narrationScene(
            sceneId = "sc_banggui_01",
            sceneOrder = 1,
            sceneDescription = BANGGUI_INTRO,
        ),
        narrationScene(
            sceneId = "sc_banggui_02",
            sceneOrder = 2,
            sceneDescription = "그래서 며느리는 방귀가 나오려고 할 때마다 꾹꾹 참았습니다. " +
                "오래 참다 보니 배는 점점 빵빵하게 부풀어 올랐고 얼굴은 노랗게 변했습니다. " +
                "몸도 마음도 힘들었지만 가족들에게 솔직하게 말하지 못했습니다.",
        ),
        dialogueScene(
            sceneId = "sc_banggui_03",
            sceneOrder = 3,
            sceneDescription = "'내 방귀가 너무 크다는 걸 알면 나를 이상하게 생각하지 않을까?' 며느리는 걱정이 많았습니다. " +
                "방귀는 누구에게나 자연스러운 일이지만 며느리에게는 큰 비밀처럼 느껴졌습니다. " +
                "한 번 나오면 지붕이 흔들릴 만큼 우렁찼기 때문에 더욱 부끄러웠습니다.",
            characterName = "ch_banggui_daughter_in_law",
            characterDisplayName = "방귀쟁이 며느리",
            characterOpening = "ㅇㅇ아, 내 방귀가 너무 크다는 걸 알면 가족들이 나를 이상하게 생각하지 않을까?",
            characterClosing = "그래도 아직은 못 말하겠어. 조금만 더 참아 볼게.",
            sceneGoal = "방귀를 숨기고 싶어하는 며느리의 입장을 이해하고 공감해주며, 문제를 숨기지 않고 솔직하게 말할 용기를 준다",
            requiredElements = listOf(
                ThinkingElement.PERSPECTIVE,
                ThinkingElement.EMOTION,
                ThinkingElement.REASON,
                ThinkingElement.SOLUTION,
            ),
            maxTurns = 4,
            characterVoice = DAUGHTER_IN_LAW_VOICE,
        ),
        narrationScene(
            sceneId = "sc_banggui_04",
            sceneOrder = 4,
            sceneDescription = "더 이상 참을 수 없어 조심스럽게 살짝만 뀌려 했지만, 오래 참은 탓에 훨씬 크게 터져 나왔습니다. " +
                "마당 먼지가 날아가고 기왓장이 달그락거리고 시아버지의 갓까지 휙 날아가 버렸습니다.",
        ),
        dialogueScene(
            sceneId = "sc_banggui_05",
            sceneOrder = 5,
            sceneDescription = "시아버지는 깜짝 놀라 화를 냈습니다. \"우리 집안이 다 흔들리는구나!\" 며느리는 고개를 숙였습니다. " +
                "시아버지는 이런 며느리와는 함께 살 수 없다며 친정에 데려다주러 길을 나섰습니다.",
            characterName = "ch_banggui_father_in_law",
            characterDisplayName = "시아버지",
            characterOpening = "아이고 이게 무슨 일이냐! 우리 집안이 다 흔들리는구나! 이렇게 창피한 며느리와 함께 못살겠다! 그렇지 않니?",
            characterClosing = "흥, 그래도 도저히 이런 며느리와는 함께 살 수 없으니 친정으로 데려다줘야겠다.",
            sceneGoal = "시아버지가 놀란 마음을 이해하면서도, 며느리가 일부러 그런 것이 아니라 오래 참아 힘들었음을 말하고 따뜻하게 이해해 달라 설득한다",
            requiredElements = listOf(
                ThinkingElement.PERSPECTIVE,
                ThinkingElement.EMOTION,
                ThinkingElement.REASON,
                ThinkingElement.SOLUTION,
            ),
            maxTurns = 5,
            characterVoice = FATHER_IN_LAW_VOICE,
        ),
        narrationScene(
            sceneId = "sc_banggui_06",
            sceneOrder = 6,
            sceneDescription = "길가에 아주 높은 배나무가 서 있었습니다. " +
                "노랗고 탐스러운 배가 주렁주렁 매달렸지만 너무 높아 아무도 딸 수 없었습니다.",
        ),
        dialogueScene(
            sceneId = "sc_banggui_07",
            sceneOrder = 7,
            sceneDescription = "며느리는 '내 방귀가 지붕도 흔들 만큼 세다면 저 높은 배를 떨어뜨릴 수도 있지 않을까?' 생각했습니다. " +
                "사람들을 피하게 하고 나무를 향해 힘을 모아 크게 방귀를 뀌자 배들이 우수수 떨어졌습니다. " +
                "시끄럽고 별나다 여겼던 방귀가 모두를 도울 특별한 힘이었던 것입니다.",
            characterName = "ch_banggui_village_chief",
            characterDisplayName = "마을 이장",
            characterOpening = "이 배나무는 해마다 탐스러운 배가 열리지만, 너무 높아서 아무도 딸 수가 없었소. 무슨 뾰족한 방법이 없겠는가?",
            characterClosing = "아이고, 방귀 뀌는 며느리 덕분에 온 마을이 배 잔치를 할 수 있겠구려, 고맙소!",
            sceneGoal = "높은 배나무의 배를 떨어뜨릴 방법을 생각하고, 며느리의 큰 방귀를 안전하게 사용할 해결책을 제안한다",
            requiredElements = listOf(
                ThinkingElement.SOLUTION,
                ThinkingElement.REASON,
                ThinkingElement.REQUEST,
                ThinkingElement.RESULT,
            ),
            maxTurns = 5,
            mission = Mission(
                goal = BANGGUI_07_MISSION_GOAL,
                examples = BANGGUI_07_MISSION_EXAMPLES,
            ),
            characterVoice = VILLAGE_CHIEF_VOICE,
        ),
        narrationScene(
            sceneId = "sc_banggui_08",
            sceneOrder = 8,
            sceneDescription = "\"남들과 다르다고 해서 부끄러운 것이 아닌데, 내가 너무 성급하게 생각했다.\"",
        ),
        dialogueScene(
            sceneId = "sc_banggui_09",
            sceneOrder = 9,
            sceneDescription = "며느리는 자신이 숨기고 싶어 했던 특징이 누군가에게 도움이 될 수도 있음을 알게 되었습니다. " +
                "이후 무조건 참지 않고, 힘들 때는 솔직히 말하고 미리 알려 주었습니다. " +
                "마을 사람들도 며느리에게 도움을 부탁했습니다.",
            characterName = "ch_banggui_daughter_in_law",
            characterDisplayName = "방귀쟁이 며느리",
            characterOpening = "ㅇㅇ이 덕분에 내 방귀가 누군가에게 도움이 될 수 있다는 걸 처음 알았어. " +
                "이제는 방귀 소리가 큰 걸 부끄러워하지 않아도 될까?",
            characterClosing = "이제는 부끄러워하며 숨기지 않고, 조심해서 좋은 일에 써 볼게",
            sceneGoal = "다름을 인정하고, 자신의 특징을 긍정적으로 받아들이는 태도를 말한다",
            requiredElements = listOf(
                ThinkingElement.EMOTION,
                ThinkingElement.PERSPECTIVE,
                ThinkingElement.RESULT,
                ThinkingElement.SOLUTION,
            ),
            maxTurns = 4,
            mission = Mission(
                goal = BANGGUI_09_MISSION_GOAL,
                examples = BANGGUI_09_MISSION_EXAMPLES,
            ),
            characterVoice = DAUGHTER_IN_LAW_VOICE,
        ),
    )

    private fun narrationScene(sceneId: String, sceneOrder: Int, sceneDescription: String): Scene = Scene(
        sceneId = SceneId(sceneId),
        storyId = StoryId(BANGGUI_STORY_ID),
        sceneOrder = sceneOrder,
        sceneType = SceneType.NARRATION,
        sceneDescription = sceneDescription,
        imageUrl = sceneImageUrl(sceneOrder),
    )

    private fun dialogueScene(
        sceneId: String,
        sceneOrder: Int,
        sceneDescription: String,
        characterName: String,
        characterDisplayName: String,
        characterOpening: String,
        characterClosing: String,
        sceneGoal: String,
        requiredElements: List<ThinkingElement>,
        maxTurns: Int,
        mission: Mission? = null,
        characterVoice: CharacterVoice? = null,
    ): Scene = Scene(
        sceneId = SceneId(sceneId),
        storyId = StoryId(BANGGUI_STORY_ID),
        sceneOrder = sceneOrder,
        sceneType = SceneType.DIALOGUE,
        sceneDescription = sceneDescription,
        characterName = characterName,
        characterDisplayName = characterDisplayName,
        characterOpening = characterOpening,
        characterClosing = characterClosing,
        conflict = null,
        sceneGoal = sceneGoal,
        requiredElements = requiredElements,
        preferredTurns = null,
        maxTurns = maxTurns,
        mission = mission,
        characterVoice = characterVoice,
        imageUrl = sceneImageUrl(sceneOrder),
    )

    private fun sceneImageUrl(sceneOrder: Int): String =
        SCENE_IMAGE_URL_PREFIX + sceneOrder.toString().padStart(SCENE_ORDER_PAD_WIDTH, '0') + SCENE_IMAGE_URL_SUFFIX

    private companion object {
        const val BANGGUI_STORY_ID = "s_banggui_daughter_in_law_001"

        const val BANGGUI_COVER_IMAGE_URL = "/files/banggui-cover.png"
        const val SCENE_IMAGE_URL_PREFIX = "/files/banggui-scene-"
        const val SCENE_IMAGE_URL_SUFFIX = ".png"
        const val SCENE_ORDER_PAD_WIDTH = 2

        const val VOICE_PROFILE_YOUNG_WOMAN_GENTLE = "young_woman_gentle"
        const val VOICE_PROFILE_ELDERLY_MAN_STERN = "elderly_man_stern"
        const val VOICE_PROFILE_ELDERLY_MAN_WARM = "elderly_man_warm"

        val DAUGHTER_IN_LAW_VOICE = CharacterVoice(
            gender = VoiceGender.FEMALE,
            ageGroup = VoiceAgeGroup.ADULT,
            voiceProfile = VOICE_PROFILE_YOUNG_WOMAN_GENTLE,
        )
        val FATHER_IN_LAW_VOICE = CharacterVoice(
            gender = VoiceGender.MALE,
            ageGroup = VoiceAgeGroup.ELDER,
            voiceProfile = VOICE_PROFILE_ELDERLY_MAN_STERN,
        )
        val VILLAGE_CHIEF_VOICE = CharacterVoice(
            gender = VoiceGender.MALE,
            ageGroup = VoiceAgeGroup.ELDER,
            voiceProfile = VOICE_PROFILE_ELDERLY_MAN_WARM,
        )
        const val BANGGUI_INTRO = "옛날 어느 마을에 방귀를 아주 크게 뀌는 며느리가 살았습니다. " +
            "며느리는 시집에 온 뒤로 늘 얌전하고 예의 바르게 보이고 싶었습니다. " +
            "시댁 식구들이 자신을 이상하게 볼까 봐 걱정했기 때문입니다."

        const val BANGGUI_07_MISSION_GOAL = "높은 배나무의 배를 떨어뜨리기 위해 며느리의 방귀를 안전하게 사용할 수 있는 방법 찾기"
        val BANGGUI_07_MISSION_EXAMPLES = listOf(
            "무엇을 사용할 것인지",
            "주변 사람들과 시아버지는 어디로 피해야 할지",
            "며느리에게 어떻게 부탁할 것인지",
            "그 결과 어떤 일이 생길지",
        )

        const val BANGGUI_09_MISSION_GOAL = "처음에는 단점처럼 보였지만 좋은 일에 쓸 수 있는 특징 찾기"
        val BANGGUI_09_MISSION_EXAMPLES = listOf(
            "목소리가 큰 친구는 멀리 있는 사람을 부를 수 있어요",
            "질문이 많은 친구는 새로운 생각을 찾을 수 있어요",
            "힘이 센 친구는 무거운 물건을 옮길 때 도울 수 있어요",
            "조용한 친구는 다른 사람의 말을 잘 들어 줄 수 있어요",
        )
    }
}
