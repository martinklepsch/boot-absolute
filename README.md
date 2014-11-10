# `boot-absolute`

Boot task to modify relative links to files in Boot fileset in `.html`
files so that they become absolute. Especially useful when hosting
assets through Compojure.

Provides the `absolute` task.

[![Clojars Project](http://clojars.org/boot-absolute/latest-version.svg)](http://clojars.org/boot-absolute)

## Quickstart

```clojure
(set-env! :dependencies #(conj % '[boot-absolute "0.0.1"]))
(boot (absolute ["/assets" "public"])) ; REPL
; Terminal: boot absolute -m /assets -m "public"
```

## Rationale & Usage

Let's assume you have a `assets/index.html` file that will end up in your `target-dir` as follows:

```html
<!DOCTYPE html>
<html>
  <head>
    <link href="css/screen.css" rel="stylesheet" type="text/css">
    <meta name="viewport" content="width=device-width, initial-scale=1, minimal-ui">
  </head>
  <body>
    <div id="app"></div>
    <script src="js/main.js" type="text/javascript"></script>
  </body>
</html>
```

This file references other files that reside in `target/assets`. Now
you want to render `target/assets/index.html` on `/` using Compojure:

```
(GET "/" req (res/resource-response "index.html" {:root "assets"})))
```

Now you'll run into problems as your page tries to request
`/js/main.js`. These can be fixed by adding a route like this:

```
(resources "/" {:root "assets"})
```

Now everything should work right? Unfortunately not. Having relative
links in your HTML will break your application when you're using the
Javascript `History` API.  Also you cannot serve your `index.html`
under another path than `/`, which you might want to do if you want to
prerender things on the server. **Therefore it's useful to use
absolute paths in HTML files.**

This task allows you to do this while staying compatible to
`boot-cljs` and how it adds additional script tags when ran with
`--unified`. It also allows you to consistently use relative links in
your `.html` files so they'd just work fine when not served through a
server (i.e. you just open an `.html` file from the filesystem.).

## How It Works And Why It May Not Work

`boot-absolute` inspects all `.html` files in the fileset. It does
this by creating [Enlive](https://github.com/cgrand/enlive) selectors
that match elements that have an attribute that contains a path to
another file in the fileset (relative from the `.html` file.)

**Currently only the attributes `href` and `src` are checked**, which
means it won't work with other attributes. As the need for more
attributes occurs they may get added (PRs welcome).

## Options

By default `boot-absolute` will modify relative links to files
in `target/assets` so that they're absolute under `/assets`.

**Example:** A link to a file `target/assets/css/screen.css` in
`target/assets/index.html` like this:

```
<link href="css/screen.css" rel="stylesheet" type="text/css">
```

will be modified to reference `/assets/css/screen.css` instead.

<hr>

```clojure
[m mapping        [str]  "[\"/assets\" \"public\"] will change links to files in public to /assets"]
```
See the [boot project](https://github.com/boot-clj/boot) for more information
on how to use a task's options.
