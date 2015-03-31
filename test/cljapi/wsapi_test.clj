(ns cljapi.wsapi-test
  (:require [cljapi.wsapi :as wsapi]
            [clojure.test :refer :all]
            [cletus.config :as config]))

(def the-env
  (delay
   (let [auth-key (config/string "RALLY_API_KEY")
         env      {:auth-key auth-key
                   :hostname "https://rally1.rallydev.com"}]
     (assoc env :workspace (-> (wsapi/workspaces env) first)))))

(def the-schema (delay (-> (wsapi/schema @the-env)))) 

(deftest ->ref
  (let [ref "/slm/webservice/v2.0/defect"
        env {:hostname "https://rally1.rallydev.com"}]
    (is (= ref (wsapi/->ref env :defect)))
    (is (= ref (wsapi/->ref env (str (:hostname env) ref))))
    (is (= (str ref "/123") (wsapi/->ref env :defect 123)))
    (is (= ref (wsapi/->ref env {:_ref ref})))
    (is (= ref (wsapi/->ref env ref)))))

(deftest ->full-ref
  (let [ref      "/slm/webservice/v2.0/defect"
        env      {:hostname "https://rally1.rallydev.com"}
        full-ref (str (:hostname env) ref)]
    (is (= full-ref (wsapi/->full-ref env :defect)))
    (is (= full-ref (wsapi/->full-ref env (str (:hostname env) ref))))
    (is (= (str full-ref "/123") (wsapi/->full-ref env :defect 123)))
    (is (= full-ref (wsapi/->full-ref env {:_ref ref})))
    (is (= full-ref (wsapi/->full-ref env ref)))))

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


