package com.karmanno.kontest.extension

import com.karmanno.kontest.docker.ContainerCleanuper
import com.karmanno.kontest.docker.ContainerCreator
import com.karmanno.kontest.docker.DockerContainer
import com.karmanno.kontest.docker.DockerContainerInstance
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class KontestExtension: BeforeAllCallback, BeforeEachCallback, ExtensionContext.Store.CloseableResource {
    companion object {
        var started = false
        var containerInstances = ConcurrentHashMap<String, DockerContainerInstance>()
    }

    private val firsRunStaticLock: Lock = ReentrantLock()
    private val firstClassRunStaticLock: Lock = ReentrantLock()

    override fun beforeAll(context: ExtensionContext?) {
        firsRunStaticLock.lock()
        if (!started) {
            started = true
            context?.root?.getStore(ExtensionContext.Namespace.GLOBAL)
                ?.put(UUID.randomUUID().toString(), this)
        }
        firsRunStaticLock.unlock()
    }

    override fun beforeEach(context: ExtensionContext?) {
        val testClassOptional = context?.testClass
        if (testClassOptional?.isPresent!!) {
            val testClass = testClassOptional.get()
            val instance = context.testInstance.get()
            testClass.declaredFields
                .filter { it.type == DockerContainer::class.java }
                .map { field -> field.isAccessible = true; field }
                .map { it.get(instance) as DockerContainer }
                .map {
                    val containerId = ContainerCreator(it).create()
                    DockerContainerInstance(containerId, it)
                }
                .forEach { containerInstances[it.id] = it }
        }
    }

    override fun close() {
        containerInstances.forEach {
            ContainerCleanuper(it.key).cleanup()
        }
    }
}