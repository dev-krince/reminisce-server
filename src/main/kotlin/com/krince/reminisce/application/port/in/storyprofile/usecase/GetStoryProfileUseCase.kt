package com.krince.reminisce.application.port.`in`.storyprofile.usecase

import com.krince.reminisce.application.port.`in`.storyprofile.command.GetStoryProfileCommand
import com.krince.reminisce.application.port.`in`.storyprofile.result.StoryProfileResult

interface GetStoryProfileUseCase {
    fun execute(command: GetStoryProfileCommand): StoryProfileResult
}
