package se.gustavkarlsson.krate.samples.android.gui.fragments.editnote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding2.widget.textChanges
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_editnote.*
import org.koin.android.ext.android.inject
import se.gustavkarlsson.krate.samples.android.R

class EditNoteFragment : Fragment() {

    private val disposables = CompositeDisposable()

    private val viewModel: EditNoteViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_editnote, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleEditText.setText(viewModel.initialTitle)
        titleEditText.text?.length?.let(titleEditText::setSelection)
        contentEditText.setText(viewModel.initialContent)
    }

    override fun onStart() {
        super.onStart()
        bind()
    }

    private fun bind() {
        disposables.add(titleEditText.textChanges()
            .subscribe(viewModel::onTitleChanged))

        disposables.add(contentEditText.textChanges()
            .subscribe(viewModel::onContentChanged))
    }

    override fun onStop() {
        disposables.clear()
        super.onStop()
    }
}
