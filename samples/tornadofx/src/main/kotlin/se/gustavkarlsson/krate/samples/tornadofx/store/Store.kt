package se.gustavkarlsson.krate.samples.tornadofx.store

import io.reactivex.Single
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import se.gustavkarlsson.krate.core.dsl.buildStore
import se.gustavkarlsson.krate.samples.tornadofx.repo.TodoRepository

val store = buildStore<State, Command> {
    val todoRepository = TodoRepository()

    commands {
        transform<CreateTodo> {
            map(CreateTodo::text)
                .flatMap { text ->
                    todoRepository.add(text, false)
                        .toFlowable()
                        .map<Command>(::CreatedNewTodo)
                        .startWith(CreatingNewTodo)
                }
        }

        transform<ToggleTodoCompleted> {
            map(ToggleTodoCompleted::todo)
                .map { todo ->
                    todo.copy(isCompleted = !todo.isCompleted)
                }
                .flatMap { todo ->
                    todoRepository.update(todo)
                        .toFlowable()
                        .map<Command>(::ChangedTodo)
                        .startWith(UpdatingTodo(todo.id))
                }
        }

        transform<DeleteTodo> {
            map(DeleteTodo::id)
                .flatMap { id ->
                    todoRepository.delete(id)
                        .toFlowable<Command>()
                        .startWith(UpdatingTodo(id))
                        .concatWith(Single.just(DeletedTodo(id)))
                }
        }
    }

    states {
        initial = State()
        observeScheduler = JavaFxScheduler.platform()

        reduce<CreatingNewTodo> {
            copy(creatingTodo = true)
        }

        reduce<CreatedNewTodo> { (createdTodo) ->
            copy(todos = todos + createdTodo, creatingTodo = false)
        }

        reduce<UpdatingTodo> { (updatingId) ->
            copy(updatingTodoIds = updatingTodoIds + updatingId)
        }

        reduce<ChangedTodo> { (changedTodo) ->
            val newTodos = todos.map {
                if (it.id == changedTodo.id) {
                    changedTodo
                } else {
                    it
                }
            }
            copy(todos = newTodos, updatingTodoIds = updatingTodoIds - changedTodo.id)
        }

        reduce<DeletedTodo> { (deletedId) ->
            val newTodos = todos.filterNot { it.id == deletedId }
            copy(todos = newTodos, updatingTodoIds = updatingTodoIds - deletedId)
        }
    }
}
