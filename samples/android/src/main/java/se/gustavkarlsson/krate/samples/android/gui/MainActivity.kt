package se.gustavkarlsson.krate.samples.android.gui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import io.reactivex.disposables.CompositeDisposable
import org.koin.android.ext.android.inject
import se.gustavkarlsson.krate.samples.android.R

class MainActivity : AppCompatActivity() {

    private val disposables = CompositeDisposable()

    private val viewModel: MainViewModel by inject()

    private val navController by lazy {
        findNavController(R.id.mainNavHost)
    }

    override fun onSupportNavigateUp(): Boolean = navController.navigateUp()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupActionBarWithNavController(navController)
        viewModel.bind()
        bind()
    }

    private fun MainViewModel.bind() {
        disposables.add(navigateToEditNote.subscribe {
            navController.navigate(R.id.action_notesFragment_to_editNoteFragment)
        })
    }

    private fun bind() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.editNoteFragment) {
                viewModel.onNotNavigatingToEditNote()
            }
        }
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
