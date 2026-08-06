package com.krince.reminisce.application.port.out.stt

interface SttPort {
    fun transcribe(audio: String): String?
}
