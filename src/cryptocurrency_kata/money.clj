(ns cryptocurrency-kata.money
  (:import (java.math RoundingMode)))

(set! *warn-on-reflection* true)

(defn sum [a b]
  (assert (= (:currency a) (:currency b))
          [a b])
  {:amount (+ (:amount a) (:amount b))
   :currency (:currency a)})

(defn- adjust-decimal-places [^BigDecimal number ^long decimal-places]
  (.setScale number decimal-places RoundingMode/HALF_UP))

(defn- divide-in-ratio [^BigDecimal target ^BigDecimal source ^BigDecimal source-total]
  (let [decimal-places (.scale target)]
    (-> (with-precision (+ 10 decimal-places)
          (* target (/ source source-total)))
        (adjust-decimal-places decimal-places))))

(defn- split-coin [coin split-amount]
  (assert (:amount coin) {:coin coin})
  (assert (:currency coin) {:coin coin})
  (assert (pos? split-amount) {:split-amount split-amount})
  (cond
    (>= split-amount (:amount coin))
    [coin]

    (:original-value coin)
    (let [[taken remain] (split-coin (select-keys coin [:amount :currency]) split-amount)
          orig-taken-amount (divide-in-ratio (:amount (:original-value coin))
                                             (:amount taken)
                                             (:amount coin))
          [orig-taken orig-remain] (split-coin (:original-value coin) orig-taken-amount)]
      [(assoc taken :original-value orig-taken)
       (assoc remain :original-value orig-remain)])

    :else
    (let [remain-amount (- (:amount coin) split-amount)
          taken-amount (- (:amount coin) remain-amount)]
      [(assoc coin :amount taken-amount)
       (assoc coin :amount remain-amount)])))

(defn take-coins
  ([coins wanted]
   (take-coins coins wanted []))
  ([coins wanted taken-coins]
   (if (zero? (:amount wanted))
     {:taken (vec taken-coins)
      :remaining (vec coins)}
     (do
       (assert (neg? (:amount wanted))
               {:wanted wanted})
       (assert (not (empty? coins))
               {:wanted wanted :coins coins :taken-coins taken-coins})
       (let [[taken-coin & remaining-coins] (concat (split-coin (first coins)
                                                                (- (:amount wanted)))
                                                    (rest coins))
             still-wanted (sum wanted taken-coin)]
         (take-coins remaining-coins
                     still-wanted
                     (conj taken-coins taken-coin)))))))

(defn change-currency [coins target]
  ;; TODO: all must be positive
  ;; TODO: all must be same currency
  (if (empty? coins)
    nil
    (let [coins-total-amount (apply + (map :amount coins))
          coin (first coins)
          converted-coin (assoc coin :amount (divide-in-ratio (:amount target)
                                                              (:amount coin)
                                                              coins-total-amount)
                                     :currency (:currency target))
          remaining-target (assoc target :amount (- (:amount target)
                                                    (:amount converted-coin)))
          converted-coins (conj (change-currency (rest coins) remaining-target)
                                converted-coin)]
      (assert (= (:amount target) (apply + (map :amount converted-coins)))
              {:target target :coins coins :converted-coins converted-coins})
      converted-coins)))
