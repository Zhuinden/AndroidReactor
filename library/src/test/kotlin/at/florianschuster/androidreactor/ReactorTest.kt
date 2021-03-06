package at.florianschuster.androidreactor

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.amshove.kluent.shouldEqual
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit


class ReactorTest {
    @get:Rule
    val rxSchedulersOverrideRule = RxSchedulersOverrideRule()

    @Test
    fun testEachMethodIsInvoked() {
        val reactor = TestReactor()
        val testObserver = reactor.state.test()

        reactor.action.accept(arrayListOf("action"))

        testObserver.let {
            it.assertNoErrors()
            it.values().let {
                it.count() shouldEqual 2
                it[0] shouldEqual listOf("transformedState")
                it[1] shouldEqual listOf(
                    "action",
                    "transformedAction",
                    "mutation",
                    "transformedMutation",
                    "transformedState"
                )
            }
        }
    }

    @Test
    fun testStateReplayCurrentState() {
        val reactor = CounterReactor()
        val testObserver = reactor.state.test() // state: 0

        reactor.action.accept(Unit) // state: 1
        reactor.action.accept(Unit) // state: 2

        testObserver.values().let {
            it.count() shouldEqual 3
            it shouldEqual listOf(0, 1, 2)
        }
    }

    @Test
    fun testCurrentState() {
        val reactor = TestReactor()
        reactor.state //create state stream

        reactor.action.accept(listOf("action"))

        reactor.currentState shouldEqual listOf(
            "action",
            "transformedAction",
            "mutation",
            "transformedMutation",
            "transformedState"
        )
    }

    @Test
    fun testCurrentStateNotCreated() {
        val reactor = TestReactor()
        reactor.action.accept(listOf("action"))
        reactor.currentState shouldEqual emptyList()
    }

    @Test
    fun testStreamIgnoresErrorFromMutate() {
        val reactor = CounterReactor()
        val testObserver = reactor.state.test()

        reactor.stateForTriggerError = 2
        reactor.action.accept(Unit)
        reactor.action.accept(Unit)
        reactor.action.accept(Unit)
        reactor.action.accept(Unit)
        reactor.action.accept(Unit)

        testObserver.values().let {
            it.size shouldEqual 6
            it shouldEqual listOf(0, 1, 2, 3, 4, 5)
        }
    }

    @Test
    fun testStreamIgnoresCompletedFromMutate() {
        val reactor = CounterReactor()
        val testObserver = reactor.state.test()

        reactor.stateForTriggerCompleted = 2
        reactor.action.accept(Unit)
        reactor.action.accept(Unit)
        reactor.action.accept(Unit)
        reactor.action.accept(Unit)
        reactor.action.accept(Unit)

        testObserver.values() shouldEqual listOf(0, 1, 2, 3, 4, 5)
    }

    @Test
    fun testCancel() {
        val reactor = StopwatchReactor()
        val testObserver = reactor.state.test()
        //todo https://github.com/ReactorKit/ReactorKit/blob/master/Tests/ReactorKitTests/ReactorTests.swift#L134
    }
}


abstract class BaseTestReactor<A : Any, M : Any, S : Any>(
    final override val initialState: S
) : Reactor<A, M, S> {
    override var disposables = CompositeDisposable()
    override val action: PublishRelay<A> by lazy { PublishRelay.create<A>() }
    override val state: Observable<out S> by lazy { createStateStream() }
    override var currentState: S = initialState
}


private class TestReactor : BaseTestReactor<List<String>, List<String>, List<String>>(ArrayList()) {
    // 1. ["action"] + ["transformedAction"]
    override fun transformAction(action: Observable<List<String>>): Observable<List<String>> {
        return action.map { it + "transformedAction" }
    }

    // 2. ["action", "transformedAction"] + ["mutation"]
    override fun mutate(action: List<String>): Observable<List<String>> {
        return Observable.just(action + "mutation")
    }

    // 3. ["action", "transformedAction", "mutation"] + ["transformedMutation"]
    override fun transformMutation(mutation: Observable<List<String>>): Observable<List<String>> {
        return mutation.map { it + "transformedMutation" }
    }

    // 4. [] + ["action", "transformedAction", "mutation", "transformedMutation"]
    override fun reduce(state: List<String>, mutation: List<String>): List<String> {
        return state + mutation
    }

    // 5. ["action", "transformedAction", "mutation", "transformedMutation"] + ["transformedState"]
    override fun transformState(state: Observable<List<String>>): Observable<List<String>> {
        return state.map { it + "transformedState" }
    }
}


private class CounterReactor : BaseTestReactor<Unit, Unit, Int>(0) {
    var stateForTriggerError: Int? = null
    var stateForTriggerCompleted: Int? = null

    override fun mutate(action: Unit): Observable<Unit> = when (currentState) {
        stateForTriggerError -> {
            val results = arrayOf(Observable.just(action), Observable.error(Error()))
            Observable.concat(results.asIterable())
        }
        stateForTriggerCompleted -> {
            val results = arrayOf(Observable.just(action), Observable.empty())
            Observable.concat(results.asIterable())
        }
        else -> Observable.just(action)
    }

    override fun reduce(state: Int, mutation: Unit): Int = state + 1
}


private class StopwatchReactor : BaseTestReactor<StopwatchReactor.Action, Int, Int>(0) {
    sealed class Action {
        object Start : Action()
        object Stop : Action()
    }

    override fun mutate(action: Action): Observable<out Int> = when (action) {
        is Action.Start -> {
            Observable.interval(1, TimeUnit.SECONDS)
                .takeUntil(this.action.filter {
                    val lel = it is Action.Stop
                    println("lel: $lel")
                    lel
                })
                .map { 1 }
        }
        is Action.Stop -> Observable.empty()
    }

    override fun reduce(state: Int, mutation: Int): Int = state + mutation
}
