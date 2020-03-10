# eav-pull

Get data from db as `eav` tuples

## Usage

lein/boot: `[eav-pull "0.1.0"]`

tools-deps:
```clojure
eav-pull {:git/url "https://github.com/souenzzo/eav-pull.git"
          :sha     "494a906b32c1767ff3c71f0c1df387ac0df7a27f"}
```

```clojure
(require '[eav-pull.core :as eav])
;; should work in datomic-client and datascript too.

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
