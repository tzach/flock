(ns flock.repl
  (:require [clojure.browser.repl :as repl]))

(defn ^:export connect
  "Connects the browser to the ClojureScript REPL running on the same
  machine as the server."
  []
  (let [url (.-location js/window)
        scheme (.-protocol url)
        hostname (.-hostname url)
        repl-url (str scheme "//" hostname ":9000" "/repl")]
    (repl/connect repl-url)))
