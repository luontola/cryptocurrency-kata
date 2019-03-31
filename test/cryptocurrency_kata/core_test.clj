(ns cryptocurrency-kata.core-test
  (:require [clojure.test :refer :all]
            [cryptocurrency-kata.core :as core]))

(set! *warn-on-reflection* true)

(deftest test-group-by-trade
  (testing "matches the source and target accounts"
    (is (= [{:type :match
             :time "2018-02-04T21:45:51.354Z"
             :trade-id 11311696
             :order-id "37f1a4bd-4f87-43a5-9b80-641598d60e54"
             :source {:amount -64.3206146174000000M
                      :balance 0.0000511356000000M
                      :currency :EUR}
             :target {:amount 0.0098621300000000M
                      :balance 0.0098621300000000M
                      :currency :BTC}}
            {:type :match
             :time "2018-02-04T21:48:48.944Z"
             :trade-id 11311817
             :order-id "780ce36b-bc09-4534-869b-330394800769"
             :source {:amount -0.0098621300000000M
                      :balance 0.0000000000000000M
                      :currency :BTC}
             :target {:amount 64.3774204862000000M
                      :balance 64.3774716218000000M
                      :currency :EUR}}]
           (core/group-by-trade
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

  (testing "matches fees"
    (is (= [{:type :match
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
           (core/group-by-trade
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
           (core/group-by-trade
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
