# AndroidReactor

![AndroidReactor Version](https://img.shields.io/badge/AndroidReactor-0.0.1-red.svg)

AndroidReactor is a framework for a reactive and unidirectional Android application architecture.  
It is an Android port of the [ReaktorKit](https://github.com/ReactorKit/ReactorKit/) iOS concept.


## Installation

Add the following Gradle dependency to your project `build.gradle` file:

```groovy
dependencies {
    implementation 'TODO'
}
```


## What should I know before I try this?

* Android (duh)
* Kotlin
* RxJava2
* nice to have: Lifecycle Architecture Components
* nice to have: MVI Architecture Pattern


## Tell me more

### General Concept and Unidirectional Data Flow

For this you should hit up the [ReactorKit Repo Readme](https://github.com/ReactorKit/ReactorKit/blob/master/README.md).  
It is very extensive and since Swift 4 and Kotlin are much alike you will feel right at home! They also have nice graphics.

### Android Specific

When binding the Reactor to an Activity or a Fragment, their life cycles have to be taken into account.  
All views have to be laid out before the bind happens, so you should not call `fun bind(Reactor)` before:

* Activity's `fun onCreate(Bundle)`
* Fragment's `fun onActivityCreated(Bundle)`


## Examples

* [Counter](https://github.com/floschu/AndroidReactor/tree/master/countersample): Most Basic Counter Example.
* [Github Search](https://github.com/floschu/AndroidReactor/tree/master/githubsample): Github Repository Search.
* [Watchables](https://github.com/floschu/Watchables): A Movie and TV Show Watchlist Application.


## License

```
Copyright 2019 Florian Schuster.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```