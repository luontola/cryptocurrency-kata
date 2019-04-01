(ns cryptocurrency-kata.money-test
  (:require [clojure.test :refer :all]
            [cryptocurrency-kata.money :as money]))

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

    (testing "cannot take more than there are coins"
      (is (thrown? AssertionError
                   (money/take-coins coins {:amount -0.0300000000000000M
                                            :currency :BTC}))))

    (testing "cannot take wrong currency"
      (is (thrown? AssertionError
                   (money/take-coins coins {:amount -0.0100000000000000M
                                            :currency :ETH}))))))
