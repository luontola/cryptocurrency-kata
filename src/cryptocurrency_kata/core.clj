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

(defn merge-coins [account]
  (assoc account :coins [(reduce money/sum (:coins account))]))

(defn- update-balance [account tx]
  (let [balance (:amount (reduce money/sum (:coins account)))]
    (assert (= balance (:balance tx))
            {:tx tx :account account})
    (cond-> (assoc account :balance balance)
      (fiat-money? (:currency tx)) (merge-coins))))

(defn- deposit [account tx]
  (assert (pos? (:amount tx))
          {:money tx})
  (-> account
      (update :coins concat [(select-keys tx [:amount :currency :original-value])])
      (update-balance tx)))

(defn- set-original-value [coin]
  (if (fiat-money? (:currency coin))
    (assoc coin :original-value coin)
    coin))

(defn- trade [accounts {source-tx :source target-tx :target}]
  (assert (neg? (:amount source-tx)) {:money source-tx})
  (assert (pos? (:amount target-tx)) {:money target-tx})
  (let [source-account (get accounts (:currency source-tx))
        target-account (get accounts (:currency target-tx))
        {:keys [taken remaining]} (money/take-coins (:coins source-account) source-tx)]
    (-> accounts
        (assoc (:currency source-tx) (-> source-account
                                         (assoc :coins remaining)
                                         (update-balance source-tx)))
        (assoc (:currency target-tx) (-> target-account
                                         (update :coins concat (-> (map set-original-value taken)
                                                                   (money/change-currency target-tx)))
                                         (update-balance target-tx))))))

(defn accounts-view [accounts tx]
  (try
    (case (:type tx)
      :deposit (update accounts (:currency tx) deposit tx)
      :trade (trade accounts tx))
    (catch Throwable t
      (throw (ex-info "accounts-view failed to process transaction"
                      {:tx tx :accounts accounts} t)))))
