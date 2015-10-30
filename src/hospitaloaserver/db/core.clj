(ns hospitaloaserver.db.core
  (:require
    [clojure.java.jdbc :as jdbc]
    [yesql.core :refer [defqueries]]
    [taoensso.timbre :as timbre]
    [monger.core :as mg]
    [monger.collection :as mc]
    [monger.operators :refer :all]
    [monger.query :refer [with-collection find options paginate sort fields limit]]
    [environ.core :refer [env]])
  )

(defonce db (let [uri (get (System/getenv) "MONGOHQ_URL" "mongodb://jack:1313@111.1.76.108/hospitaloaapp")
                  {:keys [conn db]} (mg/connect-via-uri uri)]
              db))


(defn get-depts []

   (mc/find-maps
    db "depts" {}
    )

  )

(defn get-depts-by-cond [cond]

  (mc/find-maps
    db "depts" cond
    )

  )

(defn get-dept-by-id [oid]

  (mc/find-map-by-id
    db "depts" oid
    )
  )
(defn get-users-by-cond [cond]

  (mc/find-maps
    db "users" cond
    )

  )

(defn add-user [item]

  (mc/insert-and-return db "users" item)

  )


(defn add-dept [item]

  (mc/insert-and-return db "depts" item)

  )

(defn insert-message [item]

  (mc/insert-and-return db "messages" item)

  )

(defn get-unreadmsg-by-uid [cond]
  (mc/find-maps
    db "messages" cond
    )

  )

(defn get-message [conds size]
  (with-collection db "messages"
    (find conds)
    (sort {:time -1})
    (limit size))

  )

(defn update-message-byid [oid data]
   (mc/update-by-id db "messages" oid {$set data})
  )





