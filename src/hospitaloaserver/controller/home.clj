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

  (ok db/get-depts)

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



