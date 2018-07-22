package se.gustavkarlsson.krate.samples.lanterna.ui

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Direction
import com.googlecode.lanterna.gui2.EmptySpace
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.LinearLayout
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.gui2.WindowListener
import com.googlecode.lanterna.input.KeyStroke
import se.gustavkarlsson.krate.samples.lanterna.api.models.Repo
import java.util.concurrent.atomic.AtomicBoolean

class ReposWindow : BasicWindow(TITLE) {
    private val table = SelectionListeningTable<String>("Name", "Owner", "Description").apply {
        selectionListener = {
            if (selectedRow >= tableModel.rowCount - 1) {
                loadMoreListener?.invoke()
            }
        }
        setSelectAction {
            repoClickedListener?.invoke(selectedRow)
        }
    }

    init {
        setHints(listOf(Window.Hint.EXPANDED))
        component = buildContent()
        addWindowListener(ResizeListener { updateTableSize(it.rows) })
    }

    private fun buildContent(): Panel =
        Panel(LinearLayout(Direction.VERTICAL))
            .addComponent(
                EmptySpace()
            )
            .addComponent(
                Label("Repositories")
            )
            .addComponent(
                table,
                LinearLayout.createLayoutData(LinearLayout.Alignment.Fill)
            )

    private fun updateTableSize(windowHeight: Int) {
        table.visibleRows = windowHeight - 3
    }

    var loadMoreListener: (() -> Unit)? = null

    var repoClickedListener: ((Int) -> Unit)? = null

    var isLoading: Boolean
        get() = title != TITLE
        set(value) {
            title = if (value) "$TITLE (loading)" else TITLE
        }

    val repoCount: Int
        get() = table.tableModel.rowCount

    fun addRepos(repos: List<Repo>) {
        repos.forEach {
            table.tableModel.addRow(it.name, it.owner.login, it.description ?: "-")
        }
    }

    companion object {
        private const val TITLE = "Github Browser"
    }

    private class ResizeListener(private val onResize: (TerminalSize) -> Unit) : WindowListener {
        override fun onInput(basePane: Window, keyStroke: KeyStroke, deliverEvent: AtomicBoolean) = Unit

        override fun onMoved(window: Window, oldPosition: TerminalPosition?, newPosition: TerminalPosition) = Unit

        override fun onResized(window: Window, oldSize: TerminalSize?, newSize: TerminalSize) {
            onResize(newSize)
        }

        override fun onUnhandledInput(basePane: Window, keyStroke: KeyStroke, hasBeenHandled: AtomicBoolean) = Unit
    }
}
