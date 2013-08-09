(defproject flock "0.1.0-SNAPSHOT"
  :description "Flock Simuation"
  :url "https://github.com/tzach/flock"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.cemerick/piggieback "0.0.4"]
                 [compojure "1.1.5"]
                 [mondrian "0.1.1"]
                 [ring "1.1.8"]
                 [rm-hull/monet "0.1.7"]
]
  :min-lein-version "2.1.2"
  :plugins [[lein-cljsbuild "0.3.2"]
            [lein-ring "0.8.3"]
            [lein-gh-pages "0.1.0-snapshot"]]
  :hooks [leiningen.cljsbuild]
;;  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :source-paths ["src/clj"]
  :resource-paths ["resources" "target/resources"]

  :cljsbuild
  {:builds
   {:dev
    {:source-paths ["src/cljs" 
                    ;; "src/cljs-repl"
                    ]
     :compiler {:output-to "target/resources/public/js/flock_dev.js"}}

    :prod
    {:source-paths ["src/cljs"]
     :compiler {:output-to "target/resources/public/js/flock.js"
                :optimizations :advanced
                :externs ["externs/jquery-1.9.js"
                          "externs/jquery-ui.js"
                          "jquery.knob.js"]
                :pretty-print false}}}}

  :ring {:handler flock.server/app}

  :main flock.server)

;; (load-file "src/clj/flock/server.clj") (flock.server/browser-repl)
;; (in-ns 'flock.flock)
;; (flock.server/-main)

;; lunch repl
;; lein trampoline cljsbuild repl-rhino
;; lein cljsbuild auto dev
