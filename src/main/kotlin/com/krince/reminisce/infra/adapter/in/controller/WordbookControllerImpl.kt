package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.application.port.`in`.wordbook.command.GetWordbookCommand
import com.krince.reminisce.application.port.`in`.wordbook.command.SaveWordCommand
import com.krince.reminisce.application.port.`in`.wordbook.result.SavedWordResult
import com.krince.reminisce.application.port.`in`.wordbook.usecase.GetWordbookUseCase
import com.krince.reminisce.application.port.`in`.wordbook.usecase.SaveWordUseCase
import com.krince.reminisce.infra.adapter.`in`.dto.wordbook.request.SaveWordRequest
import com.krince.reminisce.infra.adapter.`in`.dto.wordbook.response.WordbookResponse
import com.krince.reminisce.infra.adapter.`in`.dto.wordbook.response.wordbookResponse
import com.krince.reminisce.infra.security.CustomUserDetails
import com.krince.reminisce.shared.response.SuccessResponse
import com.krince.reminisce.shared.response.SuccessResponseCode.CREATED
import com.krince.reminisce.shared.response.SuccessResponseCode.OK
import com.krince.reminisce.shared.response.successResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/children/{childId}/words")
class WordbookControllerImpl(
    private val saveWordUseCase: SaveWordUseCase,
    private val getWordbookUseCase: GetWordbookUseCase,
) : WordbookController {

    @PostMapping
    override fun saveWord(
        @PathVariable childId: String,
        @Valid @RequestBody request: SaveWordRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<WordbookResponse>> {
        val command = SaveWordCommand(
            childId = childId,
            guardianId = userDetails.getId(),
            word = request.word,
            meaning = request.meaning,
            sourceSceneId = request.sourceSceneId,
        )
        val result: SavedWordResult = saveWordUseCase.execute(command)
        val response: WordbookResponse = wordbookResponse(result = result)
        val responseBody: SuccessResponse<WordbookResponse> = successResponse(responseCode = CREATED, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }

    @GetMapping
    override fun getWordbook(
        @PathVariable childId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<List<WordbookResponse>>> {
        val command = GetWordbookCommand(childId = childId, guardianId = userDetails.getId())
        val results: List<SavedWordResult> = getWordbookUseCase.execute(command)
        val response: List<WordbookResponse> = results.map { wordbookResponse(result = it) }
        val responseBody: SuccessResponse<List<WordbookResponse>> = successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }
}
