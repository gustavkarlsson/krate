package se.gustavkarlsson.krate.samples.android.gui.fragments.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_notes.*
import org.koin.android.ext.android.inject
import se.gustavkarlsson.krate.samples.android.R

class NotesFragment : Fragment() {

    private val disposables = CompositeDisposable()

    private val viewModel: NotesViewModel by inject()

    private val noteListAdapter = NoteListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_notes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notesRecyclerView.run {
            adapter = noteListAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, OrientationHelper.VERTICAL))
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.bind()
        bind()
    }

    private fun NotesViewModel.bind() {
        disposables.add(notes.subscribe {
            noteListAdapter.notes = it
        })
    }

    private fun bind() {
        disposables.add(addFab.clicks()
            .subscribe { viewModel.onAddNoteClicked() })

        noteListAdapter.onClick = viewModel::onNoteClicked
        noteListAdapter.onSwipe = viewModel::onNoteSwiped
    }

    override fun onStop() {
        disposables.clear()
        super.onStop()
    }
}
