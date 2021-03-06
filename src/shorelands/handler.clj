(ns shorelands.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [shorelands.layout :refer [error-page]]
            [shorelands.routes.home :refer [home-routes]]
            [shorelands.services.user.core :refer [user-service]]
            [shorelands.middleware :as middleware]
            [shorelands.db.core :as db]
            [compojure.route :as route]
            [compojure.api.sweet :refer [defapi swagger-ui swagger-docs]]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.3rd-party.rotor :as rotor]
            [selmer.parser :as parser]
            [environ.core :refer [env]]))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as tomcat
   put any initialization code here"
  []

  (timbre/merge-config!
    {:level     (if (env :dev) :trace :info)
     :appenders {:rotor (rotor/rotor-appender
                          {:path "shorelands.log"
                           :max-size (* 512 1024)
                           :backlog 10})}})

  (if (env :dev) (parser/cache-off!))
  (db/start)
  (timbre/info (str
                 "\n-=[shorelands started successfully"
                 (when (env :dev) " using the development profile")
                 "]=-")))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (timbre/info "shorelands is shutting down...")
  (db/stop)
  (timbre/info "shutdown complete!"))

(defapi service-apis
  (swagger-ui "/swagger-ui")
  (swagger-docs
    {:info {:title "Shorelands Apis"
            :version "0.0.1"}
     :tags [{:name "User"   :description "crud endpoints"}]})
  user-service)

(def app-routes
  (routes
    (var service-apis)
    (wrap-routes #'home-routes middleware/wrap-csrf)
    (route/resources "/")
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))

(def app (middleware/wrap-base #'app-routes))
