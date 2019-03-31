(ns cryptocurrency-kata.core)

(defn group-by-trade [txs]
  (->> (group-by :trade-id txs)
       (map (fn [[_trade-id txs]]
              (assert (= 2 (count txs)))
              (let [[from to] (sort-by :amount txs)]
                {:to to
                 :from from})))
       (sort-by (comp :time :from))))
