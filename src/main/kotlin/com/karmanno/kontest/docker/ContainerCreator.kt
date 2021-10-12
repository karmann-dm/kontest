package com.karmanno.kontest.docker

import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.core.command.LogContainerResultCallback
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ContainerCreator(
    private val dockerContainer: DockerContainer
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(ContainerCreator::class.java)
    }

    fun create(): String {
        val createContainerCommand = DockerClientConfiguration.dockerClient
            .createContainerCmd(dockerContainer.imageName)

        dockerContainer.ports.forEach {
            logger.info(it)
            createContainerCommand.withPortBindings(PortBinding.parse(it))
        }

        dockerContainer.envs.forEach {
            logger.info("${it.key}=${it.value}")
            createContainerCommand.withEnv("${it.key}=${it.value}")
        }

        val containerInstance = createContainerCommand.exec()

        DockerClientConfiguration.dockerClient.startContainerCmd(containerInstance.id).exec()
        DockerClientConfiguration.dockerClient.logContainerCmd(containerInstance.id)
            .withFollowStream(true)
            .withSince(0)
            .withStdOut(true)
            .withStdErr(true)
            .exec(object : LogContainerResultCallback() {
                override fun onNext(item: Frame?) {
                    logger.info(item.toString())
                }
            })

        var healthy = false

        runBlocking {
            launch(Dispatchers.Default) {
                var countdown = 120

                while (true) {
                    val inspect = DockerClientConfiguration.dockerClient
                        .inspectContainerCmd(containerInstance.id)
                        .exec()

                    if (inspect.state.status == "exited" || countdown == 0) {
                        this.cancel()
                    }

                    if (inspect.state.status == "running") {
                        healthy = true
                        this.cancel()
                    }

                    logger.info(inspect.state.status)
                    delay(1000)
                    countdown--
                }
            }
        }

        if (!healthy) {
            throw RuntimeException("Container for image ${dockerContainer.imageName} is unhealthy")
        }

        return containerInstance.id
    }
}