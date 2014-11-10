(set-env!
  :dependencies '[[org.clojure/clojure       "1.6.0"       :scope "provided"]
                  [boot/core                 "2.0.0-pre22" :scope "provided"]
                  [enlive                    "1.1.5"]
                  [tailrecursion/boot-useful "0.1.3"       :scope "test"]])

(require '[tailrecursion.boot-useful :refer :all])

(def +version+ "0.0.1")

(useful! +version+)

(task-options!
  pom  [:project     'boot-absolute
        :version     +version+
        :description "Boot task to make file references in html files absolute"
        :url         "https://github.com/martinklepsch/boot-absolute"
        :scm         {:url "https://github.com/martinklepsch/boot-absolute"}
        :license     {:name "Eclipse Public License"
                      :url  "http://www.eclipse.org/legal/epl-v10.html"}])