(ns cryptocurrency-kata.core
  (:require [cryptocurrency-kata.money :as money]))

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

(def ^:private fiat-money? #{:EUR :USD})
(def ^:private crypto-coin? (comp not fiat-money?))

(defn- update-balance [account tx]
  (let [updated (update account :balance (fnil + 0) (:amount tx))]
    (assert (= (:balance tx) (:balance updated))
            {:tx tx :before account :after updated})
    updated))

(defn- add-coins [account money]
  (assert (pos? (:amount money))
          {:money money})
  (let [updated (-> account
                    (update :coins concat [(select-keys money [:amount :currency :original-value])]))]
    updated))

(defn- remove-coins [account money]
  (assert (neg? (:amount money))
          {:money money})
  (let [{:keys [taken remaining]} (money/take-coins (:coins account) money)]
    (assoc account :coins remaining)))

(defn- deposit [account money]
  (assert (pos? (:amount money))
          {:money money})
  (-> account
      (update-balance money)
      (cond->
        (crypto-coin? (:currency money)) (add-coins money))))

(defn- withdraw [account money]
  (assert (neg? (:amount money))
          {:money money})
  (-> account
      (update-balance money)
      (cond->
        (crypto-coin? (:currency money)) (remove-coins money))))

(defn- trade [accounts tx]
  (let [{:keys [source target]} tx
        target (assoc target :original-value {:amount (- (:amount source))
                                              :currency (:currency source)})]
    (-> accounts
        (update (:currency source) withdraw source)
        (update (:currency target) deposit target))))

(defn accounts-view [accounts tx]
  (case (:type tx)
    :deposit (update accounts (:currency tx) deposit tx)
    :trade (trade accounts tx)))
