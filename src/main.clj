;;; run it: ./scripts/polling
(ns main
  (:use org.httpkit.server
        [clojure.tools.logging :only [info]]
        [clojure.data.json :only [json-str]]
        (compojure [core :only [defroutes GET POST]]
                   [handler :only [site]]
                   [route :only [files not-found]])))

(def ^{:const true} json-header {"Content-Type" "application/json; charset=utf-8"})

(defn- now [] (quot (System/currentTimeMillis) 1000))

(def clients
  "map channel -> sequence number"
  (atom {}))                 ; a hub, a map of client => sequence number

(let [max-id (atom 0)]
  (defn next-id []
    (swap! max-id inc)))

(defonce all-msgs (ref [{:id (next-id),            ; all message, in a list
                         :time (now)
                         :msg "this is a live chatroom, have fun",
                         :author "system"}]))

(defn- get-msgs [max-id]
  (filter #(> (-> %1 :id) max-id) @all-msgs))

(defn poll-mesg [req]
  (let [id (Integer/valueOf (-> req :params :id))
        msgs (get-msgs id)]
    (if (seq msgs)
      {:status 200 :headers json-header :body (json-str msgs)}
      (with-channel  req channel
        ;; store the channel so other threads can write into it
        ;; notice that we don't need to return anything, the body is just
        ;; executed but a default, async response with the channel is returned
        (swap! clients assoc channel id)))))

(defn on-mesg-received [req]
  (let [{:keys [msg author]} (-> req :params)
        data {:msg msg :author author :time (now) :id (next-id)}]
    (info "mesg received: " msg)
    (dosync
     (let [all-msgs* (conj @all-msgs data)
           total (count all-msgs*)]
       (if (> total 100)
         (ref-set all-msgs (vec (drop (- total 100) all-msgs*)))
         (ref-set all-msgs all-msgs*))))
    (doseq [channel (keys @clients)]
      ;; send message to client
      (send! channel {:status 200
                :headers json-header
                :body (json-str (get-msgs (@clients channel)))})
      ;; remove it, client will try to poll again
      (swap! clients dissoc channel))
    {:status 200 :headers {}}))

(defroutes chartrootm
  (GET "/poll" [] poll-mesg)
  (POST "/msg" [] on-mesg-received)
  (files "" {:root "static"})
  (not-found "<p>Page not found.</p>" ))

(defn- wrap-request-logging [handler]
  (fn [{:keys [request-method uri] :as req}]
    (let [resp (handler req)]
      (info (name request-method) (:status resp)
            (if-let [qs (:query-string req)]
              (str uri "?" qs) uri))
      resp)))

(defn -main [& args]
  (run-server (-> #'chartrootm site wrap-request-logging) {:port 9898})
  (info "server started. http://127.0.0.1:9898"))
