(ns cryptocurrency-kata.core)

(set! *warn-on-reflection* true)

(def ^:private shared-match-keys [:type :time :trade-id :order-id])

(defn group-by-trade [txs]
  (->> txs
       (group-by #(or (:trade-id %) identity))
       (map (fn [[_ txs]]
              (case (count txs)
                1 (first txs)
                2 (let [[from to] (sort-by :amount txs)
                        _ (assert (= :match (:type from)) from)
                        shared-from (select-keys from shared-match-keys)
                        shared-to (select-keys from shared-match-keys)
                        from (apply dissoc from shared-match-keys)
                        to (apply dissoc to shared-match-keys)]
                    (assert (= shared-from shared-to))
                    (assoc shared-from
                      :from from
                      :to to)))))
       (sort-by :time)))
