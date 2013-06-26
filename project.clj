(defproject org.httpkit/chat-polling "1.0"
  :description "Realtime chat by utilizing http-kit's websocket support"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring/ring-core "1.1.8"]
                 [compojure "1.1.5"]
                 [org.clojure/data.json "0.2.1"]
                 [org.clojure/tools.logging "0.2.6"]
                 [ch.qos.logback/logback-classic "1.0.13"]
                 [http-kit "2.1.1"]]
  :warn-on-reflection true
  :min-lein-version "2.0.0"
  :main main
  :test-paths ["test"]
  :plugins [[lein-swank "1.4.4"]]
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"})
