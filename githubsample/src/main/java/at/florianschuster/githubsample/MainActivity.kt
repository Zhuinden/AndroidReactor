package at.florianschuster.githubsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


private const val layout = R.layout.activity_main

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)
        supportFragmentManager.beginTransaction().add(R.id.container, GithubFragment()).commit()
    }
}
