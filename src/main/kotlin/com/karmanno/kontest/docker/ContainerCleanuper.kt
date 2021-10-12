package com.karmanno.kontest.docker

import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ContainerCleanuper(
    val dockerContainerId: String
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(ContainerCreator::class.java)
    }

    fun cleanup() {
        DockerClientConfiguration.dockerClient.stopContainerCmd(dockerContainerId).exec()

        var stopped = false
        runBlocking {
            launch(Dispatchers.Default) {
                var countdown = 120
                while (true) {
                    val inspect = DockerClientConfiguration.dockerClient
                        .inspectContainerCmd(dockerContainerId)
                        .exec()

                    if (countdown == 0) {
                        this.cancel()
                    }

                    if (inspect.state.status == "exited") {
                        stopped = true
                        this.cancel()
                    }

                    ContainerCreator.logger.info(inspect.state.status)
                    delay(1000)
                    countdown--
                }
            }
        }

        if (!stopped) {
            throw RuntimeException("Container with id $dockerContainerId was not stopped")
        }

    }
}