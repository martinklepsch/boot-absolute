(ns boot-absolute.core
  {:boot/export-tasks true}
  (:require [net.cgrand.enlive-html :as html]
            [clojure.java.io      :as io]
            [boot.core         :as core :refer [deftask]]
            [boot.file         :as file]
            [boot.util         :as util]))


(defn f [] (first (core/by-ext [".html"] (core/tgt-files))))

(def attributes
  "List of attributes to check for references to files in fileset"
  [:src :href])

(defn files-relative-to
  "Given `f` and `files` determines relative paths from `f` to `files`"
  [f files]
  (map
   #(.getPath (file/relative-to (io/file (core/relative-path (.getParentFile f)))
                                (io/file (core/relative-path %))))
   files))

(defn build-selectors [f attr]
  (let [all (core/tgt-files)
        rel (files-relative-to f all)]
    (map (fn [f] [(html/attr-ends attr f)]) rel)))

(defn extract-attr [nodes attr]
  (map #(attr (:attrs %)) nodes))

(defn by-path-re
  "This function takes two arguments: `res` and `files`, where `res` is a seq
  of regex patterns like `[#\"clj$\" #\"cljs$\"]` and `files` is a seq of
  file objects. Returns a seq of the files in `files` whose path match one of
  the regex patterns in `res`."
  [res files & [negate?]]
  ((core/file-filter #(fn [f] (re-find % (.getPath f)))) res files negate?))

(defn relative-to-exists?
  "Takes a `file` and a `path` and tests if a file at `path`
   relative to `file` exists. If it exists returns full path
   relative to target-dir"
  [file path]
  (let [base     (.getParentFile file)
        tgt-file (core/relative-path (io/file base (.getPath (file/relative-to base (io/file base path)))))
        regex    (fn [path] (re-pattern (clojure.string/replace path "." "\\.")))]
    ;(println tgt-file)
    (if (seq (by-path-re [(regex tgt-file)] (core/tgt-files)))
      tgt-file
      false)))

(defn normalize-path [f path]
  (relative-to-exists? f path))

(defn absolutize-node [node f mount dest]
  (let [attrs (:attrs node)
        abs #(clojure.string/replace-first % (re-pattern (str "^" dest)) mount)]
    (assoc-in node [:attrs]
        (into {}
            (for [attr attrs]
              (let [normalized (normalize-path f (val attr))]
                (if normalized
                  {(key attr) (abs normalized)}
                  attr)))))))

(defn transform [f [mount dest]]
  (let [sel        (apply concat (map #(build-selectors f %) attributes))
        res        (html/html-resource f)
        absolutize #(absolutize-node % f mount dest)]
    (html/at* res
              (partition 2 (interleave sel (repeat absolutize))))))

(deftask absolute
  "Change relative paths in html files linking to
   files in :target-dir to absolute paths"
  [m mapping MAPPING [str] "[\"/assets\" \"public\" will change links to files in public to /assets"]
  (core/with-pre-wrap
    (let [mapping (or ["/assets" "assets"] mapping)
          tgt-dir     (core/mktgtdir!)]
      (doseq [f (core/by-ext [".html"] (core/tgt-files))]
        (let [out (io/file tgt-dir (core/relative-path f))
              transformed (html/emit* (transform f mapping))]
          (util/info "Making references to files from fileset absolute in %s ...\n" (core/relative-path f))
          (io/make-parents out)
          ;(println (apply str transformed))
          (spit out (apply str transformed)))))))
