(ns hydrox.core
  (:require [hydrox.meta :as meta]
            [hydrox.doc :as doc]
            [hydrox.core.regulator :as regulator]
            [hara.component :as component]
            [clojure.java.io :as io]))

(defn read-project
  "like `leiningen.core.project/read` but with less features'
 
   (keys (read-project (io/file \"example/project.clj\")))
   => (just [:description :license :name :source-paths :test-paths
             :documentation :root :url :version :dependencies] :in-any-order)"
  {:added "0.1"}
  ([] (read-project (io/file "project.clj")))
  ([file]
   (let [path  (.getCanonicalPath file)
         root  (subs path 0 (- (count path) 12))
         pform (read-string (slurp file))
         [_ name version] (take 3 pform)
         proj  (->> (drop 3 pform)
                    (concat [:name name
                             :version version
                             :root root])
                    (apply hash-map))]
     (-> proj
         (update-in [:source-paths] (fnil identity ["src"]))
         (update-in [:test-paths] (fnil identity ["test"]))))))

(defn submerged?
  "checks if dive has started"
  {:added "0.1"}
  ([] (submerged? regulator/*running*))
  ([regs]
   (if (empty? regs) false true)))

(defn single-use
  "returns a working regulator for a given project file"
  {:added "0.1"}
  ([] (single-use "project.clj"))
  ([path]
   (let [proj  (read-project (io/file path))
         folio (-> proj
                   (regulator/create-folio)
                   (regulator/init-folio))
         state (atom folio)]
     (regulator/regulator state proj))))

(defn import-docstring
  "imports docstrings given a regulator"
  {:added "0.1"}
  ([]
   (if (submerged?)
     (mapv #(import-docstring % :all) regulator/*running*)
     (println "call `dive` first before running this function")))
  ([reg] (import-docstring reg :all))
  ([reg ns] (import-docstring reg ns nil))
  ([{:keys [state project] :as reg} ns var]
   (let [{:keys [references]
          lu :namespace-lu} @state]
     (cond (= ns :all)
           (doall (meta/import-project project references))

           :else
           (if-let [file (get lu ns)]
             (if var
               (meta/import-var file var references)
               (meta/import-file file references)))))))

(defn purge-docstring
  "purges docstrings given a regulator"
  {:added "0.1"}
  ([]
   (if (submerged?)
     (mapv #(purge-docstring % :all) regulator/*running*)
     (println "call `dive` first before running this function")))
  ([reg] (purge-docstring reg :all))
  ([reg ns] (purge-docstring reg ns nil))
  ([{:keys [state project] :as reg} ns var]
   (let [{lu :namespace-lu} @state]
     (cond (= ns :all)
           (doall (meta/purge-project project))

           :else
           (if-let [file (get lu ns)]
             (if var
               (meta/purge-var file var)
               (meta/purge-file file)))))))

(defn generate-docs
  "generates html docs for :documentation entries in project.clj"
  {:added "0.1"}
  ([]
   (if (submerged?)
     (mapv generate-docs regulator/*running*)
     (println "call `dive` first before running this function")))
  ([{:keys [state] :as reg}]
   (doc/render-all @state))
  ([{:keys [state] :as reg} name]
   (doc/render-single @state name)))

(defn dive
  "starts a dive"
  {:added "0.1"}
  ([] (dive "project.clj"))
  ([path]
   (component/start (regulator/regulator (read-project (io/file path))))))

(defn surface
  "finishes a dive"
  {:added "0.1"}
  ([] (doseq [reg regulator/*running*]
        (surface reg)))
  ([regulator]
   (component/stop regulator)))