(ns cryptocurrency-kata.coinbase
  (:require [clojure.data.csv :as csv]
            [clojure.edn :as edn]
            [clojure.string :as str])
  (:import (java.time Instant)
           (java.util UUID List)))

(set! *warn-on-reflection* true)

(defn- assoc-column-index [column header-row]
  (let [index (.indexOf ^List header-row (:header column))]
    (assert (not (neg? index)) column)
    (assoc column :column-index index)))

(defn- parse-row [row columns]
  (->> row
       (map-indexed (fn [index value]
                      (let [column (nth columns index)
                            parser (or (:parser column)
                                       identity)]
                        (when (not (str/blank? value))
                          [(:key column) (parser value)]))))
       (into {})))

(def ^:private column-mapping
  [{:header "portfolio"
    :key :portfolio
    :parser keyword}
   {:header "type"
    :key :type
    :parser keyword}
   {:header "time"
    :key :time
    :parser #(Instant/parse %)}
   {:header "amount"
    :key :amount
    :parser #(BigDecimal. ^String %)}
   {:header "balance"
    :key :balance
    :parser #(BigDecimal. ^String %)}
   {:header "amount/balance unit"
    :key :currency
    :parser keyword}
   {:header "transfer id"
    :key :transfer-id
    :parser #(UUID/fromString %)}
   {:header "trade id"
    :key :trade-id
    :parser edn/read-string}
   {:header "order id"
    :key :order-id
    :parser #(UUID/fromString %)}])

(defn parse-account-report-csv [content]
  (let [[header-row & rows] (csv/read-csv content)
        columns (map #(assoc-column-index % header-row) column-mapping)]
    (assert (= header-row (map :header column-mapping))
            {:header-row header-row})
    (map #(parse-row % columns) rows)))
