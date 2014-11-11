(ns boot-absolute.core
  {:boot/export-tasks true}
  (:require [clojure.java.io      :as io]
            [boot.core         :as core :refer [deftask]]
            [boot.pod          :as pod]
            [boot.file         :as file]
            [boot.util         :as util]))


(defn f [] (first (core/by-ext [".html"] (core/tgt-files))))

(def deps
  '[#_[org.clojure/clojure "1.6.0"]
    #_[boot/core "2.0.0-pre22"]
    [enlive "1.1.5"]
    [boot-absolute "0.0.2"]])

(defn enlive-pod []
  (pod/make-pod (-> (core/get-env)
                    (assoc-in [:dependencies] deps)
                    (update-in [:src-paths] #(conj % "src")))))

(def attributes
  "List of attributes to check for references to files in fileset"
  [:src :href])

(deftask absolute
  "Change relative paths in html files linking to
   files in :target-dir to absolute paths"
  [m mapping MAPPING [str] "[\"/assets\" \"public\" will change links to files in public to /assets"]
  (core/with-pre-wrap
    (let [mapping     (or ["/assets" "assets"] mapping)
          tgt-dir     (core/mktgtdir!)
          enlive-pod  (enlive-pod)
          _           (pod/eval-in enlive-pod (require 'boot-absolute.impl))]
      (doseq [f (core/by-ext [".html"] (core/tgt-files))]
        (let [out (io/file tgt-dir (core/relative-path f))
              transformed (pod/eval-in enlive-pod (boot-absolute.impl/transform-file ~f ~mapping ~attributes))]
          (util/info "Making references to files from fileset absolute in %s ...\n" (core/relative-path f))
          (io/make-parents out)
          ;(println (apply str transformed))
          (spit out (apply str transformed)))))))
