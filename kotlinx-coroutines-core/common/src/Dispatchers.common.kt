/*
 * Copyright 2016-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.coroutines

import kotlin.coroutines.*

/**
 * Groups various implementations of [CoroutineDispatcher].
 */
public expect object Dispatchers {
    /**
     * The default [CoroutineDispatcher] that is used by all standard builders like
     * [launch][CoroutineScope.launch], [async][CoroutineScope.async], etc.
     * if neither a dispatcher nor any other [ContinuationInterceptor] is specified in their context.
     *
     * It is backed by a shared pool of threads on JVM and Native. By default, the maximum number of threads used
     * by this dispatcher is equal to the number of CPU cores, but is at least two.
     */
    public val Default: CoroutineDispatcher

    /**
     * A coroutine dispatcher that is confined to the Main thread operating with UI objects.
     * Usually such dispatchers are single-threaded.
     *
     * Access to this property may throw an [IllegalStateException] if no main dispatchers are present in the classpath.
     *
     * Depending on platform and classpath, it can be mapped to different dispatchers:
     * - On JVM it is either the Android main thread dispatcher, JavaFx, or Swing EDT dispatcher. It is chosen by the
     *   [`ServiceLoader`](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html).
     * - On JS it is equivalent to the [Default] dispatcher with [immediate][MainCoroutineDispatcher.immediate] support.
     * - On Native Darwin-based targets, it is a dispatcher backed by Darwin's main queue.
     * - On other Native targets, it is not available.
     * - `Dispatchers.setMain` from the `kotlinx-coroutines-test` artifact can replace the main dispatcher with a mock one for testing.
     *
     * In order to work with the `Main` dispatcher on the JVM, the following artifact should be added to the project runtime dependencies:
     *  - `kotlinx-coroutines-android` &mdash; for Android Main thread dispatcher
     *  - `kotlinx-coroutines-javafx` &mdash; for JavaFx Application thread dispatcher
     *  - `kotlinx-coroutines-swing` &mdash; for Swing EDT dispatcher
     */
    public val Main: MainCoroutineDispatcher

    /**
     * A coroutine dispatcher that is not confined to any specific thread.
     * It executes the initial continuation of a coroutine in the current call-frame
     * and lets the coroutine resume in whatever thread that is used by the corresponding suspending function, without
     * mandating any specific threading policy. Nested coroutines launched in this dispatcher form an event-loop to avoid
     * stack overflows.
     *
     * ### Event loop
     * Event loop semantics is a purely internal concept and has no guarantees on the order of execution
     * except that all queued coroutines will be executed on the current thread in the lexical scope of the outermost
     * unconfined coroutine.
     *
     * For example, the following code:
     * ```
     * withContext(Dispatchers.Unconfined) {
     *    println(1)
     *    launch(Dispatchers.Unconfined) { // Nested unconfined
     *        println(2)
     *    }
     *    println(3)
     * }
     * println("Done")
     * ```
     * Can print both "1 2 3" and "1 3 2". This is an implementation detail that can be changed.
     * However, it is guaranteed that "Done" will only be printed once the code in both `withContext` and `launch` completes.
     *
     * If you need your coroutine to be confined to a particular thread or a thread-pool after resumption,
     * but still want to execute it in the current call-frame until its first suspension, you can use
     * an optional [CoroutineStart] parameter in coroutine builders like
     * [launch][CoroutineScope.launch] and [async][CoroutineScope.async] setting it to
     * the value of [CoroutineStart.UNDISPATCHED].
     */
    public val Unconfined: CoroutineDispatcher
}
