(ns mobile-patient.model.chat
  (:require [clojure.spec.alpha :as s]))

(s/def ::id string?)
(s/def ::resourceType string?)
(s/def ::participants vector?)
(s/def ::name string?)

(s/def ::chat-spec (s/keys :req-un [::id
                                    ::resourceType
                                    ::participants
                                    ::name]))

(defn get-participants-id-set [chat]
  (->> chat
       :participants
       (filter #(#{"Patient" "Practitioner"} (:resourceType %)))
       (map :id)
       set))

(defn other-participant-id [chat domain-user]
  {:post [(string? %)]}
  (let [participants (->> (:participants chat) (map :id) set)]
    (first (disj participants (:id domain-user)))))

(defn get-patient-id [chat]
  {:post [(string? %)]}
  (->> chat
       :participants
       (filter #(= "Patient" (:resourceType %)))
       first
       :id))
