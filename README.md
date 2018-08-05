[![Build Status](https://travis-ci.com/gustavkarlsson/krate.svg?branch=master)](https://travis-ci.com/gustavkarlsson/krate)
[![codecov](https://codecov.io/gh/gustavkarlsson/krate/branch/master/graph/badge.svg)](https://codecov.io/gh/gustavkarlsson/krate)
[![codebeat badge](https://codebeat.co/badges/ee4f1e26-fca2-420a-ac9a-e0af088242be)](https://codebeat.co/projects/github-com-gustavkarlsson-krate-master)
[![Version](https://jitpack.io/v/gustavkarlsson/krate.svg)](https://jitpack.io/#gustavkarlsson/krate)
[![MIT licensed](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/gustavkarlsson/krate/blob/master/LICENSE.md)

# Krate

Krate is a reactive state management library for Kotlin and RxJava.
It brings order to the chaos by taking care of the data flow in your app.
Krate is heavily inspired by
[This talk by Jake Wharton](https://jakewharton.com/the-state-of-managing-state-with-rxjava/)
but also [Flux](https://facebook.github.io/flux/) and [Redux](https://redux.js.org).


## Why Krate?

- **Predictable:** Full control of application state changes, even during async operations
- **Asynchronous:** Built with RxJava to simplify asynchronous programming
- **Unidirectional:** Data flows in one direction, making it easier to reason about
- **Separation of concerns:** Clear distinction between *what* and *how*
- **DSL:** The initial setup is done using a fully documented developer-friendly DSL
- **Kotlin powered:** Utilizes advanced Kotlin features to provide a beautiful API
- **Great with Android:** Alleviates many annoying aspects of Android development


## Learn more
Check out the [wiki](https://github.com/gustavkarlsson/krate/wiki) for documentation and simple examples.

You can also check out one of the provided sample projects.


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
