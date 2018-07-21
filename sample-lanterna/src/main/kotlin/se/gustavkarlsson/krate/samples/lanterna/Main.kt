package se.gustavkarlsson.krate.samples.lanterna

import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.gui2.dialogs.MessageDialog
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import io.reactivex.Maybe
import se.gustavkarlsson.krate.samples.lanterna.store.Command
import se.gustavkarlsson.krate.samples.lanterna.store.store
import se.gustavkarlsson.krate.samples.lanterna.ui.RepoDetailsWindow
import se.gustavkarlsson.krate.samples.lanterna.ui.ReposWindow

fun main(args: Array<String>) {
    val screen = DefaultTerminalFactory().createScreen()
    val gui = MultiWindowTextGUI(screen)
    screen.startScreen()
    val mainWindow = ReposWindow()

    store.states
        .map { it.isLoadingNewRepos || it.isLoadingRepoDetails }
        .subscribe { mainWindow.isLoading = it }

    store.states
        .map { it.repos }
        .subscribe { mainWindow.setRepos(it) }

    store.states
        .map { it.errors }
        .flatMapMaybe {
            if (it.isEmpty()) {
                Maybe.empty()
            } else {
                Maybe.just(it.first())
            }
        }
        .subscribe {
            store.issue(Command.AcknowledgeError(it))
            MessageDialog.showMessageDialog(gui, "Error", it)
        }

    store.states
        .subscribe {
            val repo = it.shownRepo
            if (repo != null) {
                if (gui.activeWindow !is RepoDetailsWindow) {
                    val window = RepoDetailsWindow(repo)
                    gui.addWindowAndWait(window)
                    store.issue(Command.CloseRepoDetails)
                }
            }
        }

    mainWindow.requestMoreListener = {
        store.issue(Command.LoadMoreRepos)
    }

    mainWindow.repoClickedListener = {
        store.issue(Command.LoadRepoDetails(it))
    }

    store.issue(Command.LoadMoreRepos)

    gui.addWindowAndWait(mainWindow)
}
