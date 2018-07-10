package se.gustavkarlsson.krate.sample

fun main(args: Array<String>) {
    println("Starting sample app")

    store.start()

    store.states.subscribe {
        println(it)
        it.errors.firstOrNull()?.let {
            println("Acknowledging error: '$it'")
            store.issue(Command.AcknowledgeError(it))
        }
    }

    Thread.sleep(1000)

    println("Getting notes")
    store.issue(Command.GetNotes)
    Thread.sleep(1000)

    println("Creating note")
    store.issue(Command.AddNote("Walk the dog"))
    Thread.sleep(1000)

    println("Creating note")
    store.issue(Command.AddNote("Walk the raccoon"))
    Thread.sleep(1000)

    println("Deleting note")
    store.issue(Command.DeleteNote(store.currentState.notes.first()))
    Thread.sleep(1000)
}
