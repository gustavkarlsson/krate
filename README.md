[![Build Status](https://travis-ci.com/gustavkarlsson/krate.svg?branch=master)](https://travis-ci.com/gustavkarlsson/krate)
[![codecov](https://codecov.io/gh/gustavkarlsson/krate/branch/master/graph/badge.svg)](https://codecov.io/gh/gustavkarlsson/krate)
[![codebeat badge](https://codebeat.co/badges/ee4f1e26-fca2-420a-ac9a-e0af088242be)](https://codebeat.co/projects/github-com-gustavkarlsson-krate-master)
[![Version](https://jitpack.io/v/gustavkarlsson/krate.svg)](https://jitpack.io/#gustavkarlsson/krate)
[![MIT licensed](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/gustavkarlsson/krate/blob/master/LICENSE.md)

# Krate

Krate is a reactive state management library for Kotlin and RxJava.
It's heavily inspired by
[This talk by Jake Wharton](https://jakewharton.com/the-state-of-managing-state-with-rxjava/)
but also [Flux](https://facebook.github.io/flux/) and [Redux](https://redux.js.org).


## Why Krate?

- **Predictable:** Full control of application state changes, even during async operations
- **Asynchronous:** Heavily utilizes RxJava to simplify asynchronous programming
- **Small API:** The API contains only a single public class and a few functions
- **For Kotlin:** Utilizes advanced Kotlin features to provide a beautiful API
- **For Android:** Alleviates many of the annoying aspects of Android development


## How does it work?

Krate consists of a few components:

### State

The state is an immutable class that represents your application state at any given time. The contents are not limited
to application data, but can also include things such as ongoing network requests or unprocessed error messages.  
A good idea is to use a kotlin data class to model the state.

### Commands

A command is a class that represents the intention to do something in your app, such as posting a tweet, refreshing
a view, or toggling a setting. It typically originates from some user action such as typed text or a button press.
All commands must inherit from a common superclass and should contain the data necessary to execute it.

### Results

A result is a class that represents the result of a command. When a command is executed, it can generate any number of
results. A typical result would be the success or failure of some command, but it could also indicate progress of a
long-running command. For example, a network request command could first generate an `InFlight` 
later followed by a `Success` or `Error` result. All results must inherit from a common superclass and should contain
the data necessary to update the state.

### Transformers

A transformer is a function that takes a stream of commands (of a given type) and returns a stream of results.
Transformers handle the business logic of the application, making sure that each command is processed and generates
the appropriate results.

### Reducers

A reducer is a function that takes 2 arguments: The current state and a result of some given type, and generates a new
state from that. All reducers run sequentially to guarantee that the state is updated in a predictable manner.

### Store

This is where everything comes together. The store takes commands as input and outputs a stream of state updates.
It's created with an initial state and a set of transformers and reducers. It then takes care of routing incoming
commands to transformers, results to reducers, and state updates to its subscribers.


## A simple example

Let's create a very simple application with a counter.
The counter starts at 100 and using buttons we can count it down towards 0.
At any time it can also be reset back to 100.

**State**

The state is a simple data class with a count property. Note that it is immutable.
```kotlin
data class State(val count: Int)
```

**Commands**

For the commands, we define a single parent class and two subclasses,
one for resetting the counter and one for counting down.
```kotlin
sealed class Command {
    object Reset : Command()
    data class CountDown(val amount: Int) : Command()
}
```

**Results**

The results are created similarly to the commands and contain all the information necessary to create a new state state.
One sets the count to a specific value, and the other adjusts it by some amount.
```kotlin
sealed class Result {
    data class NewCount(val newCount: Int) : Result()
    data class AdjustCount(val amount: Int) : Result()
}
```

**Store**

To create the store, we use a builder function and give it our state, command, and result classes as type parameters:
```kotlin
val store = buildStore<State, Command, Result> {
    
    setInitialState(State(100))
    
    // Transformers
    
    // Reducers
}
```
We also initialize the state with the counter set to 100.

**Transformers**

We will need two transformers. The first one handles resets:

```kotlin
transformByType<Command.Reset> {
    map { Result.NewCount(100) }
}
```

The transformer is a lambda with a receiver type of `Observable<Command.Reset>`.
The body transforms the commands into results using the RxJava `map` operator.
In this case, every command generates exactly one result, but using operators such as `flatMap` we could asynchronously
generate any number of results.

The second transformer is a bit more complex and uses data from each command to generate results:
```kotlin
transformByType<Command.CountDown> {
    map { it.amount }
        .filter { it > 0 }
        .map { Result.AdjustCount(-it) }
}
```

Here we also see a filter preventing the countdown from going up by ignoring non-positive commands.

**Reducers**

Our first reducer is very simple and sets the counter to whatever the result contains:

```kotlin
reduceByType<Result.NewCount> { _, result ->
    State(result.newCount)
}
```

The first parameter (state) is ignored here because we don't care about the previous state when setting a new count.

For our second reducer, we will use the existing state to generate the new state
(with a safeguard to avoid negative numbers):

```kotlin
reduceByType<Result.AdjustCount> { state, result ->
    val newCount = max(state.count + result.adjustment, 0)
    State(newCount)
}
```

**Using the store**

Now that we've built the store, we can start using it!
```kotlin
// Subscribe to state updates and render the view
store.states.subscribe { state ->
    render(state)
}

// Bind events to commands
countDownALittleButton.setOnClickListener {
    store.issue(Command.CountDown(1))
}
countDownALotButton.setOnClickListener {
    store.issue(Command.CountDown(10))
}
resetButton.setOnClickListener {
    store.issue(Command.Reset)
}
```

After starting the store, we can add as many subscribers as we wish. They will initially get the current state,
and then all state updates that follow.


## Download

Krate is hosted on JitPack. Here's how you include it in your gradle project:

**Step 1.** Add the JitPack repository to your build file:

```groovy
allprojects {
    repositories {
        // Other repositories
        maven { url 'https://jitpack.io' }
    }
}
```

**Step 2.** Add the dependency:

```groovy
dependencies {
    implementation 'com.github.gustavkarlsson:krate:<latest_version>'
}
```

For maven, sbt and leiningen, go to [JitPack](https://jitpack.io/#gustavkarlsson/krate)
