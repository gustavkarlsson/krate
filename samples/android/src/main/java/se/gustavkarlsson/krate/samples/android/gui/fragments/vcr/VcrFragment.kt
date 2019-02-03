package se.gustavkarlsson.krate.samples.android.gui.fragments.vcr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_vcr.*
import org.koin.android.ext.android.inject
import se.gustavkarlsson.krate.samples.android.R

class VcrFragment : Fragment() {

    private val disposables = CompositeDisposable()

    private val viewModel: VcrViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_vcr, container, false)

    override fun onStart() {
        super.onStart()
        bind()
    }

    private fun bind() {
        disposables.add(recordButton.clicks()
            .subscribe { viewModel.onRecordClicked() })

        disposables.add(stopButton.clicks()
            .subscribe { viewModel.onStopClicked() })

        disposables.add(playButton.clicks()
            .subscribe { viewModel.onPlayClicked() })
    }

    override fun onStop() {
        disposables.clear()
        super.onStop()
    }
}
