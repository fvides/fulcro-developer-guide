(ns book.queries.parsing-trace-example
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [devcards.util.edn-renderer :refer [html-edn]]
            [cljs.reader :as r]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [com.fulcrologic.fulcro.dom :as dom]))

(defn tracer-path
  "The assoc-in path of a field on the ParsingTracer in app state."
  [field] [:widget/by-id :tracer field])

(defn tracing-reader
  "Creates a parser read handler that can record the trace of the parse into the given state-atom at the proper
   tracer path."
  [state-atom]
  (fn [env k params]
    (swap! state-atom update-in (tracer-path :trace)
      conj {:env          (assoc env :parser :function-elided)
            :dispatch-key k
            :params       params})))

(defmutation record-parsing-trace
  "Mutation: Run and record the trace of a query."
  [{:keys [query]}]
  (action [{:keys [state]}]
    (let [parser (comp/parser {:read (tracing-reader state)})] ; make a parser that records calls to read
      (try
        (swap! state assoc-in (tracer-path :trace) [])      ; clear the last trace
        (swap! state assoc-in (tracer-path :error) nil)     ; clear the last error
        (parser {} (r/read-string query))                   ; Record the trace
        (catch js/Error e (swap! state assoc-in (tracer-path :error) e)))))) ; Record and exceptions

(defsc ParsingTracer [this {:keys [trace error query result]}]
  {:query         [:trace :error :query :result]
   :ident         (fn [] [:widget/by-id :tracer])
   :initial-state {:trace [] :error nil :query ""}}
  (dom/div
    (when error
      (dom/div (str error)))
    (dom/input {:type     "text"
                :value    query
                :onChange #(m/set-string! this :query :event %)})
    (dom/button {:onClick #(comp/transact! this `[(record-parsing-trace ~{:query query})])} "Run Parser")
    (dom/h4 "Parsing Trace")
    (html-edn trace)))

(def ui-tracer (comp/factory ParsingTracer))

(defsc Root [this {:keys [ui/tracer]}]
  {:query         [{:ui/tracer (comp/get-query ParsingTracer)}]
   :initial-state {:ui/tracer {}}}
  (ui-tracer tracer))
