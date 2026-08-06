package com.krince.reminisce.application.port.out.message

import com.krince.reminisce.domain.model.message.Message

interface CommandMessagePort {
    fun save(message: Message): Message
}
