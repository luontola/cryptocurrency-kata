(ns cryptocurrency-kata.core-test
  (:require [clojure.test :refer :all]
            [cryptocurrency-kata.core :as core]))

(set! *warn-on-reflection* true)

(deftest test-group-trades
  (testing "groups the source and target accounts"
    (is (= [{:type :trade
             :time "2018-02-04T21:45:51.354Z"
             :trade-id 11311696
             :order-id "37f1a4bd-4f87-43a5-9b80-641598d60e54"
             :source {:amount -64.3206146174000000M
                      :balance 0.0000511356000000M
                      :currency :EUR}
             :target {:amount 0.0098621300000000M
                      :balance 0.0098621300000000M
                      :currency :BTC}}
            {:type :trade
             :time "2018-02-04T21:48:48.944Z"
             :trade-id 11311817
             :order-id "780ce36b-bc09-4534-869b-330394800769"
             :source {:amount -0.0098621300000000M
                      :balance 0.0000000000000000M
                      :currency :BTC}
             :target {:amount 64.3774204862000000M
                      :balance 64.3774716218000000M
                      :currency :EUR}}]
           (core/group-trades
            [{:type :match
              :time "2018-02-04T21:45:51.354Z"
              :amount -64.3206146174000000M
              :balance 0.0000511356000000M
              :currency :EUR
              :trade-id 11311696
              :order-id "37f1a4bd-4f87-43a5-9b80-641598d60e54"}
             {:type :match
              :time "2018-02-04T21:45:51.354Z"
              :amount 0.0098621300000000M
              :balance 0.0098621300000000M
              :currency :BTC
              :trade-id 11311696
              :order-id "37f1a4bd-4f87-43a5-9b80-641598d60e54"}
             {:type :match
              :time "2018-02-04T21:48:48.944Z"
              :amount 64.3774204862000000M
              :balance 64.3774716218000000M
              :currency :EUR
              :trade-id 11311817
              :order-id "780ce36b-bc09-4534-869b-330394800769"}
             {:type :match
              :time "2018-02-04T21:48:48.944Z"
              :amount -0.0098621300000000M
              :balance 0.0000000000000000M
              :currency :BTC
              :trade-id 11311817
              :order-id "780ce36b-bc09-4534-869b-330394800769"}]))))

  (testing "groups fees"
    (is (= [{:type :trade
             :time "2018-03-24T10:27:32.994Z"
             :trade-id 13408287
             :order-id "2174113b-8435-4d61-82f1-018a7fc7e821"
             :source {:amount -0.0100000000000000M
                      :balance 0.0000000000000000M
                      :currency :BTC}
             :target {:amount 70.7000000000000000M
                      :balance 1106.7937284459715000M
                      :currency :EUR}
             :fee {:amount -0.1767500000000000M
                   :balance 1106.6169784459715000M
                   :currency :EUR}}]
           (core/group-trades
            [{:type :match
              :time "2018-03-24T10:27:32.994Z"
              :amount -0.0100000000000000M
              :balance 0.0000000000000000M
              :currency :BTC
              :trade-id 13408287
              :order-id "2174113b-8435-4d61-82f1-018a7fc7e821"}
             {:type :match
              :time "2018-03-24T10:27:32.994Z"
              :amount 70.7000000000000000M
              :balance 1106.7937284459715000M
              :currency :EUR
              :trade-id 13408287
              :order-id "2174113b-8435-4d61-82f1-018a7fc7e821"}
             {:type :fee
              :time "2018-03-24T10:27:32.994Z"
              :amount -0.1767500000000000M
              :balance 1106.6169784459715000M
              :currency :EUR
              :trade-id 13408287
              :order-id "2174113b-8435-4d61-82f1-018a7fc7e821"}]))))

  (testing "skips deposits"
    (is (= [{:type :deposit
             :time "2018-02-04T14:48:06.142Z"
             :amount 0.0099876500000000M
             :balance 0.0099876500000000M
             :currency :BTC
             :transfer-id "8afb99ca-b8c3-4405-b4de-30bf2e7a0c86"}
            {:type :deposit
             :time "2018-02-08T20:31:08.148Z"
             :amount 20.0000000000000000M
             :balance 20.0097755507487500M
             :currency :EUR
             :transfer-id "32b4cbea-230a-492d-a2dd-4e4c2be5a7a6"}]
           (core/group-trades
            [{:type :deposit
              :time "2018-02-04T14:48:06.142Z"
              :amount 0.0099876500000000M
              :balance 0.0099876500000000M
              :currency :BTC
              :transfer-id "8afb99ca-b8c3-4405-b4de-30bf2e7a0c86"}
             {:type :deposit
              :time "2018-02-08T20:31:08.148Z"
              :amount 20.0000000000000000M
              :balance 20.0097755507487500M
              :currency :EUR
              :transfer-id "32b4cbea-230a-492d-a2dd-4e4c2be5a7a6"}])))))

(deftest test-deposit
  (testing "deposit fiat money, doesn't track individual coins"
    (is (= {:EUR {:balance 50.0000000000000000M
                  :coins [{:amount 50.0000000000000000M
                           :currency :EUR}]}}
           (reduce core/accounts-view nil
                   [{:type :deposit
                     :time "2018-02-08T20:31:08.148Z"
                     :amount 20.0000000000000000M
                     :balance 20.0000000000000000M
                     :currency :EUR
                     :transfer-id "32b4cbea-230a-492d-a2dd-4e4c2be5a7a6"}
                    {:type :deposit
                     :time "2018-02-08T20:31:08.148Z"
                     :amount 30.0000000000000000M
                     :balance 50.0000000000000000M
                     :currency :EUR
                     :transfer-id "32b4cbea-230a-492d-a2dd-4e4c2be5a7a6"}]))))

  (testing "deposit crypto coins, keeps track of individual coins"
    (is (= {:BTC {:balance 0.3000000000000000M
                  :coins [{:amount 0.1000000000000000M
                           :currency :BTC}
                          {:amount 0.2000000000000000M
                           :currency :BTC}]}}
           (reduce core/accounts-view nil
                   [{:type :deposit
                     :time "2018-02-04T14:48:06.142Z"
                     :amount 0.1000000000000000M
                     :balance 0.1000000000000000M
                     :currency :BTC
                     :transfer-id "8afb99ca-b8c3-4405-b4de-30bf2e7a0c86"}
                    {:type :deposit
                     :time "2018-02-04T14:48:06.142Z"
                     :amount 0.2000000000000000M
                     :balance 0.3000000000000000M
                     :currency :BTC
                     :transfer-id "8afb99ca-b8c3-4405-b4de-30bf2e7a0c86"}]))))

  (testing "balance sanity check"
    (is (thrown? AssertionError
                 (reduce core/accounts-view nil
                         [{:type :deposit
                           :time "2018-02-08T20:31:08.148Z"
                           :amount 20.0000000000000000M
                           :balance 20.1000000000000000M
                           :currency :EUR
                           :transfer-id "32b4cbea-230a-492d-a2dd-4e4c2be5a7a6"}])))))

(deftest test-trade
  (testing "trade fiat money to crypto coins"
    (let [accounts (reduce core/accounts-view nil
                           [{:type :deposit
                             :time "2018-02-08T20:31:08.148Z"
                             :amount 20.0000000000000000M
                             :balance 20.0000000000000000M
                             :currency :EUR
                             :transfer-id "32b4cbea-230a-492d-a2dd-4e4c2be5a7a6"}])]
      (is (= {:EUR {:balance 5.0000000000000000M
                    :coins [{:amount 5.0000000000000000M
                             :currency :EUR}]}
              :BTC {:balance 0.0100000000000000M
                    :coins [{:amount 0.0100000000000000M
                             :currency :BTC
                             :original-value {:amount 15.0000000000000000M
                                              :currency :EUR}}]}}
             (reduce core/accounts-view accounts
                     [{:type :trade
                       :time "2018-02-04T21:45:51.354Z"
                       :trade-id 11311696
                       :order-id "37f1a4bd-4f87-43a5-9b80-641598d60e54"
                       :source {:amount -15.0000000000000000M
                                :balance 5.0000000000000000M
                                :currency :EUR}
                       :target {:amount 0.0100000000000000M
                                :balance 0.0100000000000000M
                                :currency :BTC}}])))))


  (testing "trade crypto coins to fiat money,"
    (let [accounts (reduce core/accounts-view nil
                           [{:type :deposit
                             :time "2018-02-08T20:31:08.148Z"
                             :amount 50.0000000000000000M
                             :balance 50.0000000000000000M
                             :currency :EUR
                             :transfer-id "32b4cbea-230a-492d-a2dd-4e4c2be5a7a6"}
                            {:type :trade
                             :time "2018-02-04T21:45:51.354Z"
                             :trade-id 11311696
                             :order-id "37f1a4bd-4f87-43a5-9b80-641598d60e54"
                             :source {:amount -15.0000000000000000M
                                      :balance 35.0000000000000000M
                                      :currency :EUR}
                             :target {:amount 0.0100000000000000M
                                      :balance 0.0100000000000000M
                                      :currency :BTC}}
                            {:type :trade
                             :time "2018-02-04T21:45:51.354Z"
                             :trade-id 11311696
                             :order-id "37f1a4bd-4f87-43a5-9b80-641598d60e54"
                             :source {:amount -30.0000000000000000M
                                      :balance 5.0000000000000000M
                                      :currency :EUR}
                             :target {:amount 0.0100000000000000M
                                      :balance 0.0200000000000000M
                                      :currency :BTC}}])]

      (testing "whole coin"
        (is (= {:EUR {:balance 65.0000000000000000M
                      :coins [{:amount 65.0000000000000000M, :currency :EUR}]}
                :BTC {:balance 0.0100000000000000M
                      :coins [{:amount 0.0100000000000000M
                               :currency :BTC
                               :original-value {:amount 30.0000000000000000M
                                                :currency :EUR}}]}}
               (reduce core/accounts-view accounts
                       [{:type :trade
                         :time "2018-02-04T21:45:51.354Z"
                         :trade-id 11311696
                         :order-id "37f1a4bd-4f87-43a5-9b80-641598d60e54"
                         :source {:amount -0.0100000000000000M
                                  :balance 0.0100000000000000M
                                  :currency :BTC}
                         :target {:amount 60.0000000000000000M
                                  :balance 65.0000000000000000M
                                  :currency :EUR}}]))))
      (testing "partial coin"
        (is (= {:EUR {:balance 80.0000000000000000M
                      :coins [{:amount 80.0000000000000000M, :currency :EUR}]}
                :BTC {:balance 0.0125000000000000M
                      :coins [{:amount 0.0025000000000000M
                               :currency :BTC
                               :original-value {:amount 3.7500000000000000M
                                                :currency :EUR}}
                              {:amount 0.0100000000000000M
                               :currency :BTC
                               :original-value {:amount 30.0000000000000000M
                                                :currency :EUR}}]}}
               (reduce core/accounts-view accounts
                       [{:type :trade
                         :time "2018-02-04T21:45:51.354Z"
                         :trade-id 11311696
                         :order-id "37f1a4bd-4f87-43a5-9b80-641598d60e54"
                         :source {:amount -0.0075000000000000M
                                  :balance 0.0125000000000000M
                                  :currency :BTC}
                         :target {:amount 75.0000000000000000M
                                  :balance 80.0000000000000000M
                                  :currency :EUR}}]))))
      (testing "multiple coins"
        (is (= {:EUR {:balance 110.0000000000000000M
                      :coins [{:amount 110.0000000000000000M, :currency :EUR}]}
                :BTC {:balance 0.0025000000000000M
                      :coins [{:amount 0.0025000000000000M
                               :currency :BTC
                               :original-value {:amount 7.5000000000000000M
                                                :currency :EUR}}]}}
               (reduce core/accounts-view accounts
                       [{:type :trade
                         :time "2018-02-04T21:45:51.354Z"
                         :trade-id 11311696
                         :order-id "37f1a4bd-4f87-43a5-9b80-641598d60e54"
                         :source {:amount -0.0175000000000000M
                                  :balance 0.0025000000000000M
                                  :currency :BTC}
                         :target {:amount 105.0000000000000000M
                                  :balance 110.0000000000000000M
                                  :currency :EUR}}]))))))

  #_(testing "trade crypto coins to crypto coins,"
      (let [accounts {:EUR {:balance 5.0000000000000000M}
                      :BTC {:balance 0.0200000000000000M
                            :coins [{:amount 0.0100000000000000M
                                     :currency :BTC
                                     :original-value {:amount 15.0000000000000000M
                                                      :currency :EUR}}
                                    {:amount 0.0100000000000000M
                                     :currency :BTC
                                     :original-value {:amount 30.0000000000000000M
                                                      :currency :EUR}}]}}]
        (testing "whole coin"
          (is (= {:EUR {:balance 5.0000000000000000M}
                  :BTC {:balance 0.0100000000000000M
                        :coins [{:amount 0.0100000000000000M
                                 :currency :BTC
                                 :original-value {:amount 30.0000000000000000M
                                                  :currency :EUR}}]}
                  :ETH {:balance 1.0000000000000000M
                        :coins [{:amount 1.0000000000000000M
                                 :currency :ETH
                                 :original-value {:amount 15.0000000000000000M
                                                  :currency :EUR}}]}}
                 (reduce core/accounts-view accounts
                         [{:type :trade
                           :time "2018-02-04T21:45:51.354Z"
                           :trade-id 11311696
                           :order-id "37f1a4bd-4f87-43a5-9b80-641598d60e54"
                           :source {:amount -0.0100000000000000M
                                    :balance 0.0100000000000000M
                                    :currency :BTC}
                           :target {:amount 1.0000000000000000M
                                    :balance 1.0000000000000000M
                                    :currency :ETH}}])))))))

;; TODO: trade crypto coins to crypto coins
;; TODO: calculate profits from trades
;; TODO: calculate losses from trades
;; TODO: calculate income from mining
