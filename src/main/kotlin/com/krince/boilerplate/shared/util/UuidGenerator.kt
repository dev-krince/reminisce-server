package com.krince.boilerplate.shared.util

import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object UuidGenerator {
    @OptIn(ExperimentalUuidApi::class)
    fun generate(): String = Uuid.generateV7().toString()

    fun generateFileNameFormat(): String = buildString {
        repeat(2) {
            append(UUID.randomUUID().toString().replace("-", ""))
        }
    }
}