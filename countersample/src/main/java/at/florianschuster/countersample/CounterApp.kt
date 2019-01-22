package at.florianschuster.countersample

import android.app.Application
import at.florianschuster.androidreactor.AndroidReactor
import timber.log.Timber


class CounterApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
        AndroidReactor.handleErrorsWith(Timber::e)
    }
}