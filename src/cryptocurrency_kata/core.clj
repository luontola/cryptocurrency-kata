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

(defn merge-coins [account]
  (assoc account :coins [(reduce money/sum (:coins account))]))

(defn- add-coins [account money]
  (assert (pos? (:amount money))
          {:money money})
  (let [account (update account :coins concat [(select-keys money [:amount :currency :original-value])])]
    (if (fiat-money? (:currency money))
      (merge-coins account)
      account)))

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
      (add-coins money)
      (cond->
        (fiat-money? (:currency money)) (merge-coins))))

(defn- trade [accounts {:keys [source target]}]
  (assert (neg? (:amount source)) {:money source})
  (assert (pos? (:amount target)) {:money target})
  ;; TODO: add original value only when source is in fiat currency
  (let [target (assoc target :original-value {:amount (- (:amount source))
                                              :currency (:currency source)})
        source-account (get accounts (:currency source))
        target-account (get accounts (:currency target))
        {:keys [taken remaining]} (money/take-coins (:coins source-account) source)]
    ;; TODO: add taken coins to target account & track original value
    (-> accounts
        (assoc (:currency source) (-> source-account
                                      (update-balance source)
                                      (remove-coins source)
                                      (assoc :coins remaining)))
        (assoc (:currency target) (-> target-account
                                      (update-balance target)
                                      (add-coins target))))))

(defn accounts-view [accounts tx]
  (case (:type tx)
    :deposit (update accounts (:currency tx) deposit tx)
    :trade (trade accounts tx)))
