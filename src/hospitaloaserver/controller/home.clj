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



