(ns cryptocurrency-kata.coinbase-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [cryptocurrency-kata.coinbase :as coinbase]
            [medley.core :refer [map-vals]]
            [zprint.core :as zp])
  (:import (java.io File)
           (java.time Instant)
           (java.util UUID)))

(set! *warn-on-reflection* true)

(deftest test-parse-account-report-csv
  (is (= [{:type :deposit
           :time (Instant/parse "2018-02-04T14:48:06.142Z")
           :amount 0.0099876500000000M
           :balance 0.0099876500000000M
           :currency :BTC
           :transfer-id (UUID/fromString "8afb99ca-b8c3-4405-b4de-30bf2e7a0c86")}
          {:type :match
           :time (Instant/parse "2018-02-04T21:09:11.089Z")
           :amount -0.0099876500000000M
           :balance 0.0000000000000000M
           :currency :BTC
           :trade-id 11309314
           :order-id (UUID/fromString "53d6f6cc-db5e-40d6-aab6-a9e5cc2f0c46")}]
         (coinbase/parse-account-report-csv
          (str "type,time,amount,balance,amount/balance unit,transfer id,trade id,order id\n"
               "deposit,2018-02-04T14:48:06.142Z,0.0099876500000000,0.0099876500000000,BTC,8afb99ca-b8c3-4405-b4de-30bf2e7a0c86,,\n"
               "match,2018-02-04T21:09:11.089Z,-0.0099876500000000,0.0000000000000000,BTC,,11309314,53d6f6cc-db5e-40d6-aab6-a9e5cc2f0c46\n")))))

(defn- format-for-edn [rows]
  (let [rows (map (fn [row]
                    (->> (assoc row :time (.toString ^Instant (:time row)))
                         (map-vals (fn [val]
                                     (if (instance? UUID val)
                                       (.toString ^UUID val)
                                       val)))))
                  rows)]
    (zp/zprint-str (vec rows)
                   {:map {:comma? false
                          :key-order [:type
                                      :time
                                      :amount
                                      :balance
                                      :currency
                                      :transfer-id
                                      :trade-id
                                      :order-id]}})))

(defn- convert-files [^File dir]
  (doseq [^String filename (.list dir)]
    (when (.endsWith filename ".csv")
      (let [input (io/file dir filename)
            output (io/file dir (.replaceFirst filename ".csv$" ".edn"))]
        (assert (not= input output))
        (println (str input) "->" (str output))
        (->> (slurp input)
             (coinbase/parse-account-report-csv)
             (format-for-edn)
             (spit output))))))

(comment
 (convert-files (io/file "data")))
