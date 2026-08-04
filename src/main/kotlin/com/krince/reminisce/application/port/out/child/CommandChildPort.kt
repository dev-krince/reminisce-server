package com.krince.reminisce.application.port.out.child

import com.krince.reminisce.domain.model.child.Child

interface CommandChildPort {
    fun save(child: Child): Child
}
