package at.florianschuster.countersample

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders

import at.florianschuster.androidreactor.ReactorView
import at.florianschuster.androidreactor.ViewModelReactor
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.visibility
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_counter.*
import java.util.concurrent.TimeUnit


private const val layout = R.layout.activity_counter

class CounterActivity : AppCompatActivity(), ReactorView<CounterReactor> {
    private var stateSnapshot: CounterReactor.State = CounterReactor.State()

    override val disposables = CompositeDisposable()

    @Suppress("UNCHECKED_CAST")
    override val reactor: CounterReactor by lazy {
        ViewModelProviders.of(this@CounterActivity, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass == CounterReactor::class.java) {
                    return CounterReactor(stateSnapshot) as T
                }
                throw IllegalArgumentException("Cannot create ${modelClass.name}")
            }
        }).get(CounterReactor::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            stateSnapshot = savedInstanceState.getParcelable("stateSnapshot")!!
        }

        setContentView(layout)
        bind(reactor)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("stateSnapshot", stateSnapshot)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    override fun bind(reactor: CounterReactor) {
        btnIncrease.clicks()
            .map { CounterReactor.Action.Increase }
            .subscribe(reactor.action)
            .let(disposables::add)

        btnDecrease.clicks()
            .map { CounterReactor.Action.Decrease }
            .subscribe(reactor.action)
            .let(disposables::add)

        reactor.state.map { it.value }
            .distinctUntilChanged()
            .map { "$it" }
            .subscribe(tvValue::setText)
            .let(disposables::add)

        reactor.state.map { it.loading }
            .distinctUntilChanged()
            .subscribe(progressLoading.visibility())
            .let(disposables::add)

        reactor.state
            .subscribe { state -> stateSnapshot = state }
            .let(disposables::add)
    }
}


class CounterReactor(initialState: State) : ViewModelReactor<CounterReactor.Action, CounterReactor.Mutation, CounterReactor.State>(initialState) {
    sealed class Action {
        object Increase : Action()
        object Decrease : Action()
    }

    sealed class Mutation {
        object IncreaseValue : Mutation()
        object DecreaseValue : Mutation()
        data class SetLoading(val loading: Boolean) : Mutation()
    }

    @Parcelize
    data class State(
        val value: Int = 0,
        @Transient val loading: Boolean = false
    ) : Parcelable {
        companion object: Parceler<State> { // the only way to ignore a property with @Parcelize is to manually not save it.
            override fun create(parcel: Parcel): State = State(parcel.readInt())

            override fun State.write(parcel: Parcel, flags: Int) {
                parcel.writeInt(value)
            }
        }
    }

    override fun mutate(action: Action): Observable<out Mutation> = when (action) {
        is Action.Increase -> Observable.concat(
            Observable.just(Mutation.SetLoading(true)),
            Observable.just(Mutation.IncreaseValue).delay(500, TimeUnit.MILLISECONDS),
            Observable.just(Mutation.SetLoading(false))
        )
        is Action.Decrease -> Observable.concat(
            Observable.just(Mutation.SetLoading(true)),
            Observable.just(Mutation.DecreaseValue).delay(500, TimeUnit.MILLISECONDS),
            Observable.just(Mutation.SetLoading(false))
        )
    }

    override fun reduce(state: State, mutation: Mutation): State = when (mutation) {
        is Mutation.IncreaseValue -> state.copy(value = state.value + 1)
        is Mutation.DecreaseValue -> state.copy(value = state.value - 1)
        is Mutation.SetLoading -> state.copy(loading = mutation.loading)
    }
}