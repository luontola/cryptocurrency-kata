(ns cryptocurrency-kata.core)

(defn group-by-trade [txs]
  (->> txs
       (group-by #(or (:trade-id %) identity))
       (map (fn [[_ txs]]
              (case (count txs)
                1 (first txs)
                2 (let [[from to] (sort-by :amount txs)]
                    {:to to
                     :from from}))))
       (sort-by (comp :time :from))))
