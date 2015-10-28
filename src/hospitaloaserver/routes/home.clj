(ns hospitaloaserver.routes.home
  (:require [hospitaloaserver.layout :as layout]
            [hospitaloaserver.controller.home :as home]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :refer [ok]]
            [clojure.java.io :as io]))

(defn home-page []
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page []
  (layout/render "about.html"))

(defroutes home-routes
  (GET "/" [] (home-page))

  (GET "/login" [username password] (home/login username password))

  (GET "/getdepts" [] (home/getdepts))

  (GET "/getusersbydeptid" [deptid] (home/getusersbydeptid deptid) )

  (GET "/adddept" [deptname] (home/adddept deptname))

  (GET "/adduser" [deptid username realname password]
       (home/adduser deptid username realname password))


  (GET "/about" [] (about-page)))

