(ns practitioner.events
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe reg-event-fx reg-event-db]]
            [mobile-patient.ui :as ui]
            [clojure.string :as str]
            [mobile-patient.lib.helper :as h]))

(reg-event-fx
 :boot
 (fn [_ [_ user-id]]
   {:async-flow
    {:first-dispatch [:do-load-user user-id]
     :rules
     [
      {:when     :seen?
       :events   :success-load-user
       :dispatch-n '([:do-load-practitioner] [:do-load-all-users])}

      {:when     :seen-both?
       :events   [:success-load-practitioner :success-load-all-users]
       :dispatch [:do-load-practitioner-patients]}
      ]}}))


;; load-practitioner
(reg-event-fx
 :do-load-practitioner
 (fn [{:keys [db]} _]
   (let [user-ref @(subscribe [:user-ref])]
     (assert user-ref)
     {:fetch {:uri (str "/Practitioner/" user-ref)
              :success :success-load-practitioner}})))

(reg-event-db
 :success-load-practitioner
 (fn [db [_ practitioner-data]]
   (assoc db :practitioner-data practitioner-data)))


;; load-practitioner-patients
(reg-event-fx
 :do-load-practitioner-patients
 (fn [{:keys [db]} _]
   (let [user-ref @(subscribe [:user-ref])]
     (assert user-ref)
     {:fetch {:uri (str "/Patient?general-practitioner=" user-ref)
              :success :success-load-practitioner-patients}})))

(reg-event-db
 :success-load-practitioner-patients
 (fn [db [_ patients-data]]
   (let [all-users-data (:all-users db)
         pat-ids (->> patients-data
                      :entry
                      (map #(get-in % [:resource :id]))
                      set)
         filtered-users (->> all-users-data
                             :entry
                             (map #(get % :resource))
                             (filter #(pat-ids (get-in % [:ref :id]))))]
     (assoc db :practitioner-patients (into {}
                                            (map #(vector (:id %) %))
                                            filtered-users) ; from list to map by id
            :current-screen :main))))


;;
(reg-event-db
 :set-current-patient
 (fn [db [_ id]]
   (assoc db :patient-data
          (get-in db [:practitioner-patients id]))))
