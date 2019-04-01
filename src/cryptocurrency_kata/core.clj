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
        (assoc :type :trade
               :source source
               :target target)
        (cond-> fee (assoc :fee fee)))))

(defn group-trades [txs]
  (let [trades (filter :trade-id txs)
        non-trades (remove :trade-id txs)]
    (->> trades
         (group-by :trade-id)
         (map (fn [[_ txs]] (merge-trade txs)))
         (concat non-trades)
         (sort-by :time))))

(def ^:private real-money? #{:EUR :USD})
(def ^:private crypto-coin? (comp not real-money?))

(defn- update-balance [account tx]
  (let [updated (update account :balance (fnil + 0) (:amount tx))]
    (assert (= (:balance tx) (:balance updated))
            {:tx tx :before account :after updated})
    updated))

(defn- add-coins [account tx]
  (let [updated (-> account
                    (update :coins concat [(select-keys tx [:amount :currency])]))]
    updated))

(defn- deposit [account tx]
  (-> account
      (update-balance tx)
      (cond->
        (crypto-coin? (:currency tx)) (add-coins tx))))

(defn accounts-view [accounts tx]
  (case (:type tx)
    :deposit (update accounts (:currency tx) deposit tx)))
