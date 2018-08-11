package se.gustavkarlsson.krate.samples.javafx.store

import io.reactivex.Single
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import se.gustavkarlsson.krate.core.dsl.buildStore
import se.gustavkarlsson.krate.samples.javafx.repo.TodoRepository

val store = buildStore<State, Command, Result> {
    val todoRepository = TodoRepository()

    states {
        initial = State()
        observeScheduler = JavaFxScheduler.platform()
    }

    commands {
        transform<AddTodo> { commands ->
            commands
                .map(AddTodo::text)
                .flatMap { text ->
                    todoRepository.add(text, false)
                        .toFlowable()
                        .map<Result>(::CreatedNewTodo)
                        .startWith(CreatingNewTodo)
                }
        }

        transform<UpdateTodo> { commands ->
            commands
                .map(UpdateTodo::todo)
                .flatMap { todo ->
                    todoRepository.update(todo)
                        .toFlowable()
                        .map<Result>(::ChangedTodo)
                        .startWith(UpdatingTodo(todo.id))
                }
        }

        transform<DeleteTodo> { commands ->
            commands
                .map(DeleteTodo::id)
                .flatMap { id ->
                    todoRepository.delete(id)
                        .toFlowable<Result>()
                        .startWith(UpdatingTodo(id))
                        .concatWith(Single.just(DeletedTodo(id)))
                }
        }
    }

    results {
        reduce<CreatingNewTodo> { state, _ ->
            state.copy(creatingTodo = true)
        }

        reduce<CreatedNewTodo> { state, (createdTodo) ->
            state.copy(todos = state.todos + createdTodo, creatingTodo = false)
        }

        reduce<UpdatingTodo> { state, (updatingId) ->
            state.copy(updatingTodoIds = state.updatingTodoIds + updatingId)
        }

        reduce<ChangedTodo> { state, (changedTodo) ->
            val newTodos = state.todos.map {
                if (it.id == changedTodo.id) {
                    changedTodo
                } else {
                    it
                }
            }
            state.copy(todos = newTodos, updatingTodoIds = state.updatingTodoIds - changedTodo.id)
        }

        reduce<DeletedTodo> { state, (deletedId) ->
            val newTodos = state.todos.filterNot { it.id == deletedId }
            state.copy(todos = newTodos, updatingTodoIds = state.updatingTodoIds - deletedId)
        }
    }
}
