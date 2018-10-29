package se.gustavkarlsson.krate.samples.tornadofx.store

import io.reactivex.Single
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import se.gustavkarlsson.krate.core.dsl.buildStore
import se.gustavkarlsson.krate.samples.tornadofx.repo.TodoRepository

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

        transform<ToggleTodoCompleted> { commands ->
            commands
                .map(ToggleTodoCompleted::todo)
                .map { todo ->
                    todo.copy(isCompleted = !todo.isCompleted)
                }
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
        reduce { state, result ->
            when (result) {
                is CreatingNewTodo -> {
                    state.copy(creatingTodo = true)
                }
                is CreatedNewTodo -> {
                    state.copy(todos = state.todos + result.todo, creatingTodo = false)
                }
                is UpdatingTodo -> {
                    state.copy(updatingTodoIds = state.updatingTodoIds + result.id)
                }
                is ChangedTodo -> {
                    val changedTodo = result.todo
                    val newTodos = state.todos.map {
                        if (it.id == changedTodo.id) {
                            changedTodo
                        } else {
                            it
                        }
                    }
                    state.copy(todos = newTodos, updatingTodoIds = state.updatingTodoIds - changedTodo.id)
                }
                is DeletedTodo -> {
                    val deletedId = result.id
                    val newTodos = state.todos.filterNot { it.id == deletedId }
                    state.copy(todos = newTodos, updatingTodoIds = state.updatingTodoIds - deletedId)
                }
            }
        }
    }
}
