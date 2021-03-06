(ns cryptocurrency-kata.money-test
  (:require [clojure.test :refer :all]
            [cryptocurrency-kata.money :as money]))

(set! *warn-on-reflection* true)

(deftest test-sum
  (testing "sum positive amounts"
    (is (= {:amount 30.00M
            :currency :EUR}
           (money/sum {:amount 10.00M
                       :currency :EUR}
                      {:amount 20.00M
                       :currency :EUR}))))

  (testing "subtract negative amounts"
    (is (= {:amount -10.00M
            :currency :EUR}
           (money/sum {:amount 10.00M
                       :currency :EUR}
                      {:amount -20.00M
                       :currency :EUR}))))

  (testing "subtract to zero"
    (is (= {:amount 0.00M
            :currency :EUR}
           (money/sum {:amount 10.00M
                       :currency :EUR}
                      {:amount -10.00M
                       :currency :EUR}))))

  (testing "nil"
    (is (= {:amount 10.00M
            :currency :EUR}
           (money/sum {:amount 10.00M
                       :currency :EUR}
                      nil)
           (money/sum nil
                      {:amount 10.00M
                       :currency :EUR})
           (money/sum {:amount 10.00M
                       :currency :EUR})))
    (is (= nil
           (money/sum nil nil)
           (money/sum nil))))

  (testing "cannot sum different currencies"
    (is (thrown? AssertionError
                 (money/sum {:amount 10.00M
                             :currency :EUR}
                            {:amount 20.00M
                             :currency :USD})))))

(deftest test-take-coins
  (let [coins [{:amount 0.0100000000000000M
                :currency :BTC
                :original-value {:amount 10.0000000000000000M
                                 :currency :EUR}}
               {:amount 0.0100000000000000M
                :currency :BTC
                :original-value {:amount 20.0000000000000000M
                                 :currency :EUR}}]]

    (testing "take exact amount"
      (is (= {:taken [{:amount 0.0100000000000000M
                       :currency :BTC
                       :original-value {:amount 10.0000000000000000M
                                        :currency :EUR}}]
              :remaining [{:amount 0.0100000000000000M
                           :currency :BTC
                           :original-value {:amount 20.0000000000000000M
                                            :currency :EUR}}]}
             (money/take-coins coins {:amount -0.0100000000000000M
                                      :currency :BTC}))))

    (testing "take partial coin"
      (is (= {:taken [{:amount 0.0025000000000000M
                       :currency :BTC
                       :original-value {:amount 2.5000000000000000M
                                        :currency :EUR}}]
              :remaining [{:amount 0.0075000000000000M
                           :currency :BTC
                           :original-value {:amount 7.5000000000000000M
                                            :currency :EUR}}
                          {:amount 0.0100000000000000M
                           :currency :BTC
                           :original-value {:amount 20.0000000000000000M
                                            :currency :EUR}}]}
             (money/take-coins coins {:amount -0.0025000000000000M
                                      :currency :BTC}))))

    (testing "take multiple coins"
      (is (= {:taken [{:amount 0.0100000000000000M
                       :currency :BTC
                       :original-value {:amount 10.0000000000000000M
                                        :currency :EUR}}
                      {:amount 0.0025000000000000M
                       :currency :BTC
                       :original-value {:amount 5.0000000000000000M
                                        :currency :EUR}}]
              :remaining [{:amount 0.0075000000000000M
                           :currency :BTC
                           :original-value {:amount 15.0000000000000000M
                                            :currency :EUR}}]}
             (money/take-coins coins {:amount -0.0125000000000000M
                                      :currency :BTC}))))

    (testing "take fiat money"
      (let [coins [{:amount 10.00M
                    :currency :EUR}]]
        (is (= {:taken [{:amount 7.00M
                         :currency :EUR}]
                :remaining [{:amount 3.00M
                             :currency :EUR}]}
               (money/take-coins coins {:amount -7.00M
                                        :currency :EUR})))))

    (testing "uneven split of original value"
      (let [coins [{:amount 0.0300000000000000M
                    :currency :BTC
                    :original-value {:amount 10.0000000000000000M
                                     :currency :EUR}}]]
        (is (= {:taken [{:amount 0.0100000000000000M
                         :currency :BTC
                         :original-value {:amount 3.3333333333333333M
                                          :currency :EUR}}]
                :remaining [{:amount 0.0200000000000000M
                             :currency :BTC
                             :original-value {:amount 6.6666666666666667M
                                              :currency :EUR}}]}
               (money/take-coins coins {:amount -0.0100000000000000M
                                        :currency :BTC})))))

    (testing "cannot take more than there are coins"
      (is (thrown? AssertionError
                   (money/take-coins coins {:amount -0.0300000000000000M
                                            :currency :BTC}))))

    (testing "cannot take wrong currency"
      (is (thrown? AssertionError
                   (money/take-coins coins {:amount -0.0100000000000000M
                                            :currency :ETH}))))))

(deftest test-change-currency
  (testing "one coin"
    (is (= [{:amount 20.0000000000000000M
             :currency :EUR
             :original-value {:amount 11.0000000000000000M
                              :currency :EUR}}]
           (money/change-currency [{:amount 0.0100000000000000M
                                    :currency :BTC
                                    :original-value {:amount 11.0000000000000000M
                                                     :currency :EUR}}]
                                  {:amount 20.0000000000000000M
                                   :currency :EUR}))))

  (testing "two coins, split even"
    (is (= [{:amount 10.0000000000000000M
             :currency :EUR
             :original-value {:amount 11.0000000000000000M
                              :currency :EUR}}
            {:amount 10.0000000000000000M
             :currency :EUR
             :original-value {:amount 12.0000000000000000M
                              :currency :EUR}}]
           (money/change-currency [{:amount 0.0100000000000000M
                                    :currency :BTC
                                    :original-value {:amount 11.0000000000000000M
                                                     :currency :EUR}}
                                   {:amount 0.0100000000000000M
                                    :currency :BTC
                                    :original-value {:amount 12.0000000000000000M
                                                     :currency :EUR}}]
                                  {:amount 20.0000000000000000M
                                   :currency :EUR}))))

  (testing "two coins, split according to ratio"
    (is (= [{:amount 5.0000000000000000M
             :currency :EUR
             :original-value {:amount 11.0000000000000000M
                              :currency :EUR}}
            {:amount 15.0000000000000000M
             :currency :EUR
             :original-value {:amount 12.0000000000000000M
                              :currency :EUR}}]
           (money/change-currency [{:amount 0.0100000000000000M
                                    :currency :BTC
                                    :original-value {:amount 11.0000000000000000M
                                                     :currency :EUR}}
                                   {:amount 0.0300000000000000M
                                    :currency :BTC
                                    :original-value {:amount 12.0000000000000000M
                                                     :currency :EUR}}]
                                  {:amount 20.0000000000000000M
                                   :currency :EUR}))))

  (testing "three coins, indivisible split"
    (is (= [{:amount 6.6666666666666667M
             :currency :EUR
             :original-value {:amount 11.0000000000000000M
                              :currency :EUR}}
            {:amount 6.6666666666666667M
             :currency :EUR
             :original-value {:amount 12.0000000000000000M
                              :currency :EUR}}
            {:amount 6.6666666666666666M
             :currency :EUR
             :original-value {:amount 13.0000000000000000M
                              :currency :EUR}}]
           (money/change-currency [{:amount 0.0100000000000000M
                                    :currency :BTC
                                    :original-value {:amount 11.0000000000000000M
                                                     :currency :EUR}}
                                   {:amount 0.0100000000000000M
                                    :currency :BTC
                                    :original-value {:amount 12.0000000000000000M
                                                     :currency :EUR}}
                                   {:amount 0.0100000000000000M
                                    :currency :BTC
                                    :original-value {:amount 13.0000000000000000M
                                                     :currency :EUR}}]
                                  {:amount 20.0000000000000000M
                                   :currency :EUR})))))

(deftest test-add-to-original-value
  (testing "one coin"
    (is (= [{:amount 0.0100000000000000M
             :currency :BTC
             :original-value {:amount 10.0200000000000000M
                              :currency :EUR}}]
           (money/add-to-original-value [{:amount 0.0100000000000000M
                                          :currency :BTC
                                          :original-value {:amount 10.0000000000000000M
                                                           :currency :EUR}}]
                                        {:amount 0.0200000000000000M
                                         :currency :EUR}))))

  (testing "one coin, no original value"
    (is (= [{:amount 0.0100000000000000M
             :currency :BTC
             :original-value {:amount 0.0200000000000000M
                              :currency :EUR}}]
           (money/add-to-original-value [{:amount 0.0100000000000000M
                                          :currency :BTC}]
                                        {:amount 0.0200000000000000M
                                         :currency :EUR}))))

  (testing "two coins, split even"
    (is (= [{:amount 0.0100000000000000M
             :currency :BTC
             :original-value {:amount 10.0100000000000000M
                              :currency :EUR}}
            {:amount 0.0100000000000000M
             :currency :BTC
             :original-value {:amount 10.0100000000000000M
                              :currency :EUR}}]
           (money/add-to-original-value [{:amount 0.0100000000000000M
                                          :currency :BTC
                                          :original-value {:amount 10.0000000000000000M
                                                           :currency :EUR}}
                                         {:amount 0.0100000000000000M
                                          :currency :BTC
                                          :original-value {:amount 10.0000000000000000M
                                                           :currency :EUR}}]
                                        {:amount 0.0200000000000000M
                                         :currency :EUR}))))

  (testing "two coins, split according to ratio"
    (is (= [{:amount 0.0100000000000000M
             :currency :BTC
             :original-value {:amount 10.0050000000000000M
                              :currency :EUR}}
            {:amount 0.0300000000000000M
             :currency :BTC
             :original-value {:amount 10.0150000000000000M
                              :currency :EUR}}]
           (money/add-to-original-value [{:amount 0.0100000000000000M
                                          :currency :BTC
                                          :original-value {:amount 10.0000000000000000M
                                                           :currency :EUR}}
                                         {:amount 0.0300000000000000M
                                          :currency :BTC
                                          :original-value {:amount 10.0000000000000000M
                                                           :currency :EUR}}]
                                        {:amount 0.0200000000000000M
                                         :currency :EUR}))))

  (testing "three coins, indivisible split"
    (is (= [{:amount 0.0100000000000000M
             :currency :BTC
             :original-value {:amount 10.0066666666666667M
                              :currency :EUR}}
            {:amount 0.0100000000000000M
             :currency :BTC
             :original-value {:amount 10.0066666666666667M
                              :currency :EUR}}
            {:amount 0.0100000000000000M
             :currency :BTC
             :original-value {:amount 10.0066666666666666M
                              :currency :EUR}}]
           (money/add-to-original-value [{:amount 0.0100000000000000M
                                          :currency :BTC
                                          :original-value {:amount 10.0000000000000000M
                                                           :currency :EUR}}
                                         {:amount 0.0100000000000000M
                                          :currency :BTC
                                          :original-value {:amount 10.0000000000000000M
                                                           :currency :EUR}}
                                         {:amount 0.0100000000000000M
                                          :currency :BTC
                                          :original-value {:amount 10.0000000000000000M
                                                           :currency :EUR}}]
                                        {:amount 0.0200000000000000M
                                         :currency :EUR})))))
