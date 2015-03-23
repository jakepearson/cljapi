(defproject cljapi "0.1.0-SNAPSHOT"
  :repositories [["rally" "http://alm-build.f4tech.com:8080/nexus/content/groups/public"]
                 ["3rd"   "http://alm-build.f4tech.com:8080/nexus/content/repositories/thirdparty"]
                 ["snapshots" {:url "http://alm-build.f4tech.com:8080/nexus/content/groups/public-snapshots"}]
                 ["releases" {:url "http://alm-build:8080/nexus/content/repositories/releases"
                              :sign-releases false
                              :username "admin"
                              :password "admin123"}]]
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.rallydev/cletus "1.1.68"]])
