package com.krince.boilerplate.infra.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "file.storage")
data class FileStorageProperties(
    val path: String,
    val cachePeriod: Int,
)