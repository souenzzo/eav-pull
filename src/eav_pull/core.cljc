(ns eav-pull.core
  (:require [edn-query-language.core :as eql]))

#_(defn variable?
    "symbol starting with \"?\""
    [x]
    #(string/starts-with? % "?"))

#_(s/def ::find (s/and symbol?
                       variable?))

#_(s/def ::where coll?)

#_(s/def ::pattern coll?)

(defn normalize-ast
  [{:keys [children current-path]
    :or   {current-path []}}]
  (->> (concat [{:path current-path
                 :ks   (mapv :dispatch-key children)}]
               (for [{:keys [dispatch-key] :as node} children
                     :let [path (conj current-path dispatch-key)]
                     norm-el (normalize-ast (assoc node
                                              :current-path path))]
                 norm-el))
       (sort-by (comp count :path))))

(defn normalize
  [path pattern]
  (let [getkey (fn [x] (if (keyword? x) x (ffirst x)))]
    (->> (concat [{:path path
                   :ks   (mapv getkey pattern)}]
                 (for [sub pattern
                       :when (map? sub)
                       :let [path (conj path (ffirst sub))]
                       x (normalize path (val (first sub)))]
                   x))
         (sort-by (comp count :path)))))

(defn path->and
  [{:keys [path ks ?e ?a ?v ?be]}]
  (let [path-count (count path)
        down-path (if (zero? path-count)
                    `[[(~'identity ~?be) ~?e]]
                    (map (fn [k [e v]]
                           [e k v])
                         path (partition 2 1 (concat
                                               [?be]
                                               (repeatedly (dec path-count) #(gensym "?path"))
                                               [?e]))))]
    (for [k ks]
      `(~'and
         ~@down-path
         [(identity ~k) ~?a]
         ~[?e k ?v]))))

(defn ->query
  [{::keys [where find pattern]}]
  (let [paths (normalize-ast (eql/query->ast pattern))
        [?e ?a ?v ?be] `[?e# ?a# ?v# ~find]]
    `{:find  ~[?e ?a ?v]
      :where [~@where
              (~'or-join ~[?be ?e ?a ?v]
                ~@(for [path paths
                        ands (path->and (assoc path :?e ?e :?a ?a :?v ?v :?be ?be))]
                    ands))]}))

#_(s/fdef ->query
          :args (s/cat :args (s/keys :req [::find
                                           ::where
                                           ::pattern])))
