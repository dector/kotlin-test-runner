package exercism.kotlin.autotests

import org.junit.jupiter.engine.config.JupiterConfiguration.DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME
import org.junit.platform.commons.util.ClassFilter
import org.junit.platform.commons.util.ReflectionUtils
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.engine.reporting.ReportEntry
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import utils.resolveAsDir
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.net.URLClassLoader

fun main() {
    val launcher = LauncherFactory.create()

//    val dir = "examples/full/build/classes/".resolveAsDir()
//    println(dir.absolutePath + " ${dir.exists()}")
//
//    val cl = run {
//        URLClassLoader(arrayOf(dir.toURL()),
//            launcher.javaClass.classLoader)
//    }
//    ReflectionUtils.findAllClassesInClasspathRoot(dir.toURI(), ClassFilter.of { true})
//        .let { println("Found: ${it.size}") }

    val testPlan = launcher.discover(LauncherDiscoveryRequestBuilder.request()
        .selectors(DiscoverySelectors.selectClass(MyTest::class.java))
        .configurationParameter(DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME, "*")
        .build())

    val listener = MyTestListener()
    launcher.registerTestExecutionListeners(listener)

    launcher.execute(testPlan)

//    listener.summary.printTo(System.out.writer().let { PrintWriter(it) })
}

class MyTestListener : TestExecutionListener {

    val captures = mutableMapOf<TestIdentifier, String>()

    var lastOut = System.out
    var currentCapture: ByteArrayOutputStream? = null

    override fun executionFinished(testIdentifier: TestIdentifier?, testExecutionResult: TestExecutionResult?) {
        System.setOut(lastOut)

        println("finished: $testIdentifier")
        println("with: $testExecutionResult")

        println("=== LOG ===")
        currentCapture?.toString(Charsets.UTF_8).let(::println)
        currentCapture = null
        println("=== LOG END ===")
    }

    override fun reportingEntryPublished(testIdentifier: TestIdentifier?, entry: ReportEntry?) {
        println("published $testIdentifier as $entry")
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan?) {
    }

    override fun executionSkipped(testIdentifier: TestIdentifier?, reason: String?) {
        println("skipped, $testIdentifier skipped because of $reason")
    }

    override fun executionStarted(testIdentifier: TestIdentifier) {
        println("started $testIdentifier")

        currentCapture = ByteArrayOutputStream()
        System.setOut(PrintStream(currentCapture))
    }

    override fun testPlanExecutionStarted(testPlan: TestPlan?) {
        println("plan started: ${testPlan!!.countTestIdentifiers { it.type == TestDescriptor.Type.TEST }}")
    }

    override fun dynamicTestRegistered(testIdentifier: TestIdentifier?) {
        println("registered $testIdentifier")
    }
}
