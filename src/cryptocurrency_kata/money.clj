(ns cryptocurrency-kata.money)

(defn sum [a b]
  (assert (= (:currency a) (:currency b))
          [a b])
  {:amount (+ (:amount a) (:amount b))
   :currency (:currency a)})

(defn- split-coin [coin split-amount]
  (assert (pos? split-amount)
          split-amount)
  (if (>= split-amount (:amount coin))
    [coin]
    (let [coin-original-value (:amount (:original-value coin))
          coin-remaining-amount (- (:amount coin) split-amount)
          coin-taken-amount (- (:amount coin) coin-remaining-amount)
          coin-remaining-value (* coin-original-value (/ coin-remaining-amount (:amount coin)))
          coin-taken-value (- coin-original-value coin-remaining-value)
          taken (-> coin
                    (assoc :amount coin-taken-amount)
                    (assoc-in [:original-value :amount] coin-taken-value))
          remaining (-> coin
                        (assoc :amount coin-remaining-amount)
                        (assoc-in [:original-value :amount] coin-remaining-value))]
      [taken remaining])))

(defn take-coins
  ([coins wanted]
   (take-coins coins wanted []))
  ([coins wanted taken-coins]
   (if (zero? (:amount wanted))
     {:taken taken-coins
      :remaining coins}
     (do
       (assert (neg? (:amount wanted))
               {:wanted wanted})
       (let [[taken-coin & remaining-coins] (concat (split-coin (first coins)
                                                                (- (:amount wanted)))
                                                    (rest coins))
             still-wanted (sum wanted taken-coin)]
         (take-coins remaining-coins
                     still-wanted
                     (conj taken-coins taken-coin)))))))
