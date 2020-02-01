package exercism

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import exercism.TestItem.ExecutionResult
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.TestExecutionResult.Status.ABORTED
import org.junit.platform.engine.TestExecutionResult.Status.FAILED
import org.junit.platform.engine.TestExecutionResult.Status.SUCCESSFUL
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

private const val DEBUG = true

class TestReporter : TestExecutionListener {

    init {
        if (DEBUG) println("Initializing ${TestReporter::class.java}")
    }

    private val capture = OutputCapture()

    private val items = mutableListOf<TestItem>()

    private var testPlans = 0

    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        testPlans++
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        testPlans--

        if (testPlans == 0) {
            exportTestResults()
        }
    }

    override fun executionStarted(identifier: TestIdentifier) {
        if (!identifier.isTest) return

        capture.startCapturing()
    }

    override fun executionFinished(identifier: TestIdentifier, result: TestExecutionResult) {
        if (!identifier.isTest) return

        recordTestItem(identifier, result.parseExecutionResult())
    }

    override fun executionSkipped(identifier: TestIdentifier, reason: String) {
        if (!identifier.isTest) return

        recordTestItem(identifier, ExecutionResult.Skipped(reason))
    }

    private fun recordTestItem(identifier: TestIdentifier, result: ExecutionResult) {
        val captureResult = capture.stopCapturing()

        items += TestItem(
            name = identifier.displayName.removeSuffix("()"),
            result = result,
            stdOut = captureResult.stdOut.trim(),
            stdErr = captureResult.stdErr.trim()
        )
    }

    private fun exportTestResults() {
        val moshi = Moshi.Builder()
            .build()

        val json = moshi
            .adapter<List<TestItem>>(Types.newParameterizedType(List::class.java, TestItem::class.java))
            .toJson(items)

        File("build/out.json").writeText(json)
    }
}

data class TestItem(val name: String, val result: ExecutionResult, val stdOut: String, val stdErr: String) {

    sealed class ExecutionResult {
        object Successful : ExecutionResult()
        data class Skipped(val reason: String) : ExecutionResult()
        data class Aborted(val throwable: Throwable?) : ExecutionResult()
        data class Failed(val throwable: Throwable?) : ExecutionResult()
    }
}

fun TestExecutionResult.parseExecutionResult(): ExecutionResult = when (status!!) {
    SUCCESSFUL -> ExecutionResult.Successful
    FAILED -> ExecutionResult.Failed(throwable.orElse(null))
    ABORTED -> ExecutionResult.Aborted(throwable.orElse(null))
}

class OutputCapture {

    private val stdOutBuffer = ByteArrayOutputStream()
    private val stdErrBuffer = ByteArrayOutputStream()

    private val stdOutStream = PrintStream(stdOutBuffer)
    private val stdErrStream = PrintStream(stdErrBuffer)

    fun startCapturing() {
        stdOutBuffer.reset()
        stdErrBuffer.reset()

        System.setOut(stdOutStream)
        System.setErr(stdErrStream)
    }

    fun stopCapturing(): CaptureResult = CaptureResult(
        stdOut = stdOutBuffer.toString(),
        stdErr = stdErrBuffer.toString()
    )

    data class CaptureResult(val stdOut: String, val stdErr: String)
}
