package se.gustavkarlsson.krate.samples.lanterna.ui

import com.googlecode.lanterna.gui2.Interactable
import com.googlecode.lanterna.gui2.table.Table
import com.googlecode.lanterna.input.KeyStroke

class SelectionListeningTable<V>(vararg columnLabels: String) : Table<V>(*columnLabels) {

    var selectionListener: (() -> Unit)? = null

    override fun handleKeyStroke(keyStroke: KeyStroke): Interactable.Result {
        return super.handleKeyStroke(keyStroke).also {
            if (it == Interactable.Result.HANDLED) {
                selectionListener?.invoke()
            }
        }
    }
}
