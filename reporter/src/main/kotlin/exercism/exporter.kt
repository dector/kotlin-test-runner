package exercism

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

fun exportTestPlanResults(name: String, cases: List<TestItem>, dir: File) {
    val moshi = Moshi.Builder()
        .add(PolymorphicJsonAdapterFactory.of(TestItem.ExecutionResult::class.java, "__type")
            .withSubtype(TestItem.ExecutionResult.Successful::class.java, TestItem.ExecutionResult.Successful::class.simpleName)
            .withSubtype(TestItem.ExecutionResult.Skipped::class.java, TestItem.ExecutionResult.Skipped::class.simpleName)
            .withSubtype(TestItem.ExecutionResult.Aborted::class.java, TestItem.ExecutionResult.Aborted::class.simpleName)
            .withSubtype(TestItem.ExecutionResult.Failed::class.java, TestItem.ExecutionResult.Failed::class.simpleName)
        )
        .add(KotlinJsonAdapterFactory())
        .add(ThrowableAdapter())
        .build()

    val json = moshi
        .adapter<List<TestItem>>(Types.newParameterizedType(List::class.java, TestItem::class.java))
        .indent("  ")
        .toJson(cases)

    println("JSON:")
    println(json)

    dir.resolve("TESTCASE-$name.json").writeText(json)
}
