package com.karmanno.kontest

import com.karmanno.kontest.docker.DockerContainer
import com.karmanno.kontest.extension.KontestExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(KontestExtension::class)
class TestTest {
    val postgresContainer: DockerContainer = DockerContainer("postgres:13")
        .port(5410, 5432)
        .env("POSTGRES_PASSWORD", "qwe")

    @Test
    fun test() {
    }
}