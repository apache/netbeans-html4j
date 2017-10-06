# HTML/Java API

In need of cross platform, client side interaction between Java and JavaScript?

The HTML/Java library provides [basic building blocks](https://builds.apache.org/job/incubator-netbeans-html4j-linux/javadoc/net/java/html/js/package-summary.html)
as well as advanced [high level concepts](https://builds.apache.org/job/incubator-netbeans-html4j-linux/javadoc/net/java/html/json/Model.html)
to make communication between JavaScript and Java as smooth as possible.

Read more in the [latest javadoc](https://builds.apache.org/job/incubator-netbeans-html4j-linux/javadoc/) documentation.

## Portable Applications

Every browser widget Java API offers ways for communication between Java and
JavaScript running in such widget. However, each of them is unique - e.g. one
writes different code when communicating with [JavaFX WebView](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/web/WebView.html),
different one when communicating with [Android WebView](https://developer.android.com/reference/android/webkit/WebView.html),
yet another one when talking to [iOS WebView](https://developer.apple.com/documentation/uikit/uiwebview).

The goal of HTML/Java API is to **unify this communication**. By providing simple
and highly portable [building blocks](https://builds.apache.org/job/incubator-netbeans-html4j-linux/javadoc/net/java/html/js/package-summary.html)
one can create sophisticated APIs (like
[UI bindings](https://builds.apache.org/job/incubator-netbeans-html4j-linux/javadoc/net/java/html/json/package-summary.html),
[charts](https://dukescript.com/javadoc/charts/),
[maps](https://dukescript.com/javadoc/leaflet4j/),
[canvas](https://dukescript.com/javadoc/canvas/), or
[SnapSVG](https://dukescript.com/javadoc/libs/net/java/html/lib/snapsvg/Snap/package-summary.html))
that can be embedded into
[Swing or JavaFX applications](https://builds.apache.org/job/incubator-netbeans-html4j-linux/javadoc/net/java/html/boot/fx/FXBrowsers.html),
executed [headlessly on a server](https://builds.apache.org/job/incubator-netbeans-html4j-linux/javadoc/net/java/html/boot/script/Scripts.html)
or executed anywhere HTML/Java API was ported.

Various ports of this rendering pipeline were built including support for
pure [webkit desktop rendring](https://github.com/dukescript/dukescript-presenters/),
[Android WebView](https://dukescript.com/javadoc/presenters/com/dukescript/presenters/Android.html)
and [iOS WebView](https://dukescript.com/javadoc/presenters/com/dukescript/presenters/iOS.html)
developed by [DukeScript project](https://dukescript.com/).

This technology has also been adopted by some Java bytecode to JavaScript
transpilers - for example [Bck2Brwsr VM](https://github.com/jtulach/bck2brwsr/) -
as such you can also run the same Java application in a pluginless browser.

Porting of HTML/Java rendering pipeline is as easy as implementing
[Fn.Presenter](https://builds.apache.org/job/incubator-netbeans-html4j-linux/javadoc/org/netbeans/html/boot/spi/Fn.Presenter.html)
interface and successfully passing the
[test compatibility kit](https://builds.apache.org/job/incubator-netbeans-html4j-linux/javadoc/org/netbeans/html/json/tck/package-summary.html).

## Getting Started

The HTML/Java API is IDE and build tool neutral. It can be used with
[Ant](http://ant.apache.org), [Maven](http://maven.apache.org) or **Gradle**.
It is easy to edit it with [Eclipse](https://dukescript.com/best/practices/2015/07/01/DukeScript-with-Eclipse.html),
[IntelliJ](https://dukescript.com/best/practices/2016/04/19/IDEA.html) or
[NetBeans](https://dukescript.com/getting_started.html).

The most comprehensive getting started guide is available from
[the DukeScript project](https://dukescript.com/getting_started.html) website.

## Contributing

You can contribute to development of this library by forking
its [GitHub repository](https://github.com/apache/incubator-netbeans-html4j).
Change, modify, test:

```bash
$ mvn clean install
```

and create a pull request, which may then be merged into the
official [Apache repository](https://git-wip-us.apache.org/repos/asf?p=incubator-netbeans-html4j.git).

### Full History

The origins of the code in this repository are older than
its Apache existence. As such significant part of the history
(before the code was donated to Apache) is kept in an
independent repository. To fully understand the code you may
want to merge the modern and ancient versions together:

```bash
$ git clone https://github.com/apache/incubator-netbeans-html4j.git html+java+both
$ cd html+java+both
$ git log boot/src/main/java/net/java/html/boot/BrowserBuilder.java
```

This gives you just few log entries including the initial checkin and change of the
file headers to Apache. But then the magic comes:

```bash
$ git remote add emilian https://github.com/emilianbold/netbeans-html4j.git
$ git fetch emilian
$ git replace 408363d d029b8e
```

When you search the log, or use the blame tool, the full history is
available:

```bash
$ git log boot/src/main/java/net/java/html/boot/BrowserBuilder.java
$ git blame boot/src/main/java/net/java/html/boot/BrowserBuilder.java
```

Many thanks to Emilian Bold who converted the ancient history to
[his Git repository](https://github.com/emilianbold/netbeans-html4j)
and made the magic possible!
