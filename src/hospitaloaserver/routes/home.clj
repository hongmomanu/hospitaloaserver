(ns hospitaloaserver.routes.home
  (:require [hospitaloaserver.layout :as layout]
            [hospitaloaserver.controller.home :as home]
            [hospitaloaserver.public.common :as commonfunc]
            [noir.io :as nio]
            [clj-time.coerce :as c]
            [clj-time.local :as l]
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

  (GET "/addmessage" [content ftype fromid toid groupid mtype toname fromname] (home/addmessage content ftype fromid toid groupid mtype toname fromname))

  (POST "/addmessage" [content ftype fromid toid groupid mtype toname fromname] (home/addmessage content ftype fromid toid groupid mtype toname fromname))


  (GET "/addgroupmessage" [content ftype fromid toid groupid mtype toname fromname] (home/addgroupmessage content ftype fromid toid groupid mtype toname fromname))

  (POST "/addgroupmessage" [content ftype fromid toid groupid mtype toname fromname] (home/addgroupmessage content ftype fromid toid groupid mtype toname fromname))



  (GET "/getmessagehistory" [fromid toid time] (home/getmessage-history fromid toid time))

  (POST "/getmessagehistory" [fromid toid time] (home/getmessage-history fromid toid time))

  (GET "/getgroupmessagehistory" [fromid groupid time] (home/get-group-message-history fromid groupid time))

  (POST "/getgroupmessagehistory" [fromid groupid time] (home/get-group-message-history fromid groupid time))

  (GET "/getusersbydeptid" [deptid] (home/getusersbydeptid deptid) )

  (GET "/adddept" [deptname] (home/adddept deptname))

  (GET "/adduser" [deptid username realname password]
       (home/adduser deptid username realname password))

  (GET "/getunreadmsgbyuid" [userid] (home/getunreadmsgbyuid userid))


  (POST "/uploadfile"  [file]

    (let [
          uploadpath  (str commonfunc/datapath "upload/")
          timenow (c/to-long  (l/local-now))
          filename (str timenow (:filename file))
          ]
      ;(println filename)
      (nio/upload-file uploadpath  (conj file {:filename filename}))
      (ok {:success true :filename filename})
      )

    )



  (GET "/about" [] (about-page)))

