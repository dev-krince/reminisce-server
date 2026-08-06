package com.krince.reminisce.application.port.out.postactivityresult

import com.krince.reminisce.domain.model.postactivityresult.PostActivityResult

interface CommandPostActivityResultPort {
    fun save(result: PostActivityResult): PostActivityResult

    fun deleteAllBySessionIds(sessionIds: List<String>)
}
