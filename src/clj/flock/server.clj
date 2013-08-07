(ns flock.server
  (:require [cljs.repl]
            [cljs.repl.browser]
            [cemerick.piggieback]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as response]))

(defroutes app
  (GET "/" [] (response/resource-response "index.html" {:root "public"}))
  (route/resources "/")
  (route/resources "/" {:root "META-INF/resources"})
  (route/not-found "Not found"))

(defn -main
  [& args]
  (jetty/run-jetty app {:port 3000 :join? false}))

(defn browser-repl
  "Starts both a web server to host static content and a Piggieback
  server for use with the ClojureScript REPL."
  []
  (-main)
  (cemerick.piggieback/cljs-repl
    :repl-env (doto (cljs.repl.browser/repl-env
                      :port 9000
                      :serve-static false)
                    cljs.repl/-setup)))
