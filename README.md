# eav-pull

Get data from db as `eav` tuples

## Usage

lein/boot: `[eav-pull "0.1.0"]`

tools-deps: `eav-pull {:mvn/version "0.1.0"}`

```clojure
(require '[eav-pull.core :as eav])
;; should work in datomic client too.
;; will work with datascript when 'or-join' is available
(require '[datomic.api :as d])

;; tradicional pull/query:
(d/q '[:find [(pull ?e [:user/name {:user/friends [:user/name]}]) ...]
       :where [?e :user/id]] db)
;; => [{:user/name "Alice"
;;      :user/friends [{:user/name "Bob"}
;;                     {:user/name "Jack"}]}
;;     {:user/name "Bob"
;;      :user/friends [{:user/name "Jack"}]}
;;     {:user/name "Jack"}]
;; eav query:
(d/q (eav/->query '{::eav/find ?e
                    ::eav/pattern [:user/name
                                   {:user/friends [:user/name]}]
                    ::eav/where [[?e :user/id]]}) db)
;; => #{[1 :user/name "Alice"]
;;      [2 :user/name "Bob"]
;;      [3 :user/name "Jack"]
;;      [1 :user/friends 2]
;;      [1 :user/friends 3]
;;      [2 :user/friends 3]}

```
