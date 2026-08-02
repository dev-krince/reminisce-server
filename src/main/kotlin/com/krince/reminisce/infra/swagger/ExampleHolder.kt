package com.krince.reminisce.infra.swagger

import io.swagger.v3.oas.models.examples.Example

class ExampleHolder(
    val holder: Example,
    val code: Int,
    val name: String,
    val description: String
)