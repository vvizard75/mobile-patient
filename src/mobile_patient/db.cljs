(ns mobile-patient.db
  (:require [clojure.spec.alpha :as s]))

;; spec of app-db
(s/def ::greeting string?)
(s/def ::app-db
  (s/keys :req-un [::greeting]))

;; initial state of app-db
(def app-db {:config {:base-url "https://sansara.health-samurai.io"
                      :client-id "sansara"}
             :chats []
             :messages []
             :user {:id "patient" :ref {:id "fe0ecce6-a577-4cde-8c02-f7c482111de8" :resourceType "Patient"}}})
