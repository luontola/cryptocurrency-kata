(ns cryptocurrency-kata.core)

(set! *warn-on-reflection* true)

(def ^:private common-match-keys [:type :time :trade-id :order-id])

(defn- merge-match [txs]
  (assert (= 2 (count txs)))
  (assert (= [:match :match] (map :type txs)))
  (let [[source target] (sort-by :amount txs)
        common (select-keys source common-match-keys)
        common2 (select-keys target common-match-keys)
        _ (assert (= common common2))
        source (apply dissoc source common-match-keys)
        target (apply dissoc target common-match-keys)]
    (assoc common
      :source source
      :target target)))

(defn group-by-trade [txs]
  (->> txs
       (group-by #(or (:trade-id %) identity))
       (map (fn [[_ txs]]
              (case (count txs)
                1 (first txs)
                2 (merge-match txs)
                3 (let [{:keys [match fee]} (group-by :type txs)]
                    (assoc (merge-match match)
                      :fee (apply dissoc (first fee) common-match-keys))))))
       (sort-by :time)))
