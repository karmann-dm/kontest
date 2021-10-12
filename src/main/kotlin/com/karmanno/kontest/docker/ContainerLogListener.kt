package com.karmanno.kontest.docker

import com.github.dockerjava.api.command.LogContainerCmd
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.core.command.LogContainerResultCallback
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ContainerLogListener(
    containerId: String
) {
    val log: Logger = LoggerFactory.getLogger("Container $containerId")

    private val logContainerCmd: LogContainerCmd = DockerClientConfiguration.dockerClient
        .logContainerCmd(containerId)
        .withStdOut(true)
        .withStdErr(true)
        .withTimestamps(true)
        .withFollowStream(true)

    fun log() {
        logContainerCmd.exec(object: LogContainerResultCallback() {
            override fun onNext(item: Frame?) {
                println(item.toString())
                log.info(item.toString())
            }
        })
    }
}