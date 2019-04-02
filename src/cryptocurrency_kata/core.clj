(ns cryptocurrency-kata.core
  (:require [cryptocurrency-kata.money :as money]
            [medley.core :refer [map-vals]]))

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
  (update account :coins (fn [coins]
                           (if (not (empty? coins))
                             [(reduce money/sum coins)]
                             []))))

(defn- balance-check [accounts & txs]
  (doseq [tx (->> (group-by :currency txs)
                  (map-vals last) ; if there are fees, it's last transaction to the same account
                  (vals))]
    (let [account (get accounts (:currency tx))]
      (assert (= (:balance tx) (:balance account))
              {:tx tx :account account})))
  accounts)

(defn- update-balance [account]
  (let [balance (if (not (empty? (:coins account)))
                  (:amount (reduce money/sum (:coins account)))
                  0M)
        currency (:currency (first (:coins account)))]
    (cond-> (assoc account :balance balance)
      (fiat-money? currency) (merge-coins))))

(defn- deposit [accounts tx]
  (assert (pos? (:amount tx))
          {:money tx})
  (let [account (get accounts (:currency tx))
        coins [(select-keys tx [:amount :currency :original-value])]]
    (-> accounts
        (assoc (:currency tx) (-> account
                                  (update :coins concat coins)
                                  (update-balance)))
        (balance-check tx))))

(defn- set-original-value [coin]
  (if (fiat-money? (:currency coin))
    (assoc coin :original-value coin)
    coin))

(defn- take-coins [accounts source-tx]
  (assert (neg? (:amount source-tx)) {:tx source-tx})
  (let [account (get accounts (:currency source-tx))
        {:keys [taken remaining]} (money/take-coins (:coins account) source-tx)
        accounts (assoc accounts (:currency source-tx) (-> account
                                                           (assoc :coins remaining)
                                                           (update-balance)))]
    [taken accounts]))

(defn- put-coins [accounts coins target-tx]
  (assert (pos? (:amount target-tx)) {:money target-tx})
  (let [account (get accounts (:currency target-tx))]
    (assoc accounts (:currency target-tx) (-> account
                                              (update :coins concat coins)
                                              (update-balance)))))

(defn add-fees-to-original-value [coins fees]
  (if (empty? fees)
    coins
    (money/add-to-original-value coins (reduce money/sum fees))))

(defn- trade [accounts {source-tx :source target-tx :target fee-tx :fee}]
  (let [[coins accounts] (take-coins accounts source-tx)
        [fees accounts] (if fee-tx
                          (take-coins accounts fee-tx)
                          [nil accounts])
        coins (-> (map set-original-value coins)
                  (add-fees-to-original-value fees)
                  (money/change-currency target-tx))
        accounts (put-coins accounts coins target-tx)]
    (balance-check accounts source-tx target-tx fee-tx)))

(defn accounts-view [accounts tx]
  (try
    (case (:type tx)
      :deposit (deposit accounts tx)
      :trade (trade accounts tx))
    (catch Throwable t
      (throw (ex-info "accounts-view failed to process transaction"
                      {:tx tx :accounts accounts} t)))))
