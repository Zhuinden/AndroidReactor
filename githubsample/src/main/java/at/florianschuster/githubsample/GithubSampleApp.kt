package at.florianschuster.githubsample

import android.app.Application
import at.florianschuster.androidreactor.AndroidReactor
import timber.log.Timber


class GithubSampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
        AndroidReactor.handleErrorsWith(Timber::e)
    }
}