package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.application.port.`in`.savedstory.command.AddStoryBookmarkCommand
import com.krince.reminisce.application.port.`in`.savedstory.command.GetBookmarkedStoriesCommand
import com.krince.reminisce.application.port.`in`.savedstory.command.RemoveStoryBookmarkCommand
import com.krince.reminisce.application.port.`in`.savedstory.result.BookmarkedStoryResult
import com.krince.reminisce.application.port.`in`.savedstory.usecase.AddStoryBookmarkUseCase
import com.krince.reminisce.application.port.`in`.savedstory.usecase.GetBookmarkedStoriesUseCase
import com.krince.reminisce.application.port.`in`.savedstory.usecase.RemoveStoryBookmarkUseCase
import com.krince.reminisce.infra.adapter.`in`.dto.savedstory.request.AddStoryBookmarkRequest
import com.krince.reminisce.infra.adapter.`in`.dto.savedstory.response.BookmarkedStoryResponse
import com.krince.reminisce.infra.adapter.`in`.dto.savedstory.response.bookmarkedStoryResponse
import com.krince.reminisce.infra.security.CustomUserDetails
import com.krince.reminisce.shared.response.SuccessResponse
import com.krince.reminisce.shared.response.SuccessResponseCode.CREATED
import com.krince.reminisce.shared.response.SuccessResponseCode.NO_CONTENT
import com.krince.reminisce.shared.response.SuccessResponseCode.OK
import com.krince.reminisce.shared.response.successResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/children/{childId}/bookmarked-stories")
class StoryBookmarkControllerImpl(
    private val addStoryBookmarkUseCase: AddStoryBookmarkUseCase,
    private val getBookmarkedStoriesUseCase: GetBookmarkedStoriesUseCase,
    private val removeStoryBookmarkUseCase: RemoveStoryBookmarkUseCase,
) : StoryBookmarkController {

    @PostMapping
    override fun addBookmark(
        @PathVariable childId: String,
        @Valid @RequestBody request: AddStoryBookmarkRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<BookmarkedStoryResponse>> {
        val command = AddStoryBookmarkCommand(
            childId = childId,
            guardianId = userDetails.getId(),
            storyId = request.storyId,
        )
        val result: BookmarkedStoryResult = addStoryBookmarkUseCase.execute(command)
        val response: BookmarkedStoryResponse = bookmarkedStoryResponse(result = result)
        val responseBody: SuccessResponse<BookmarkedStoryResponse> =
            successResponse(responseCode = CREATED, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }

    @GetMapping
    override fun getBookmarks(
        @PathVariable childId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<List<BookmarkedStoryResponse>>> {
        val command = GetBookmarkedStoriesCommand(childId = childId, guardianId = userDetails.getId())
        val results: List<BookmarkedStoryResult> = getBookmarkedStoriesUseCase.execute(command)
        val response: List<BookmarkedStoryResponse> = results.map { bookmarkedStoryResponse(result = it) }
        val responseBody: SuccessResponse<List<BookmarkedStoryResponse>> =
            successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }

    @DeleteMapping("/{storyId}")
    override fun removeBookmark(
        @PathVariable childId: String,
        @PathVariable storyId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Void> {
        val command = RemoveStoryBookmarkCommand(
            childId = childId,
            guardianId = userDetails.getId(),
            storyId = storyId,
        )
        removeStoryBookmarkUseCase.execute(command)

        return ResponseEntity.status(NO_CONTENT.code).build()
    }
}
