package at.florianschuster.androidreactor

import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable


/**
 * Abstract Reactor implementing ViewModel.
 * It handles action and state creation and clearing of the state observable.
 */
abstract class ViewModelReactor<Action : Any, Mutation : Any, State : Any>(
    final override val initialState: State
) : ViewModel(), Reactor<Action, Mutation, State> {
    override val disposables = CompositeDisposable()

    override val action: PublishRelay<Action> by lazy { PublishRelay.create<Action>() }
    override val state: Observable<out State> by lazy { createStateStream() }

    override var currentState: State = initialState

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        clear()
    }
}


/**
 * Lazily gets or creates the ViewModel Reactor for a FragmentActivity scope.
 */
inline fun <reified R> FragmentActivity.reactor(): Lazy<R> where R : Reactor<*, *, *>, R : ViewModel =
    lazy { ViewModelProviders.of(this).get(R::class.java) }


/**
 * Lazily gets or creates the ViewModel Reactor for a Fragment scope.
 */
inline fun <reified R> Fragment.reactor(): Lazy<R> where R : Reactor<*, *, *>, R : ViewModel =
    lazy { ViewModelProviders.of(this).get(R::class.java) }