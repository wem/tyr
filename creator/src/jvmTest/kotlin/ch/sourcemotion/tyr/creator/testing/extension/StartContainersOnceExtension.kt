package ch.sourcemotion.tyr.creator.testing.extension

import mu.KLogging
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.util.AnnotationUtils
import org.junit.platform.commons.util.ReflectionUtils
import org.testcontainers.containers.GenericContainer

class StartContainersOnceExtension : BeforeAllCallback {
    private companion object : KLogging()

    override fun beforeAll(context: ExtensionContext?) {
        val containers = findContainers(context)
        containers.forEach { container ->
            if (container.isRunning.not()) {
                logger.info { "Start ${container.dockerImageName} container..." }
                container.start()
                logger.info { "${container.dockerImageName} container started" }
            }
        }
    }

    private fun findContainers(context: ExtensionContext?): List<GenericContainer<*>> {
        val testClass = context?.testClass
        return if (testClass?.isPresent == true) {
            val staticContainerGetters = ReflectionUtils.findMethods(
                testClass.get(),
                { func -> AnnotationUtils.isAnnotated(func, OnceStartedContainer::class.java) && ReflectionUtils.isStatic(func) },
                ReflectionUtils.HierarchyTraversalMode.TOP_DOWN
            )
            val containerInstances = staticContainerGetters.map { it.invoke(null) }
            containerInstances.filterIsInstance<GenericContainer<*>>()
        } else {
            emptyList()
        }
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY_GETTER)
annotation class OnceStartedContainer
