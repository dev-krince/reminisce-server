package com.krince.reminisce.infra.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "child.policy")
data class ChildPolicyProperties(
    val maxPerGuardian: Int,
)
