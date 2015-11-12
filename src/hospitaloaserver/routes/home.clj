(ns hospitaloaserver.routes.home
  (:require [hospitaloaserver.layout :as layout]
            [hospitaloaserver.controller.home :as home]
            [hospitaloaserver.public.common :as commonfunc]
            [noir.io :as nio]
            [clj-time.coerce :as c]
            [clj-time.local :as l]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :refer [ok]]
            [ring.util.response :refer [file-response]]
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

  (GET "/getunreadmessages" [userid deptid] (home/getunreadmessages userid deptid))

  (GET "/getunreadnotifications" [userid ] (home/getunreadnotifications userid))

  (GET "/addregist" [userid rid] (home/addregist userid rid))

  (GET "/addmessage" [content ftype fromid toid groupid mtype toname fromname] (home/addmessage content ftype fromid toid groupid mtype toname fromname))

  (POST "/addmessage" [content ftype fromid toid groupid mtype toname fromname] (home/addmessage content ftype fromid toid groupid mtype toname fromname))


  (GET "/addgroupmessage" [content ftype fromid toid groupid mtype toname fromname] (home/addgroupmessage content ftype fromid toid groupid mtype toname fromname))

  (POST "/addgroupmessage" [content ftype fromid toid groupid mtype toname fromname] (home/addgroupmessage content ftype fromid toid groupid mtype toname fromname))


  (POST "/addnotification" [content ftype fromid toids  fromname] (home/addnotification content ftype fromid toids  fromname))



  (GET "/getmessagehistory" [fromid toid time] (home/getmessage-history fromid toid time))

  (POST "/getmessagehistory" [fromid toid time] (home/getmessage-history fromid toid time))

  (GET "/getgroupmessagehistory" [fromid groupid time] (home/get-group-message-history fromid groupid time))

  (POST "/getgroupmessagehistory" [fromid groupid time] (home/get-group-message-history fromid groupid time))

 (GET "/getnotificationhistory" [userid  time] (home/getnotificationhistory userid time))

  (POST "/getnotificationhistory" [userid time] (home/getnotificationhistory userid time))


  (GET "/getusersbydeptid" [deptid] (home/getusersbydeptid deptid) )

  (GET "/getusersbyrid" [registrationid] (home/getusersbyrid registrationid) )

  (GET "/adddept" [deptname] (home/adddept deptname))

  (GET "/sendalarm" [userid realname deptname] (home/sendalarm userid realname deptname))

  ;(GET "/firechatvideo" [fromid toid ] (home/firechatvideo fromid toid ))
  (POST "/firechatvideo" [fromid toid ] (home/firechatvideo fromid toid ))

  (POST "/firechatend" [fromid toid ] (home/firechatend fromid toid ))

  ;(GET "/firechatarrived" [fromid toid ] (home/firechatarrived fromid toid ))
  (POST "/firechatarrived" [fromid toid ischating] (home/firechatarrived fromid toid ischating))

  (GET "/adduser" [deptid username realname password]
       (home/adduser deptid username realname password))

   (GET "/addregistration" [registrationtitle makeuserid]
       (home/addregistration registrationtitle makeuserid))


   (GET "/getregistrationsimake" [userid]
       (home/getregistrationsimake userid))

  (GET "/getregistrationsiamin" [userid]
       (home/getregistrationsiamin userid))


  (GET "/getdeptpersonstree" []
       (home/getdeptpersonstree))

  (GET "/getunreadmsgbyuid" [userid] (home/getunreadmsgbyuid userid))

  (GET "/changepassword" [userid newpassword] (home/changepassword userid newpassword))

    (GET "/files/:filename" [filename]

    (file-response (str commonfunc/datapath "upload/" filename))

    )





  (POST "/uploadfile"  [file]

        (println file)
    (try
      (do
       (let [
          uploadpath  (str commonfunc/datapath "upload/")
          timenow (c/to-long  (l/local-now))
          filename (str timenow (:filename file))
          ]
      ;(println filename)
      (nio/upload-file uploadpath  (conj file {:filename filename}))
      (ok {:success true :filename filename :name (:filename file) :filetype (:content-type file)})
      )
      )
    (catch Exception ex

      (ok {:success false :message (.getMessage ex)})
      ))




    )



  (GET "/about" [] (about-page)))

