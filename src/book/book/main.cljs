(ns book.main
  (:require
    [com.fulcrologic.fulcro.networking.mock-server-remote :refer [mock-http-server]]
    [book.macros :refer [defexample deftool]]
    [book.ui.d3-example :as d3-example]
    [book.ui.focus-example :as focus-example]
    [book.ui.hover-example :as hover-example]
    [book.ui.victory-example :as victory-example]
    [book.queries.union-example-1 :as union-example-1]
    ;[book.queries.union-example-2 :as union-example-2]
    ;[book.queries.parsing-trace-example :as trace]
    ;book.queries.parsing-key-trace
    ;book.queries.naive-read
    ;book.queries.simple-property-read
    ;book.queries.parsing-simple-join
    ;book.queries.parsing-recursion-one
    ;book.queries.parsing-recursion-two
    ;book.queries.parsing-parameters
    ;book.queries.dynamic-queries
    ;book.queries.dynamic-query-parameters
    ;book.queries.recursive-demo-1
    ;book.queries.recursive-demo-2
    ;book.queries.recursive-demo-3
    ;book.queries.recursive-demo-bullets
    ;book.forms.form-state-demo-1
    ;book.forms.form-state-demo-2
    ;book.forms.forms-demo-1
    ;book.forms.forms-demo-2
    ;book.forms.forms-demo-3
    ;book.forms.whole-form-logic
    ;book.forms.full-stack-forms-demo
    [book.demos.autocomplete :as autocomplete]
    book.ui-routing
    book.simple-router-1
    book.simple-router-2
    book.tree-to-db
    book.merge-component
    book.html-converter
    ;book.server.morphing-example
    ;book.demos.cascading-dropdowns
    book.demos.component-localized-css
    book.demos.localized-dom
    book.demos.dynamic-ui-routing
    book.demos.initial-app-state
    ;book.demos.legacy-load-indicators
    ;book.demos.loading-data-basics
    ;book.demos.loading-data-targeting-entities
    ;book.demos.loading-in-response-to-UI-routing
    ;book.demos.loading-indicators
    ;book.demos.paginating-large-lists-from-server
    ;book.demos.parallel-vs-sequential-loading
    ;book.demos.parent-child-ownership-relations
    ;book.demos.pre-merge.post-mutation-countdown
    ;book.demos.pre-merge.post-mutation-countdown-many
    ;book.demos.pre-merge.countdown
    ;book.demos.pre-merge.countdown-many
    ;book.demos.pre-merge.countdown-with-initial
    ;book.demos.pre-merge.countdown-initial-state
    ;book.demos.pre-merge.countdown-extracted
    ;book.demos.pre-merge.countdown-mutation
    ;book.demos.server-error-handling
    ;book.demos.server-query-security
    ;book.demos.server-return-values-as-data-driven-mutation-joins
    ;book.demos.server-targeting-return-values-into-app-state
    ;book.demos.server-return-values-manually-merging
    ;[book.server.ui-blocking-example :as ui-blocking]

    ;[fulcro.server :as server :refer [defquery-root]]
    ;[com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    ;[fulcro.client.network :as fcn]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro-css.css-injection :as css]
    [com.wsscode.pathom.connect :as pc]
    [com.wsscode.pathom.core :as p]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [book.example-1 :as ex1]))

(pc/defmutation todo-new-item [env {:keys [id list-id text]}]
  {::pc/sym    `fulcro-todomvc.api/todo-new-item
   ::pc/params [:list-id :id :text]
   ::pc/output [:item/id]}
  (log/info "Mutation ran"))

;; How to go from :person/id to that person's details
(pc/defresolver list-resolver [env params]
  {::pc/input  #{:list/id}
   ::pc/output [:list/title {:list/items [:item/id]}]}
  {:list/title "The List"
   :list/items [{:item/id 1} {:item/id 2} {:item/id 3}]})

(def my-resolvers [autocomplete/list-resolver
                   #_book.demos.cascading-dropdowns/model-resolver])

(def parser
  (p/parallel-parser
    {::p/env     {::p/reader [p/map-reader
                              pc/parallel-reader
                              pc/open-ident-reader
                              p/env-placeholder-reader]}
     ::p/mutate  pc/mutate-async
     ::p/plugins [(pc/connect-plugin {::pc/register my-resolvers})
                  (p/post-process-parser-plugin p/elide-not-found)
                  p/error-handler-plugin]}))

;(defn raise-response
;  "For om mutations, converts {'my/mutation {:result {...}}} to {'my/mutation {...}}"
;  [resp]
;  (reduce (fn [acc [k v]]
;            (if (and (symbol? k) (not (nil? (:result v))))
;              (assoc acc k (:result v))
;              (assoc acc k v)))
;    {} resp))
;
;(def parser (server/fulcro-parser))
;
;(defonce latency (atom 100))
;
;(server/defmutation set-server-latency [{:keys [delay]}]
;  (action [env]
;    (when (<= 100 delay 30000)
;      (reset! latency delay))
;    (with-meta {:server-control/delay @latency}
;      {::nodelay true})))
;
;(defquery-root :server-control
;  (value [env params]
;    {:server-control/delay @latency}))
;
;?(:cljs
;   (defrecord MockNetwork []
;     fcn/FulcroNetwork
;     (send [this edn ok error]
;       (log/info "Server received " edn)
;       (try
;         (let [resp        (raise-response (parser {} edn))
;               skip-delay? (and (map? resp) (some-> resp first second meta ::nodelay))]
;           ; simulates a network delay:
;           (if skip-delay?
;             (ok resp)
;             (js/setTimeout (fn []
;                              (log/info "Server responded with " resp)
;                              (ok resp)) @latency)))
;         (catch :default e
;           (log/error "Exception thrown during parse:" e)
;           (error {:type (type e)
;                   :data (some-> (ex-data e) :body)}))))
;     (start [this] this)))
;
;(defsc ServerControl [this {:keys [:server-control/delay ui/hidden?]}]
;  {:query         [:server-control/delay :ui/hidden?]
;   :initial-state {:ui/hidden? true}
;   :ident         (fn [] [:server-control/by-id :server])}
;  (dom/div (clj->js {:style {:position        :fixed
;                             :width           "180px"
;                             :height          "130px"
;                             :fontSize        "10px"
;                             :backgroundColor :white
;                             :zIndex          60000
;                             :opacity         1.0
;                             :padding         "3px"
;                             :border          "3px groove white"
;                             :top             0
;                             :right           (if hidden? "-179px" "-1px")}})
;    (dom/div nil "Latency: " (dom/span nil delay))
;    (dom/br nil)
;    (dom/button #js {:onClick #(comp/transact! this `[(set-server-latency {:delay ~(+ delay 500)})])} "Slower")
;    (dom/button #js {:onClick #(comp/transact! this `[(set-server-latency {:delay ~(- delay 500)})])} "Faster")
;    (dom/div #js {:onClick #(m/toggle! this :ui/hidden?)
;                  :style   #js {:color           "grey"
;                                :backgroundColor "lightgray"
;                                :padding         "5px"
;                                :paddingLeft     "10px"
;                                :fontSize        "14px"
;                                :position        :relative
;                                :opacity         1.0
;                                :transform       "rotate(-90deg) translate(12px, -100px)"}}
;      "Server Controls")))
;
;(defmutation set-server-latency [{:keys [delay]}]
;  (remote [{:keys [ast state]}]
;    (-> ast
;      (m/returning state ServerControl))))
;
;(def ui-server-control (comp/factory ServerControl))
;
;(defsc ServerControlRoot [this {:keys [ui/react-key server-control]}]
;  {:query         [:ui/react-key {:server-control (comp/get-query ServerControl)}]
;   :initial-state {:server-control {}}}
;  (dom/div #js {:key react-key}
;    (ui-server-control server-control)))
;
(defonce example-server {:remote (mock-http-server {:parser (fn [req] (parser {} req))})})
;
;#?(:cljs (deftool ServerControlRoot "server-controls"
;           :started-callback (fn [app]
;                               (df/load app :server-control ServerControl {:marker false}))
;           :networking {:remote example-server}))
;
(css/upsert-css "example-css" {:component     book.macros/ExampleRoot
                               :auto-include? false})
(defexample "Sample Example" ex1/Root "example-1")
(defexample "D3" d3-example/Root "ui-d3")
(defexample "Input Focus and React Refs/Lifecycle" focus-example/Root "focus-example")
(defexample "Drawing in a Canvas" hover-example/Root "hover-example")
(defexample "Using External React Libraries" victory-example/Root "victory-example")
(defexample "Unions to Select Type" union-example-1/Root "union-example-1")
;#?(:cljs (defexample "UI Blocking" ui-blocking/Root "ui-blocking-example" :networking book.main/example-server))
;
;;; Dynamic queries
;#?(:cljs (defexample "Dynamic Query" book.queries.dynamic-queries/Root "dynamic-queries"))
;#?(:cljs (defexample "Dyanmic Query Parameters" book.queries.dynamic-query-parameters/Root "dynamic-query-parameters"))
;
(defexample "Routing Demo" book.ui-routing/Root "ui-routing" :networking book.main/example-server)
(defexample "Simple Router" book.simple-router-1/Root "simple-router-1")
(defexample "Nested Router" book.simple-router-2/Root "simple-router-2")
(defexample "Tree to DB with Queries" book.tree-to-db/Root "tree-to-db" :networking book.main/example-server)
(defexample "Merging with a Component" book.merge-component/Root "merge-component" :networking book.main/example-server)
(defexample "HTML Converter" book.html-converter/Root "html-converter")
;
;;; Forms
;#?(:cljs (defexample "Editing Existing Data" book.forms.form-state-demo-1/Root "form-state-demo-1" :networking book.main/example-server))
;#?(:cljs (defexample "Network Interactions and Forms" book.forms.form-state-demo-2/Root "form-state-demo-2" :networking book.main/example-server))
;
;#?(:cljs (defexample "Basic Form" book.forms.forms-demo-1/Root "forms-demo-1" :networking book.main/example-server))
;#?(:cljs (defexample "Validated Form" book.forms.forms-demo-2/Root "forms-demo-2" :networking book.main/example-server))
;#?(:cljs (defexample "Validated Form" book.forms.forms-demo-3/Root "forms-demo-3" :networking book.main/example-server))
;#?(:cljs (defexample "Whole Form Logic" book.forms.whole-form-logic/Root "whole-form-logic" :networking book.main/example-server))
;#?(:cljs (defexample "Full Stack Forms" book.forms.full-stack-forms-demo/Root "full-stack-forms-demo"
;           :started-callback book.forms.full-stack-forms-demo/initialize
;           :networking book.main/example-server))
;
(defexample "Autocomplete" autocomplete/AutocompleteRoot "autocomplete-demo" :remotes book.main/example-server)
;(defexample "Cascading Dropdowns" book.demos.cascading-dropdowns/Root "cascading-dropdowns" :remotes book.main/example-server)
(defexample "Component Localized CSS" book.demos.component-localized-css/Root "component-localized-css" :remotes book.main/example-server)
(defexample "Localized DOM" book.demos.localized-dom/Root "localized-dom")
(defexample "Dynamic UI Routing" book.demos.dynamic-ui-routing/Root "dynamic-ui-routing"
  :client-did-mount book.demos.dynamic-ui-routing/application-loaded
  :remotes book.main/example-server)
;
;#?(:cljs (defexample "Loading Data Basics" book.demos.loading-data-basics/Root "loading-data-basics" :networking book.main/example-server :started-callback book.demos.loading-data-basics/initialize))
;#?(:cljs (defexample "Loading Data and Targeting Entities" book.demos.loading-data-targeting-entities/Root "loading-data-targeting-entities" :networking book.main/example-server))
;#?(:cljs (defexample "Loading In Response To UI Routing" book.demos.loading-in-response-to-UI-routing/Root "loading-in-response-to-UI-routing" :networking book.main/example-server))
;#?(:cljs (defexample "Loading Indicators" book.demos.loading-indicators/Root "loading-indicators" :networking book.main/example-server))
;#?(:cljs (defexample "Initial State" book.demos.initial-app-state/Root "initial-app-state" :networking book.main/example-server))
;#?(:cljs (defexample "Legacy Load Indicators" book.demos.legacy-load-indicators/Root "legacy-load-indicators" :networking book.main/example-server))
;#?(:cljs (defexample "Paginating Lists From Server" book.demos.paginating-large-lists-from-server/Root "paginating-large-lists-from-server"
;           :started-callback book.demos.paginating-large-lists-from-server/initialize
;           :networking book.main/example-server))
;
;#?(:cljs (defexample "Parallel vs. Sequential Loading" book.demos.parallel-vs-sequential-loading/Root "parallel-vs-sequential-loading" :networking book.main/example-server))
;#?(:cljs (defexample "Parent-Child Ownership" book.demos.parent-child-ownership-relations/Root "parent-child-ownership-relations" :networking book.main/example-server))
;
;#?(:cljs (defexample "Pre merge - using post mutations" book.demos.pre-merge.post-mutation-countdown/Root "pre-merge-postmutations" :networking book.main/example-server))
;#?(:cljs (defexample "Pre merge - using post mutations to many" book.demos.pre-merge.post-mutation-countdown-many/Root "pre-merge-postmutations-many" :networking book.main/example-server))
;#?(:cljs (defexample "Pre merge" book.demos.pre-merge.countdown/Root "postmutations-single" :networking book.main/example-server))
;#?(:cljs (defexample "Pre merge - to many" book.demos.pre-merge.countdown-many/Root "postmutations-many" :networking book.main/example-server))
;#?(:cljs (defexample "Pre merge - with initial" book.demos.pre-merge.countdown-with-initial/Root "postmutations-with-initial" :networking book.main/example-server))
;#?(:cljs (defexample "Pre merge - extracted ui" book.demos.pre-merge.countdown-extracted/Root "postmutations-extracted" :networking book.main/example-server))
;#?(:cljs (defexample "Pre merge - initial state" book.demos.pre-merge.countdown-initial-state/Root "postmutations-initial-state" :networking book.main/example-server))
;#?(:cljs (defexample "Pre merge - mutation" book.demos.pre-merge.countdown-mutation/Root "postmutations-mutation" :networking book.main/example-server))
;
;#?(:cljs (defexample "Error Handling" book.demos.server-error-handling/Root "server-error-handling"
;           :networking book.main/example-server))
;#?(:cljs (defexample "Query Security" book.demos.server-query-security/Root "server-query-security"
;           :networking book.main/example-server))
;#?(:cljs (defexample "Return Values and Mutation Joins" book.demos.server-return-values-as-data-driven-mutation-joins/Root "server-return-values-as-data-driven-mutation-joins"
;
;           :networking book.main/example-server))
;#?(:cljs (defexample "Manually Merging Server Mutation Return Values" book.demos.server-return-values-manually-merging/Root "server-return-values-manually-merging"
;           :mutation-merge book.demos.server-return-values-manually-merging/merge-return-value
;           :networking book.main/example-server))
;#?(:cljs (defexample "Targeting Mutation Return Values" book.demos.server-targeting-return-values-into-app-state/Root "server-targeting-return-values-into-app-state" :networking book.main/example-server))
