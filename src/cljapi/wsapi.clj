(ns cljapi.wsapi
  (:require [cletus.http-client :as http]
            [cletus.config :as config]
            [cletus.utils :as utils]
            [clojure.string :as string]))

(def api-key (memoize #(config/string "RALLY_API_KEY")))
(defn host-name [] "https://rally1.rallydev.com")
(defn page-size [] 100)

(defn body [response]
  (-> response
      :body
      utils/->map))

(defn call [options]
  (let [response (-> options
                     (assoc-in [:headers :zsessionid] (api-key))
                     (assoc :hostname "https://rally1.rallydev.com")
                     http/call
                     deref)
        body     (body response)]
    (if body
      (assoc response :body body)
      response)))

(defn read-ref
  ([uri] (read-ref uri nil))
  ([uri query-parameters]
   (let [response  (-> (call (assoc {:uri uri} :query-params query-parameters))
                       :body)
         first-key (-> response keys first)]
     (get response first-key))))

(defn ->ref
  ([value]
   (let [url (cond
               (string? value)  value
               (keyword? value) (str "/slm/webservice/v2.0/" (name value))
               :default         (:_ref value))]
     (string/replace url (host-name) "")))
  ([value oid]
   (str (->ref value) "/" oid)))

(defn ->full-ref
  ([value]
   (->> value
        ->ref
        (str (host-name))))
  ([value oid]
   (str (->full-ref value) "/" oid)))

(defn user []
  (read-ref (->ref :user)))

(defn subscription []
  (read-ref (->ref :subscription)))

(defn ->oid [value]
  (:ObjectID value))

(defn workspaces []
  (let [subscription   (-> :subscription ->ref read-ref)
        workspace-refs (->ref (:Workspaces subscription))]
    (->> (read-ref workspace-refs)
         :Results)))

(defn schema [scope]
  (let [workspace-oid (->oid (:workspace scope))]
    (-> (read-ref (str "/slm/schema/v2.0/workspace/" workspace-oid))
        :Results)))

(defn scope [workspace]
  {:workspace workspace})

(defn read-page [scope type page-number]
  (let [params {:fetch     true
                :workspace (->full-ref (:workspace scope))
                :start     (inc (* page-number (page-size)))
                :pagesize  (page-size)}]
    (-> (read-ref (->ref type) params)
        :Results)))

(defn read-all
  ([scope type] (read-all scope type 1))
  ([scope type page-number]
   (let [page (read-page scope type page-number)]
     (when (< 0 (count page))
       (read-all scope type page-number page))))
  ([scope type page-number page]
   (let [[head & rest] page]
     (if head
       (cons head (lazy-seq (read-all scope type page-number rest)))
       (read-all scope type (inc page-number))))))
