(ns cljapi.core
  (:require [cljapi.wsapi :as wsapi]))

(def api-key "_0c6D9bHSRZO2nmJ6NnsUyOURPe5PEqZraZeyrsFC8")

(defn- types-to-export []
  [:TestCaseStep
   :TestCaseResult
   :TestCase
   :ConversationPost
   :DefectSuite
   :Task
   :Defect
   :UserStory
   :PortfolioItem
   :Iteration
   :Release
   :Project
   :WorkspaceConfiguration])

(defn- export-type [env wsapi-type]
  (println "Beginning" wsapi-type)
  (doseq [wsapi-object (take 10 (wsapi/read-all env wsapi-type))]
    (println "  " (wsapi/->ref env wsapi-object))))

(defn- export-workspace-configuration [env]
  )

(defn- export-workspace [env]
  (println "WS:" (wsapi/->ref env (:workspace env)))
  (doseq [wsapi-type (types-to-export)]
    (if (= wsapi-type :WorkspaceConfiguration)
      (export-workspace-configuration env)
      (export-type env wsapi-type))))

(defn env []
  {:hostname  "https://test4cluster.rallydev.com"
   :auth-key  api-key
   :page-size 5})

(defn- run []
  (let [env (env)]
    (doseq [workspace (wsapi/workspaces env)]
      (>pprint workspace)
      (export-workspace (assoc env :workspace workspace)))))

(defn -main [& args])
