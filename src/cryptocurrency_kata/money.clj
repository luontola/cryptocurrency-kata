(ns cryptocurrency-kata.money)

(defn sum [a b]
  (assert (= (:currency a) (:currency b))
          [a b])
  {:amount (+ (:amount a) (:amount b))
   :currency (:currency a)})

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
       (let [coin (first coins)
             coin-original-value (:amount (:original-value coin))
             coin-remaining-amount (+ (:amount coin) (:amount wanted)) ;; TODO cap to zero
             coin-taken-amount (- (:amount coin) coin-remaining-amount)
             coin-remaining-value (* coin-original-value (/ coin-remaining-amount (:amount coin)))
             coin-taken-value (- coin-original-value coin-remaining-value)
             taken (-> coin
                       (assoc :amount coin-taken-amount)
                       (assoc-in [:original-value :amount] coin-taken-value))
             remaining (-> coin
                           (assoc :amount coin-remaining-amount)
                           (assoc-in [:original-value :amount] coin-remaining-value))]

         ;;(println "----------")
         ;;(prn 'coin coin)
         ;;(prn 'wanted wanted)
         ;;(prn 'taken taken)
         ;;(prn 'remaining remaining)
         ;;(prn 'coin-taken-amount coin-taken-amount)
         ;;(prn 'coin-remaining-amount coin-remaining-amount)
         ;;(prn 'coin-taken-value coin-taken-value)
         ;;(prn 'coin-remaining-value coin-remaining-value)

         (if (pos? coin-remaining-amount)
           (take-coins (concat [remaining] (rest coins))
                       (assoc wanted :amount 0M)
                       [taken])
           (take-coins (rest coins)
                       (assoc wanted :amount 0M)
                       [coin])))))))
