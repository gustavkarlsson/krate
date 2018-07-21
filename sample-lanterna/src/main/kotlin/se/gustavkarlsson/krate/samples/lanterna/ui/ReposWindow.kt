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
    private val repositoriesTable = SelectionListeningTable<String>("Name", "Owner", "Description").apply {
        selectionListener = {
            if (selectedRow >= tableModel.rowCount - 5) {
                requestMoreListener?.invoke()
            }
        }
        setSelectAction {
            repoClickedListener?.invoke(selectedRow)
        }
    }

    init {
        setHints(listOf(Window.Hint.EXPANDED))
        component = Panel(LinearLayout(Direction.VERTICAL))
            .addComponent(
                EmptySpace()
            )
            .addComponent(
                Label("Repositories")
            )
            .addComponent(
                repositoriesTable,
                LinearLayout.createLayoutData(LinearLayout.Alignment.Fill)
            )
        addWindowListener(object : WindowListener {
            override fun onInput(basePane: Window, keyStroke: KeyStroke, deliverEvent: AtomicBoolean) = Unit

            override fun onMoved(window: Window, oldPosition: TerminalPosition?, newPosition: TerminalPosition) = Unit

            override fun onResized(window: Window, oldSize: TerminalSize?, newSize: TerminalSize) {
                updateTableSize(newSize.rows)
            }

            override fun onUnhandledInput(basePane: Window, keyStroke: KeyStroke, hasBeenHandled: AtomicBoolean) = Unit
        })
    }

    private fun updateTableSize(windowHeight: Int) {
        repositoriesTable.visibleRows = windowHeight - 3
    }

    var requestMoreListener: (() -> Unit)? = null

    var repoClickedListener: ((Int) -> Unit)? = null

    var isLoading: Boolean
        get() = title != TITLE
        set(value) {
            title = if (value) "$TITLE (loading)" else TITLE
        }

    fun setRepos(repos: List<Repo>) {
        repos.drop(repositoriesTable.tableModel.rowCount).forEach {
            repositoriesTable.tableModel.addRow(it.name, it.owner.login, it.description ?: "")
        }
    }

    companion object {
        private const val TITLE = "Github Browser"
    }
}
