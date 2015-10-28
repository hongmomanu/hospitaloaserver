(ns hospitaloaserver.controller.home
  (:use compojure.core)
  (:require [hospitaloaserver.db.core :as db]
            ;[doctorserver.public.common :as common]
            [ring.util.http-response :refer [ok]]
            [clojure.data.json :as json]
            [monger.json]
            )
  (:import [org.bson.types ObjectId]
           )
  )

(defn getdepts[]

  (let [depts (db/get-depts)]


    (ok (map #(conj % {:title (:deptname %) :persons (count (db/get-users-by-cond {:deptid (str (:_id %))}))}) depts))
    )


  )

(defn getusersbydeptid [deptid]
  (ok (db/get-users-by-cond {:deptid deptid}))

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



