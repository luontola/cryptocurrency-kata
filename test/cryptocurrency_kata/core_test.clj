(ns cryptocurrency-kata.core-test
  (:require [clojure.test :refer :all]
            [cryptocurrency-kata.core :as core]))

(deftest test-group-by-trade
  (is (= [{:from {:type :match
                  :time "2018-02-04T21:45:51.354Z"
                  :amount -64.3206146174000000M
                  :balance 0.0000511356000000M
                  :currency :EUR
                  :trade-id 11311696
                  :order-id "37f1a4bd-4f87-43a5-9b80-641598d60e54"}
           :to {:type :match
                :time "2018-02-04T21:45:51.354Z"
                :amount 0.0098621300000000M
                :balance 0.0098621300000000M
                :currency :BTC
                :trade-id 11311696
                :order-id "37f1a4bd-4f87-43a5-9b80-641598d60e54"}}
          {:from {:type :match
                  :time "2018-02-04T21:48:48.944Z"
                  :amount -0.0098621300000000M
                  :balance 0.0000000000000000M
                  :currency :BTC
                  :trade-id 11311817
                  :order-id "780ce36b-bc09-4534-869b-330394800769"}
           :to {:type :match
                :time "2018-02-04T21:48:48.944Z"
                :amount 64.3774204862000000M
                :balance 64.3774716218000000M
                :currency :EUR
                :trade-id 11311817
                :order-id "780ce36b-bc09-4534-869b-330394800769"}}]
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
