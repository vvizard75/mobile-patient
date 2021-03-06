(ns mobile-patient.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [subscribe reg-sub reg-sub-raw]]
            [cljs-time.core :as time]
            [mobile-patient.model.patient :as patient-model]
            [mobile-patient.model.chat :as chat-model]
            ))

;; -- Subscriptions----------------------------------------------------------
#_(reg-sub-raw
 :user-id
 (fn [db _] (reaction (get-in @db [:user :id]))))

(reg-sub
 :user-id
 (fn [db _]
   (:user-id db)))

(reg-sub
 :user
 (fn [db _]
   (get (:users db) (:user-id db))))

#_(reg-sub-raw
 :user-ref
 (fn [db _] (reaction (get-in @db [:user :ref :id]))))

(reg-sub
 :user-ref
 (fn [db _]
   (get-in @(subscribe [:user]) [:ref :id])))

(reg-sub
 :patient
 (fn [db _]
   (get @(subscribe [:patients-data])
        (:patient-id db))))

(reg-sub
 :practitioner
 (fn [db _]
   (get @(subscribe [:practitioners-data])
        (:practitioner-id db))))

(reg-sub
 :patient-name
 (fn [db _]
   (patient-model/get-official-name @(subscribe [:patient]))))

(reg-sub
 :users
 (fn [db _]
   (vals (:users db))))

(reg-sub
 :chat
 (fn [db _] (:chat db)))

(reg-sub
 :messages
 (fn [db _] (:messages db)))

(reg-sub
 :message
 (fn [db _] (:message db)))

(reg-sub
 :active-medication-statements
 (fn [db [_ pat-ref]] (get-in db [:active-medication-statements pat-ref])))

(reg-sub
 :other-medication-statements
 (fn [db [_ pat-ref]] (get-in db [:other-medication-statements pat-ref])))

(reg-sub
  :patient-id
  (fn [db _]
    #_(get-in db [:patient-data :id])
    (get db :patient-id)))

(reg-sub
  :practitioner-ref
  (fn [db _]
    (get-in db [:practitioner-data :id])))

(reg-sub
 :get-in
 (fn [db [_ path]]
   (get-in db path)))

(reg-sub
 :get-current-screen
 (fn [db _]
   (:current-screen db)))

(reg-sub
 :get-patients-general-practitioner-ids
 (fn [db [_]]
   (->> (get-in db [:patient-data :generalPractitioner])
        (filter #(= (:resourceType %) "Practitioner"))
        (map :id))))

(defn extract[item]
  {:value (:value item)
   :code (get-in item [:code :coding 0 :code])
   :date-time (time/date-time (js/Date. (get-in item [:effective :dateTime])))})

(defn prepare-observation [data]
  (->> data
       ;;(map :resource)
       (map extract)
       (group-by :code)
       (map (fn [[key values]] [key (sort #(time/after? (:date-time %1) (:date-time %2))values)]))
       (into {})
       ))

(reg-sub
 :get-observations
 (fn [db _]
   (let [remote-data (:observations db)
         data-key (->> remote-data
                      keys
                      (map name)
                      (filter #(clojure.string/ends-with? % "-data"))
                      first
                      keyword)]
     (if (= (:status remote-data) :succeed)
       (update-in remote-data [data-key] prepare-observation)
       remote-data))))

(reg-sub
 :get-patient-ref-by-id
 (fn [db [_ user-id]]
   (-> @(subscribe [:patients])
       (get user-id)
       (get-in [:ref :id]))))

(reg-sub
 :patients
 (fn [db [_]]
   (:patients db)))

(reg-sub
 :patients-data
 (fn [db [_]]
   (:patients-data (:patients db))))


(reg-sub
 :practitioners
 (fn [db [_]]
   (get-in db [:practitioners])))

(reg-sub
 :practitioners-data
 (fn [db [_]]
   (get-in db [:practitioners :practitioners-data])))


(reg-sub
 :user-by-id
 (fn [db [_ id]]
   (or (get @(subscribe [:patients-data]) id)
       (get @(subscribe [:practitioners-data]) id))
   ))

(reg-sub
 :practice-groups
 (fn [db _]
   (->> (:chats db)
        (filter #(= "practice-group" (:name %)))
        (map #(assoc % :unread @(subscribe [:get-unread-count (:id %)]))))))

(reg-sub
 :personal-chats
 (fn [db _]
   (let [domain-user @(subscribe [:domain-user])
         chats (->> (:chats db)
                    (filter #(= "personal-chat" (:name %))))
         contragents @(subscribe [:domain-user-contragents-data])
         persons-with-chat (->> chats
                                (map #(assoc % :unread @(subscribe [:get-unread-count (:id %)])))
                                (map #(assoc (get contragents (chat-model/other-participant-id % domain-user)) :chat %))
                                )]
     persons-with-chat)))

(reg-sub
 :last-time
 (fn [db _]
   (:last-time db)))

(reg-sub
 :get-unread-count
 (fn [db [_ chat-id]]
   (let [domain-user-id (:id @(subscribe [:domain-user]))
         unread (->> (get-in db [:unread-messages chat-id])
                     (remove #(= (get-in % [:author :id])
                                 domain-user-id))
                     count)]
     (if (pos? unread)
       unread))))
