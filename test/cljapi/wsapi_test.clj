(ns cljapi.wsapi-test
  (:require [cljapi.wsapi :as wsapi]
            [clojure.test :refer :all]
            [cletus.config :as config]))

(def the-env
  (delay
   (let [auth-key (config/string "RALLY_API_KEY")
         env      {:auth-key auth-key}]
     (assoc env :workspace (-> (wsapi/workspaces env) first)))))

(def the-schema (delay (-> (wsapi/schema @the-env)))) 

(deftest ->ref
  (let [ref "/slm/webservice/v2.0/defect"]
    (is (= ref (wsapi/->ref :defect)))
    (is (= ref (wsapi/->ref (str (wsapi/host-name) ref))))
    (is (= (str ref "/123") (wsapi/->ref :defect 123)))
    (is (= ref (wsapi/->ref {:_ref ref})))
    (is (= ref (wsapi/->ref ref)))))

(deftest ->full-ref
  (let [ref      "/slm/webservice/v2.0/defect"
        full-ref (str (wsapi/host-name) ref)]
    (is (= full-ref (wsapi/->full-ref :defect)))
    (is (= full-ref (wsapi/->full-ref (str (wsapi/host-name) ref))))
    (is (= (str full-ref "/123") (wsapi/->full-ref :defect 123)))
    (is (= full-ref (wsapi/->full-ref {:_ref ref})))
    (is (= full-ref (wsapi/->full-ref ref)))))

(deftest user
  (let [user (wsapi/user @the-env)]
    (is (:FirstName user))
    (is (:LastName user))))

(deftest subscription
  (let [subscription (wsapi/subscription @the-env)]
    (is (:Name subscription))
    (is (:SubscriptionID subscription))))

(deftest oid
  (is (= 123 (wsapi/->oid {:ObjectID 123}))))

(deftest workspaces
  (let [workspaces (wsapi/workspaces @the-env)
        workspace (first workspaces)]
    (is (vector? workspaces))
    (is (:Subscription workspace))
    (is (:Projects workspace))))

(deftest schema
  (is (= 58 (count @the-schema))))


