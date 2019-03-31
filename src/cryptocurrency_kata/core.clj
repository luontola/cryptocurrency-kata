(ns cryptocurrency-kata.core)

(set! *warn-on-reflection* true)

(defn- merge-trade [txs]
  (let [{matches :match fees :fee} (group-by :type txs)
        _ (assert (= 2 (count matches)))
        _ (assert (<= 0 (count fees) 1))
        [source target] (sort-by :amount matches)
        [fee] fees
        common-keys [:type :time :trade-id :order-id]
        common (select-keys source common-keys)
        common2 (select-keys target common-keys)
        _ (assert (= common common2))
        source (apply dissoc source common-keys)
        target (apply dissoc target common-keys)
        fee (apply dissoc fee common-keys)]
    (-> common
        (assoc :source source
               :target target)
        (cond-> fee (assoc :fee fee)))))

(defn group-by-trade [txs]
  (let [trades (filter :trade-id txs)
        non-trades (remove :trade-id txs)]
    (->> trades
         (group-by :trade-id)
         (map (fn [[_ txs]] (merge-trade txs)))
         (concat non-trades)
         (sort-by :time))))
