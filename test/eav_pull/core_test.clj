(ns eav-pull.core-test
  (:require [clojure.test :refer [deftest use-fixtures testing is]]
            [datomic.api :as d]
            [eav-pull.core :as eav]
            [datascript.core :as ds]))

(def tx-schema
  [{:db/ident       :address/id
    :db/valueType   :db.type/uuid
    :db/unique      :db.unique/identity
    :db/cardinality :db.cardinality/one}
   {:db/ident       :address/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident       :user/id
    :db/valueType   :db.type/uuid
    :db/unique      :db.unique/identity
    :db/cardinality :db.cardinality/one}
   {:db/ident       :user/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident       :user/addresses
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many}
   {:db/ident       :user/friends
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many}])

(deftest datomic-integration
  (let [db-uri (doto (str "datomic:mem://" (d/squuid))
                 (d/create-database))
        conn (d/connect db-uri)
        rj {:db/id        (d/tempid :db.part/user)
            :address/id   (d/squuid)
            :address/name "rj"}
        bh {:db/id        (d/tempid :db.part/user)
            :address/id   (d/squuid)
            :address/name "bh"}
        sp {:db/id        (d/tempid :db.part/user)
            :address/id   (d/squuid)
            :address/name "sp"}
        alice {:db/id          (d/tempid :db.part/user)
               :user/id        (d/squuid)
               :user/name      "Alice"
               :user/addresses (map :db/id [bh sp])}
        jack {:db/id     (d/tempid :db.part/user)
              :user/id   (d/squuid)
              :user/name "Jack"}
        bob {:db/id          (d/tempid :db.part/user)
             :user/id        (d/squuid)
             :user/name      "Bob"
             :user/friends   (map :db/id [jack])
             :user/addresses (map :db/id [rj sp])}
        tx-data [alice bob bh rj sp jack]
        {:keys [db-after tempids]} (reduce (fn [{:keys [db-after]} tx-data]
                                             (d/with db-after tx-data))
                                           {:db-after (d/db conn)}
                                           [tx-schema tx-data])
        db-id #(d/resolve-tempid db-after tempids (:db/id %))
        [id-alice id-bob id-bh id-rj id-sp id-jack] (map db-id tx-data)
        query-from-pattern (eav/->query
                             '{::eav/find    ?e
                               ::eav/pattern [:user/id
                                              :user/name
                                              {:user/friends [:user/name]}
                                              {:user/addresses [:address/name]}]
                               ::eav/where   [[?e :user/id]]})]
    (testing
      "simple"
      (is (= (set (d/q query-from-pattern db-after))
             #{[id-alice :user/addresses id-bh]
               [id-alice :user/addresses id-sp]
               [id-alice :user/id (:user/id alice)]
               [id-alice :user/name (:user/name alice)]
               [id-bob :user/addresses id-rj]
               [id-bob :user/addresses id-sp]
               [id-bob :user/friends id-jack]
               [id-bob :user/id (:user/id bob)]
               [id-bob :user/name (:user/name bob)]
               [id-jack :user/id (:user/id jack)]
               [id-jack :user/name (:user/name jack)]
               [id-rj :address/name "rj"]
               [id-sp :address/name "sp"]
               [id-bh :address/name "bh"]})))))

(deftest datascript-integration
  (let [conn (ds/create-conn {:user/friends   {:db/cardinality :db.cardinality/many
                                               :db/valueType   :db.type/ref}
                              :user/id        {:db/unique :db.unique/identity}
                              :user/addresses {:db/cardinality :db.cardinality/many
                                               :db/valueType   :db.type/ref}})
        rj {:db/id        (ds/tempid :db.part/user)
            :address/id   (ds/squuid)
            :address/name "rj"}
        bh {:db/id        (ds/tempid :db.part/user)
            :address/id   (ds/squuid)
            :address/name "bh"}
        sp {:db/id        (ds/tempid :db.part/user)
            :address/id   (ds/squuid)
            :address/name "sp"}
        alice {:db/id          (ds/tempid :db.part/user)
               :user/id        (ds/squuid)
               :user/name      "Alice"
               :user/addresses (map :db/id [bh sp])}
        jack {:db/id     (ds/tempid :db.part/user)
              :user/id   (ds/squuid)
              :user/name "Jack"}
        bob {:db/id          (ds/tempid :db.part/user)
             :user/id        (ds/squuid)
             :user/name      "Bob"
             :user/friends   (map :db/id [jack])
             :user/addresses (map :db/id [rj sp])}
        tx-data [alice bob bh rj sp jack]
        {:keys [db-after tempids]} (reduce (fn [{:keys [db-after]} tx-data]
                                             (ds/with db-after tx-data))
                                           {:db-after (ds/db conn)}
                                           [tx-schema tx-data])
        db-id #(ds/resolve-tempid db-after tempids (:db/id %))
        [id-alice id-bob id-bh id-rj id-sp id-jack] (map db-id tx-data)
        query-from-pattern (eav/->query
                             '{::eav/find    ?e
                               ::eav/pattern [:user/id
                                              :user/name
                                              {:user/friends [:user/name]}
                                              {:user/addresses [:address/name]}]
                               ::eav/where   [[?e :user/id]]})]
    (testing
      "simple"
      (is (= (set (ds/q query-from-pattern db-after))
             #{[id-alice :user/addresses id-bh]
               [id-alice :user/addresses id-sp]
               [id-alice :user/id (:user/id alice)]
               [id-alice :user/name (:user/name alice)]
               [id-bob :user/addresses id-rj]
               [id-bob :user/addresses id-sp]
               [id-bob :user/friends id-jack]
               [id-bob :user/id (:user/id bob)]
               [id-bob :user/name (:user/name bob)]
               [id-jack :user/id (:user/id jack)]
               [id-jack :user/name (:user/name jack)]
               [id-rj :address/name "rj"]
               [id-sp :address/name "sp"]
               [id-bh :address/name "bh"]})))))
