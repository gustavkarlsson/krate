# Krate

Krate is a reactive state management library for JVM applications written for Kotlin and RxJava.
It's heavily inspired by
[This talk by Jake Wharton](https://jakewharton.com/the-state-of-managing-state-with-rxjava/)
but also [Flux](https://facebook.github.io/flux/) and [Redux](https://redux.js.org).


## Why Krate?

- **Predictable:** Full control of application state changes, even during async operations
- **Asynchronous:** Heavily utilizes RxJava to simplify asynchronous programming
- **Small API:** The API contains only a single public class and a few functions
- **For Kotlin:** Utilizes advanced Kotlin features to provide a beautiful API
- **For Android:** Alleviates many of the annoying aspects of Android development


## Including

Krate is hosted on Jitpack. Here's how you include it in your gradle project:

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
    implementation "com.github.gustavkarlsson:krate:<latest_version>"
}
```

For maven, sbt and leiningen, go to [JitPack](https://jitpack.io/#gustavkarlsson/krate)


## More coming soon...
