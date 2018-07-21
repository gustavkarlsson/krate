package se.gustavkarlsson.krate.samples.lanterna.ui

import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.EmptySpace
import com.googlecode.lanterna.gui2.GridLayout
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.Window
import se.gustavkarlsson.krate.samples.lanterna.api.models.RepoDetails

class RepoDetailsWindow(repo: RepoDetails) : BasicWindow(repo.name) {
    init {
        setHints(listOf(Window.Hint.CENTERED))
        component = Panel(GridLayout(2))
            .addComponent(
                Label("Owner: ")
            )
            .addComponent(
                Label(repo.owner.login)
            )
            .addComponent(
                Label("Description: ")
            )
            .addComponent(
                Label(repo.description ?: "")
            )
            .addComponent(
                Label("Stars: ")
            )
            .addComponent(
                Label(repo.stars.toString())
            )
            .addComponent(
                Label("License: ")
            )
            .addComponent(
                Label(repo.license?.name ?: "Unknown")
            )
            .addComponent(
                EmptySpace(),
                GridLayout.createHorizontallyFilledLayoutData(2)
            )
            .addComponent(
                Button("OK", ::close),
                GridLayout.createLayoutData(
                    GridLayout.Alignment.CENTER,
                    GridLayout.Alignment.BEGINNING,
                    false,
                    false,
                    2,
                    1
                )
            )
    }
}
