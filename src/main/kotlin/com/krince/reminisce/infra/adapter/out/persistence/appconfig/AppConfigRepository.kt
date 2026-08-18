package com.krince.reminisce.infra.adapter.out.persistence.appconfig

import com.krince.reminisce.infra.adapter.out.persistence.appconfig.entity.AppConfigOrmEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AppConfigRepository : JpaRepository<AppConfigOrmEntity, String>
