(ns cljapi.core
  (:require [cljapi.wsapi :as wsapi]))

(def api-key "_0c6D9bHSRZO2nmJ6NnsUyOURPe5PEqZraZeyrsFC8")
(def scope (memoize (fn [] (-> (wsapi/workspaces) first wsapi/scope))))

(defn- types-to-export []
  [:UserStory :Defect :Task :DefectSuite])

(defn- export [type]
  )

(defn -main [& args])
