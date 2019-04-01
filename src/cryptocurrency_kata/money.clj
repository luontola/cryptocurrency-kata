(ns cryptocurrency-kata.money)

(defn sum [a b]
  (assert (= (:currency a) (:currency b))
          [a b])
  {:amount (+ (:amount a) (:amount b))
   :currency (:currency a)})

(defn- split-coin [coin split-amount]
  (assert (:amount coin) {:coin coin})
  (assert (:currency coin) {:coin coin})
  (assert (pos? split-amount) {:split-amount split-amount})
  (if (>= split-amount (:amount coin))
    [coin]
    ;; TODO: refactor the case of no original value
    (let [original-value (:amount (or (:original-value coin)
                                      coin))
          _ (assert original-value {:coin coin})
          remaining-amount (- (:amount coin) split-amount)
          remaining-value (* original-value (/ remaining-amount (:amount coin)))
          remaining (-> coin
                        (assoc :amount remaining-amount)
                        (cond->
                          (:original-value coin) (assoc-in [:original-value :amount] remaining-value)))
          taken-amount (- (:amount coin) remaining-amount)
          taken-value (- original-value remaining-value)
          taken (-> coin
                    (assoc :amount taken-amount)
                    (cond->
                      (:original-value coin) (assoc-in [:original-value :amount] taken-value)))]
      [taken remaining])))

(defn take-coins
  ([coins wanted]
   (take-coins coins wanted []))
  ([coins wanted taken-coins]
   (if (zero? (:amount wanted))
     {:taken (vec taken-coins)
      :remaining (vec coins)}
     (do
       (assert (not (empty? coins)))
       (assert (neg? (:amount wanted))
               {:wanted wanted})
       (let [[taken-coin & remaining-coins] (concat (split-coin (first coins)
                                                                (- (:amount wanted)))
                                                    (rest coins))
             still-wanted (sum wanted taken-coin)]
         (take-coins remaining-coins
                     still-wanted
                     (conj taken-coins taken-coin)))))))
