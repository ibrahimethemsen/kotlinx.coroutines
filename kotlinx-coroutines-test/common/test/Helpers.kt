/*
 * Copyright 2016-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.coroutines.test

import kotlinx.atomicfu.*
import kotlin.test.*
import kotlin.time.*
import kotlin.time.Duration.Companion.seconds

/**
 * The number of milliseconds that is sure not to pass [assertRunsFast].
 */
const val SLOW = 100_000L

/**
 * Asserts that a block completed within [timeout].
 */
@OptIn(ExperimentalTime::class)
inline fun <T> assertRunsFast(timeout: Duration, block: () -> T): T {
    val result: T
    val elapsed = TimeSource.Monotonic.measureTime { result = block() }
    assertTrue("Should complete in $timeout, but took $elapsed") { elapsed < timeout }
    return result
}

/**
 * Asserts that a block completed within two seconds.
 */
inline fun <T> assertRunsFast(block: () -> T): T = assertRunsFast(2.seconds, block)

/**
 * Runs [test], and then invokes [block], passing to it the lambda that functionally behaves
 * the same way [test] does.
*/
fun testResultMap(block: (() -> Unit) -> Unit, test: () -> TestResult): TestResult = testResultChain(
    block = test,
    after = {
        block { it.getOrThrow() }
        createTestResult { }
    }
)

/**
 * Chains together [block] and [after], passing the result of [block] to [after].
 */
expect fun testResultChain(block: () -> TestResult, after: (Result<Unit>) -> TestResult): TestResult

fun testResultChain(vararg chained: (Result<Unit>) -> TestResult, initialResult: Result<Unit> = Result.success(Unit)): TestResult =
    if (chained.isEmpty()) {
        createTestResult {
            initialResult.getOrThrow()
        }
    } else {
        testResultChain(block = {
            chained[0](initialResult)
        }) {
            testResultChain(*chained.drop(1).toTypedArray(), initialResult = it)
        }
    }

class TestException(message: String? = null): Exception(message)

/**
 * A class inheriting from which allows to check the execution order inside tests.
 *
 * @see TestBase
 */
open class OrderedExecutionTestBase {
    private val actionIndex = atomic(0)
    private val finished = atomic(false)

    /** Expect the next action to be [index] in order. */
    protected fun expect(index: Int) {
        val wasIndex = actionIndex.incrementAndGet()
        check(index == wasIndex) { "Expecting action index $index but it is actually $wasIndex" }
    }

    /** Expect this action to be final, with the given [index]. */
    protected fun finish(index: Int) {
        expect(index)
        check(!finished.getAndSet(true)) { "Should call 'finish(...)' at most once" }
    }

    @AfterTest
    fun ensureFinishCalls() {
        assertTrue(finished.value || actionIndex.value == 0, "Expected `finish` to be called")
    }
}

internal fun <T> T.void() { }

@OptionalExpectation
expect annotation class NoJs()

@OptionalExpectation
expect annotation class NoNative()
