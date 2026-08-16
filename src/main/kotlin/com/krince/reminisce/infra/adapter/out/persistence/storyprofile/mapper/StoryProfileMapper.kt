package com.krince.reminisce.infra.adapter.out.persistence.storyprofile.mapper

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.profileinterview.vo.ProfileInterviewId
import com.krince.reminisce.domain.model.storyprofile.StoryProfile
import com.krince.reminisce.domain.model.storyprofile.vo.StoryProfileId
import com.krince.reminisce.infra.adapter.out.persistence.storyprofile.entity.StoryProfileOrmEntity

object StoryProfileMapper {

    fun toEntity(profile: StoryProfile): StoryProfileOrmEntity = StoryProfileOrmEntity(
        profileId = profile.profileId.value,
        childId = profile.childId.value,
        interviewId = profile.interviewId.value,
        interestTopics = profile.interestTopics,
        strengths = profile.strengths,
        practicePoints = profile.practicePoints,
        speechAnalyses = profile.speechAnalyses,
        createdAt = profile.createdAt,
    )

    fun toDomain(entity: StoryProfileOrmEntity): StoryProfile = StoryProfile(
        profileId = StoryProfileId(entity.profileId),
        childId = ChildId(entity.childId),
        interviewId = ProfileInterviewId(entity.interviewId),
        interestTopics = entity.interestTopics,
        strengths = entity.strengths,
        practicePoints = entity.practicePoints,
        speechAnalyses = entity.speechAnalyses,
        createdAt = entity.createdAt,
    )
}
