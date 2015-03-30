(ns cljapi.wsapi
  (:require [cletus.http-client :as http]
            [cletus.config :as config]
            [cletus.utils :as utils]
            [clojure.string :as string]))

(defn body [response]
  (-> response
      :body
      utils/->map))

(defn- apply-default-env [env]
  (merge {:hostname "https://rally1.rallydev.com"
          :pagesize 100}
         env))

(defn call [env options]
  (let [env      (apply-default-env env)
        response (-> options
                     (assoc-in [:headers :zsessionid] (:auth-key env))
                     (assoc :hostname (:hostname env))
                     http/call
                     deref)
        body     (body response)]
    (if body
      (assoc response :body body)
      response)))

(defn read-ref
  ([env uri] (read-ref env uri nil))
  ([env uri query-parameters]
   (let [response  (-> (call env (assoc {:uri uri} :query-params query-parameters))
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

(defn user [env]
  (read-ref env (->ref :user)))

(defn subscription [env]
  (read-ref env (->ref :subscription)))

(defn ->oid [value]
  (:ObjectID value))

(defn workspaces [env]
  (let [subscription   (->> :subscription ->ref (read-ref env))
        workspace-refs (->ref (:Workspaces subscription))]
    (->> (read-ref env workspace-refs)
         :Results)))

(defn schema [env]
  (let [workspace-oid (->oid (:workspace env))]
    (-> (read-ref env (str "/slm/schema/v2.0/workspace/" workspace-oid))
        :Results)))

(defn scope [workspace]
  {:workspace workspace})

(defn read-page [env type page-number]
  (let [params {:fetch     true
                :workspace (->full-ref (:workspace env))
                :start     (inc (* page-number (page-size)))
                :pagesize  (page-size)}]
    (-> (read-ref (->ref type) params)
        :Results)))

(defn read-all
  ([env type] (read-all env type 1))
  ([env type page-number]
   (let [page (read-page env type page-number)]
     (when (< 0 (count page))
       (read-all env type page-number page))))
  ([env type page-number page]
   (let [[head & rest] page]
     (if head
       (cons head (lazy-seq (read-all env type page-number rest)))
       (read-all env type (inc page-number))))))
