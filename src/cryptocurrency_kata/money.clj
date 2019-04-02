(ns cryptocurrency-kata.money
  (:import (java.math RoundingMode)))

(set! *warn-on-reflection* true)

(defn sum
  ([a] a)
  ([a b]
   (cond
     (and a b) (do
                 (assert (= (:currency a) (:currency b))
                         [a b])
                 {:amount (+ (:amount a) (:amount b))
                  :currency (:currency a)})
     a a
     b b)))

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
  (if (empty? coins)
    nil
    (let [_ (assert (pos? (:amount target)) {:target target})
          _ (assert (every? (comp pos? :amount) coins) {:coins coins})
          _ (assert (apply = (map :currency coins)) {:coins coins})
          coins-total-amount (apply + (map :amount coins))
          coin (first coins)
          consumed-target (divide-in-ratio (:amount target)
                                           (:amount coin)
                                           coins-total-amount)
          remaining-target (assoc target :amount (- (:amount target) consumed-target))
          converted-coin (assoc coin :amount consumed-target
                                     :currency (:currency target))
          converted-coins (conj (change-currency (rest coins) remaining-target)
                                converted-coin)]
      (assert (= (:amount target) (apply + (map :amount converted-coins)))
              {:target target :coins coins :converted-coins converted-coins})
      converted-coins)))

(defn add-to-original-value [coins value-add]
  (if (empty? coins)
    nil
    (let [_ (assert (pos? (:amount value-add)) {:value-add value-add})
          _ (assert (every? (comp pos? :amount) coins) {:coins coins})
          coins-total-amount (apply + (map :amount coins))
          coin (first coins)
          consumed-value-add (divide-in-ratio (:amount value-add)
                                              (:amount coin)
                                              coins-total-amount)
          remaining-value-add (- (:amount value-add) consumed-value-add)
          enriched-coin (assoc coin :original-value (if (:original-value coin)
                                                      (sum (:original-value coin)
                                                           (assoc value-add :amount consumed-value-add))
                                                      (assoc value-add :amount consumed-value-add)))
          enriched-coins (conj (add-to-original-value (rest coins) (assoc value-add :amount remaining-value-add))
                               enriched-coin)]
      enriched-coins)))
