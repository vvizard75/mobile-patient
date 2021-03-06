(ns practitioner.events
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe reg-event-fx reg-event-db]]
            [mobile-patient.lib.interceptor :refer [validate-spec]]
            [mobile-patient.ui :as ui]
            [clojure.string :as str]
            [mobile-patient.lib.services :as service]
            [mobile-patient.lib.helper :as h]
            [mobile-patient.model.core :refer [list-to-map-by-id]]))

(reg-event-fx
 :boot
 (fn [_ [_ user-id]]
   {:async-flow
    {:first-dispatch [:do-load-user user-id]
     :rules
     [
      {:when     :seen?
       :events   :success-load-user
       :dispatch [:do-load-practitioner]}

      {:when     :seen?
       :events   [:success-load-practitioner]
       :dispatch-n '([:do-load-practitioner-patients]
                     [:do-get-chats]
                     [:set-current-screen :main])}

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
 validate-spec
 (fn [db [_ practitioner-data]]
   (-> db
       (assoc :practitioner-data practitioner-data) ;; legacy
       (assoc :practitioner-id (:id practitioner-data))
       (assoc :practitioners {:status :succeed
                              :practitioners-data {(:id practitioner-data) practitioner-data}})))) ;; redo as service


;; load-practitioner-patients
(reg-event-fx
 :do-load-practitioner-patients
 (fn [{:keys [db]} _]
   {:dispatch [:fetch-patients-data
               {:params {:general-practitioner @(subscribe [:user-ref])}}]}))

(service/reg-get-service
  :fetch-patients-data
  [:patients]
  {:uri "/Patient"}
  :mutator #(list-to-map-by-id (map :resource %)))

;;
(reg-event-db
 :set-current-patient
 validate-spec
 (fn [db [_ id]]
   (assoc db :patient-data (get-in db [:patients id])
             :patient-id id)))
