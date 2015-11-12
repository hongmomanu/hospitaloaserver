(ns hospitaloaserver.controller.home
  (:use [compojure.core]
        [org.httpkit.server]
        )
  (:require [hospitaloaserver.db.core :as db]
            [hospitaloaserver.public.websocket :as websocket]

            [ring.util.http-response :refer [ok]]
            [clojure.data.json :as json]
            [monger.json]
            [taoensso.timbre :as timbre]
             [monger.operators :refer [$gt $lt $and $ne $push $or $nin $in]]
            [cheshire.core :refer :all]
            [clj-time.core :as t]
            [clj-time.format :as f]
            )
  (:import [org.bson.types ObjectId]
           [java.util  Date Calendar]
           )
  )

(def custom-formatter (f/formatter "yyyy-MM-dd'T'hh:mm:ss'Z'"))

(declare send-message-online send-message-online-group send-notification-online getfulluserinfo deptpersons)
(defn getdepts[]

  (let [depts (db/get-depts)]
     (ok (map #(conj % {:title (:deptname %) :persons (count (db/get-users-by-cond {:deptid (str (:_id %))}))}) depts))
    )


  )

(defn getusersbydeptid [deptid]
  (ok (db/get-users-by-cond {:deptid deptid}))

  )

(defn getunreadmsgbyuid [userid]

  (ok (db/get-unreadmsg-by-uid {:userid userid :isread false}))


  )
(defn getmessage-history [fromid toid time]


  (let [
        datetime (f/parse (f/formatters :date-time-no-ms) time)
        ]

    (db/get-message {$and [{$or [{:fromid fromid :toid toid} {:fromid  toid :toid fromid}]} {:time { $lt (.toDate datetime) }} ]} 10)
    )


  )



(defn getunreadmessages [userid deptid]

  (let [
        unreadperson (db/get-unreadmsg-by-uid {:toid userid :isread false})



        unreadgroups (db/get-unreadmsg-by-uid {:toid deptid :userids {$nin [userid]}})

        msgs (concat unreadperson unreadgroups)

        ]
    (dorun (map #(send-message-online  userid %) msgs))

     (dorun (map #(db/update-group-message-byid (:_id %)  {:userids userid}) unreadgroups))

     (ok {:success true} )
   )

  )


(defn getunreadnotifications [userid]

  (let [
        unreadnotifications (db/get-notification-by-cond {:toids {$in [userid]} :userids {$nin [userid]}})

        ]
     (dorun (map #(send-notification-online  userid %) unreadnotifications))

     (ok {:success true} )
   )

  )

(defn addregist [userid rid]

  (println userid rid)

  (try

    (let [item (db/get-registrations-by-cond {:_id  (ObjectId. rid) :checkids {$in [userid]}})


          ]

      (println userid rid item)
      (when (= (count item) 0)

        (db/update-regist-checkids (ObjectId. rid) {:checkids userid})

        )



      (ok {:success true})

      )

      (catch Exception ex
        (ok {:success false :message (.getMessage ex)})
        )

    )


  )

(defn get-group-message-history [fromid groupid time]


  (let [
        datetime (f/parse (f/formatters :date-time-no-ms) time)
        ]

    (db/get-message {$and [{:groupid  groupid } {:time { $lt (.toDate datetime) }} ]} 10)
    )


  )

(defn getnotificationhistory [fromid  time]


  (let [
        datetime (f/parse (f/formatters :date-time-no-ms) time)
        ]

    (db/get-notification {$and [ {$or  [{:fromid  fromid } {:toids {$in [fromid]}} ]} {:time { $lt (.toDate datetime) }} ]} 10)



    )


  )

(defn getdeptpersonstree []
  (let [
        depts (db/get-depts)

        deptwithusers (map #(conj {} {:name (:deptname %) :isparent true :tree (deptpersons (str (:_id %))) }) depts)

        ]
   (ok deptwithusers)
   )



  )
(defn deptpersons [deptid]
  (let [
        users (db/get-users-by-cond {:deptid deptid})

        userstreeitem (map #(conj {:name (:realname %) :checked true} {:id (:_id %)}) users)
        ]

    userstreeitem
    )


  )
(defn addmessage [content ftype fromid toid groupid mtype toname fromname]

  (try

    (let [
          item (db/insert-message
                {
                 :content content :ftype ftype
                 :fromid fromid :toid toid :isread false
                 :toname toname :fromname fromname
                 :groupid groupid :mtype mtype :time (new Date)
                 }
             )

          ]

      (send-message-online toid item)

      (ok {:success true :data item})

      )

      (catch Exception ex
        (ok {:success false :message (.getMessage ex)})
        )

    )

  )

(defn addnotification [content ftype fromid toids  fromname]

  (println toids  )
  (try
   (let [

            item (db/insert-notification
                {
                 :content content :ftype ftype
                 :fromid fromid :toids toids :userids [] :isread false
                 :fromname fromname  :time (new Date)
                 }
             )

          ]



      (dorun (map #(send-notification-online % item) toids))

     (db/update-notification-byid (:_id item)  {:userids fromid})


      )
    (ok {:success true})


    (catch Exception ex
        (ok {:success false :message (.getMessage ex)})
        )
    )







  )

(defn addgroupmessage [content ftype fromid toid groupid mtype toname fromname]

  (try


    (let [
          item (db/insert-message
                {
                 :content content :ftype ftype
                 :fromid fromid :toid toid :isread false
                 :toname toname :fromname fromname :userids []
                 :groupid groupid :mtype mtype :time (new Date)
                 }
             )

          groupitems (db/get-users-by-cond {$and [{:deptid groupid} {:_id {$ne (ObjectId. fromid)}}]})

          ]

      (dorun (map #(send-message-online-group (str (:_id %)) item) groupitems))
      (db/update-group-message-byid (:_id item)  {:userids fromid})


      (ok {:success true })

      )

      (catch Exception ex
        (ok {:success false :message (.getMessage ex)})
        )

    )

  )



(defn firechatvideo [fromid toid]


 (doseq [channel (keys @websocket/channel-hub)]
    (when (= (get  (get @websocket/channel-hub channel) "userid") toid)

      (do
        (timbre/info "firechatvideo-online : " fromid )

        (send! channel (generate-string
                       {

                         :data  {:fromid fromid :toid toid}
                         :type "firechatvideo"
                         }
                       )
        false)


        )



      )
    )

  (ok {:success true})


 )


(defn addregistration [registrationtitle makeuserid]

  (try


      (do
        (db/add-registration {:title registrationtitle :makeuserid makeuserid
                              :time (new Date)
                              :checkids [makeuserid]})
        (ok {:success true})

        )

    (catch Exception ex
        (ok {:success false :message (.getMessage ex)})
        )
      )





  )


(defn getusersbyrid [registrationid]
  (let [

        checkids (:checkids (db/get-registration-by-id (ObjectId. registrationid) ))

        users (map #(getfulluserinfo %) checkids)

        ]

    (ok users)
    )


  )

(defn getfulluserinfo [userid]

   (let [

       user (db/get-user-by-id (ObjectId. userid))
       deptname (:deptname (db/get-dept-by-id (ObjectId. (:deptid user))))


        ]

       (conj user {:deptname deptname})
    )


  )


(defn getregistrationsimake [userid]

  (ok (db/get-registrations-by-cond  {:makeuserid userid}))

  )

(defn getregistrationsiamin [userid]

  (ok (db/get-registrations-by-cond {:checkids {$in [userid]}}))

  )



(defn sendalarm [userid realname deptname]

  (let [
        nums (count (keys @websocket/channel-hub))

        ]
    (doseq [channel (keys @websocket/channel-hub)]
    (when-not (= (get  (get @websocket/channel-hub channel) "userid") userid)

      (do
        (timbre/info "firesendalarm-online : " userid )

        (send! channel (generate-string
                       {

                         :data  {:userid userid :realname realname :deptname deptname}
                         :type "firealarm"
                         }
                       )
        false)


        )



      )
    )
    (ok {:success true :message (str "有" (- nums 1) "位工作人员收到警报")})

    )



  )


(defn firechatend [fromid toid]


 (doseq [channel (keys @websocket/channel-hub)]
    (when (= (get  (get @websocket/channel-hub channel) "userid") toid)

      (do
        (timbre/info "firechatvideo-online : " fromid )

        (send! channel (generate-string
                       {

                         :data  {:fromid fromid :toid toid}
                         :type "firechatend"
                         }
                       )
        false)


        )



      )
    )

  (ok {:success true})


 )

(defn firechatarrived [fromid toid ischating]


 (doseq [channel (keys @websocket/channel-hub)]
    (when (= (get  (get @websocket/channel-hub channel) "userid") fromid)

      (do
        (timbre/info "firechatarrived-online : " toid )

        (send! channel (generate-string
                       {

                         :data  {:fromid fromid :toid toid :ischating ischating}
                         :type "firechatarrived"
                         }
                       )
        false)


        )



      )
    )
  (ok {:success true})


 )


(defn send-message-online [userid msg ]

  (doseq [channel (keys @websocket/channel-hub)]
    (when (= (get  (get @websocket/channel-hub channel) "userid") userid)

      (do
        (timbre/info "send-message-online : " msg )

        (send! channel (generate-string
                       {

                         :data  msg
                         :type "message"
                         }
                       )
        false)

        (db/update-message-byid (:_id msg) {:isread true})
        )



      )
    )


  )

(defn send-notification-online [userid msg ]

  (doseq [channel (keys @websocket/channel-hub)]
    (when (= (get  (get @websocket/channel-hub channel) "userid") userid)

      (do
        (timbre/info "send-notification-online : " msg )

        (send! channel (generate-string
                       {

                         :data  msg
                         :type "notification"
                         }
                       )
        false)

        (db/update-notification-byid (:_id msg) {:userids userid})
        )



      )
    )


  )

(defn send-message-online-group [userid msg ]

  (doseq [channel (keys @websocket/channel-hub)]
    (when (= (get  (get @websocket/channel-hub channel) "userid") userid)

      (do
        (timbre/info "send-message-online : " msg )

        (send! channel (generate-string
                       {

                         :data  msg
                         :type "message"
                         }
                       )
        false)

        (db/update-group-message-byid (:_id msg)  {:userids userid})

        )



      )
    )


  )


(defn login [username password]

  (try
      (let [
             item (db/get-users-by-cond {:username username :password password})
             ]

      (do
        (if (empty? item)
          (ok {:success false :message "用户或密码错误"})
          (ok {:success true :message "登录成功" :user (conj (first item) {:deptname (:deptname (db/get-dept-by-id (ObjectId. (:deptid (first item)))))})})
          )

        )
      )
      (catch Exception ex
        (ok {:success false :message (.getMessage ex)})
        )

    )

  )


(defn changepassword [userid newpassword]

  (try


      (do
        (db/update-users-by-id (ObjectId. userid) {:password newpassword})
        (ok {:success true :message "修改密码成功"})
        )

      (catch Exception ex
        (ok {:success false :message (.getMessage ex)})
        )

    )


  )


(defn adduser [deptid username realname password]

  (try
      (let [
             item (db/get-users-by-cond {:username username})
             ]

      (do
        (if (empty? item)
          (ok {:success true :message "添加成功" :id
               (:_id (db/add-user
                      {:username username :realname realname
                       :password password :deptid deptid}))})
          (ok {:success false :message (str "添加失败!" "用户:"username ",已经存在")})
          )

        )
      )
      (catch Exception ex
        (ok {:success false :message (.getMessage ex)})
        )

    )

  )

(defn adddept [deptname]
   (try
      (let [
             item (db/get-depts-by-cond {:deptname deptname})
             ]

      (do
        (if (empty? item)
          (ok {:success true :message "添加成功" :id  (:_id (db/add-dept {:deptname deptname}))})
          (ok {:success false :message (str "添加失败!" "科室:"deptname ",已经存在")})
          )

        )
      )
      (catch Exception ex
        (ok {:success false :message (.getMessage ex)})
        )

    )




  )



