(ns cryptocurrency-kata.core)

(set! *warn-on-reflection* true)

(def ^:private common-match-keys [:type :time :trade-id :order-id])

(defn- merge-trade [txs]
  (let [{matches :match fees :fee} (group-by :type txs)
        _ (assert (= 2 (count matches)))
        _ (assert (>= 1 (count fees)))
        [source target] (sort-by :amount matches)
        [fee] fees
        common (select-keys source common-match-keys)
        common2 (select-keys target common-match-keys)
        _ (assert (= common common2))
        source (apply dissoc source common-match-keys)
        target (apply dissoc target common-match-keys)
        fee (apply dissoc fee common-match-keys)]
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
