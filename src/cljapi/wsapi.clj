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
          :page-size 100}
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

(defn translate-type [type]
  (if (= :UserStory type)
    :HierarchicalRequirement
    type))

(defn ->ref
  ([env value]
   (let [url (cond
               (string? value)  value
               (keyword? value) (str "/slm/webservice/v2.0/" (-> value translate-type name))
               :default         (:_ref value))]
     (string/replace url (:hostname env) "")))
  ([env value oid]
   (str (->ref env value) "/" oid)))

(defn ->full-ref
  ([env value]
   (->> value
        (->ref env)
        (str (:hostname env))))
  ([env value oid]
   (str (->full-ref env value) "/" oid)))

(defn user [env]
  (read-ref env (->ref env :user)))

(defn subscription [env]
  (read-ref env (->ref env :subscription)))

(defn ->oid [value]
  (:ObjectID value))

(defn workspaces [env]
  (let [subscription   (->> (subscription env) (->ref env) (read-ref env))
        workspace-refs (->ref env (:Workspaces subscription))]
    (->> (read-ref env workspace-refs)
         :Results)))

(defn schema [env]
  (let [workspace-oid (->oid (:workspace env))]
    (-> (read-ref env (str "/slm/schema/v2.0/workspace/" workspace-oid))
        :Results)))

(defn read-page [env type page-number]
  (let [page-size {:page-size env}
        params    {:fetch     true
                   :workspace (->full-ref env (:workspace env))
                   :start     (inc (* page-number (:page-size env)))
                   :pagesize  page-size}]
    (-> (read-ref env (->ref env type) params)
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
