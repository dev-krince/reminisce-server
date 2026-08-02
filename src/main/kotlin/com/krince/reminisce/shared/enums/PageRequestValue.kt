package com.krince.reminisce.shared.enums

@JvmInline
value class PageRequestValue(val value: Long) {
    companion object {
        private const val DEFAULT_VALUE = 1L

        val DEFAULT = PageRequestValue(DEFAULT_VALUE).value
    }
}