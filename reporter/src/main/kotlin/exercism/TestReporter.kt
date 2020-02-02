package exercism

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.Types
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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

    private val capture = MultiOutputCapture(
        OutputCapture(System.out) { out -> System.setOut(out) },
        OutputCapture(System.err) { out -> System.setErr(out) }
    )

    private val items = mutableListOf<TestItem>()

    private var testPlans = 0

    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        testPlans++
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        testPlans--

        if (testPlans == 0) {
            capture.resetCaptures()
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
            .add(PolymorphicJsonAdapterFactory.of(ExecutionResult::class.java, "__type")
                .withSubtype(ExecutionResult.Successful::class.java, ExecutionResult.Successful::class.simpleName)
                .withSubtype(ExecutionResult.Skipped::class.java, ExecutionResult.Skipped::class.simpleName)
                .withSubtype(ExecutionResult.Aborted::class.java, ExecutionResult.Aborted::class.simpleName)
                .withSubtype(ExecutionResult.Failed::class.java, ExecutionResult.Failed::class.simpleName)
            )
            .add(KotlinJsonAdapterFactory())
            .add(ThrowableAdapter())
            .build()

        val json = moshi
            .adapter<List<TestItem>>(Types.newParameterizedType(List::class.java, TestItem::class.java))
            .indent("  ")
            .toJson(items)

        println("JSON:")
        println(json)

        File("build/out.json").writeText(json)
    }
}

data class TestItem(val name: String, val result: ExecutionResult, val stdOut: String, val stdErr: String) {

    sealed class ExecutionResult {
        class Successful : ExecutionResult()
        data class Skipped(val reason: String) : ExecutionResult()
        data class Aborted(val throwable: Throwable?) : ExecutionResult()
        data class Failed(val throwable: Throwable?) : ExecutionResult()
    }
}

class ThrowableAdapter : JsonAdapter<Throwable>() {

    @FromJson
    override fun fromJson(reader: JsonReader): Throwable? =
        reader.nextString()?.let { Throwable(it) }

    @ToJson
    override fun toJson(writer: JsonWriter, value: Throwable?) {
        writer.value(value?.message)
    }
}

fun TestExecutionResult.parseExecutionResult(): ExecutionResult = when (status!!) {
    SUCCESSFUL -> ExecutionResult.Successful()
    FAILED -> ExecutionResult.Failed(throwable.orElse(null))
    ABORTED -> ExecutionResult.Aborted(throwable.orElse(null))
}

// FIXME capture for each thread
class MultiOutputCapture(
    private val stdOut: OutputCapture,
    private val stdErr: OutputCapture
) {

    fun startCapturing() {
        stdOut.startCapturing()
        stdErr.startCapturing()
    }

    fun stopCapturing(): CaptureResult = CaptureResult(
        stdOut = stdOut.stopCapturing(),
        stdErr = stdErr.stopCapturing()
    )

    fun resetCaptures() {
        stdOut.resetCaptures()
        stdErr.resetCaptures()
    }

    data class CaptureResult(val stdOut: String, val stdErr: String)
}

class OutputCapture(
    private val previousValue: PrintStream,
    private val setter: (PrintStream) -> Unit
) {
    private val buffer = ByteArrayOutputStream()
    private val newStream = PrintStream(buffer)

    fun startCapturing() {
        buffer.reset()
        setter(newStream)
    }

    fun stopCapturing(): String =
        buffer.toString()

    fun resetCaptures() {
        setter(previousValue)
    }
}
