(ns cryptocurrency-kata.test-data
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer :all]
            [cryptocurrency-kata.coinbase :as coinbase]
            [cryptocurrency-kata.core :as core]
            [medley.core :refer [map-vals]]
            [zprint.core :as zp])
  (:import (java.io File)
           (java.time Instant)
           (java.util UUID)))

(set! *warn-on-reflection* true)

(defn- strip-types [rows]
  (map (fn [row]
         (->> (assoc row :time (.toString ^Instant (:time row)))
              (map-vals (fn [val]
                          (if (instance? UUID val)
                            (.toString ^UUID val)
                            val)))))
       rows))

(def ^:private zprint-opts
  {:map {:comma? false
         :force-nl? true
         :key-order [:type :time :currency :amount :balance
                     :transfer-id :trade-id :order-id
                     :source :target :fee]}})

(defn- format-for-edn [rows]
  (-> ^String (zp/zprint-str (vec rows) zprint-opts)
      (.replace " 0E-16M\n" " 0.0000000000000000M\n")))

(defn- convert-files [^File dir]
  (doseq [^String filename (.list dir)]
    (when (.endsWith filename ".csv")
      (let [input (io/file dir filename)
            output (io/file dir (.replaceFirst filename ".csv$" ".edn"))]
        (assert (not= input output))
        (println (str input) "->" (str output))
        (->> (slurp input)
             (coinbase/parse-account-report-csv)
             (strip-types)
             (format-for-edn)
             (spit output))))))

(comment
  (convert-files (io/file "data"))

  (->> (slurp "data/all.edn")
       (edn/read-string)
       (core/group-trades)
       (format-for-edn)
       (spit "data/all-grouped.edn"))

  (->> (slurp "data/all-grouped.edn")
       (edn/read-string)
       (reduce core/accounts-view nil)
       (format-for-edn)
       (spit "data/all-accounts.edn")))
