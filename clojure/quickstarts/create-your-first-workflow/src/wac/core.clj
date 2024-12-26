(ns wac.core
  (:require [io.orkes.workflow-resource :as wr]
            [io.orkes.sdk :as sdk]
            [io.orkes.metadata :as metadata])
  (:gen-class))

(def options
  {:app-key "_CHANGE_ME_"
   :app-secret "_CHANGE_ME_"
   :url "https://developer.orkescloud.com/api/"})


(defn create-tasks
  []
  (vector (sdk/http-task "get-user_ref" {:uri "https://randomuser.me/api/"})
          (sdk/switch-task "user-criteria_ref" "${get-user_ref.output.response.body.results[0].location.country}"
                           {"United States" [(sdk/simple-task "simple_ref"
                                                              "helloWorld"
                                                              {"user"
                                                               "${get-user_ref.output.response.body.results[0].name.first}"})]}
                           [])))

(defn create-workflow
  [tasks]
  (merge (sdk/workflow "myFirstWorkflow" tasks)))

(def workflow-request {:name "myFirstWorkflow"
                       :version 1})

(defn -main
  []
  (metadata/register-workflow-def options (-> (create-tasks) (create-workflow)) true)
  (let [workflow-id (wr/start-workflow options workflow-request)]
   (println "Started workflow:" workflow-id)))