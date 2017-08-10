(ns mobile-patient.db
  (:require [clojure.spec.alpha :as s]
            [mobile-patient.model.user :as user-model]
            [mobile-patient.model.patient :as patient-model]
            [mobile-patient.model.practitioner :as practitioner-model]
            ))

;; spec of app-db
(s/def ::status #{:not-asked :loading :succeed :failure})

(s/def ::user-id string?)
(s/def ::patient-id string?)

(s/def ::users
  (s/map-of string? ::user-model/user-spec))


(s/def ::patients-data
  (s/map-of string? ::patient-model/patient-spec))

(s/def ::patients
  (s/keys :req-un [::status
                   ::patients-data]))


(s/def ::practitioners-data
  (s/map-of string? ::practitioner-model/practitioner-spec))

(s/def ::practitioners
  (s/keys :req-un [::status
                   ::practitioners-data]))



(s/def ::app-db
  (s/and (s/keys :req-un [::user-id
                          ::patient-id
                          ::users
                          ::patients
                          ::practitioners
                          ]
                 :opt-un [])
         #(not (contains? % nil))))

;; initial state of app-db
(def app-db {:spinner {}
             :current-screen :login
             :config {:base-url "https://sansara.health-samurai.io"
                      :client-id "sansara"}
             :active-medication-statements {}
             :other-medication-statements {}
             :observations {:status :not-asked}
             :users {}
             :chats []
             :messages []
             :user nil
             :patient-data {}
             :practitioner-data {}

             :user-id ""
             :patient-id ""
             :patients {:status :not-asked :patients-data {}}
             :practitioners {:status :not-asked :practitioners-data {}}
             })
