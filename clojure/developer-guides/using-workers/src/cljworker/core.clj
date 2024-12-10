(ns cljworker.core
  (:require [io.orkes.taskrunner :as tr]))

(def options
  {:app-key "_CHANGE_ME_"
   :app-secret "_CHANGE_ME_"
   :url "https://developer.orkescloud.com/api/"})

(def worker
  {:name "myTask",
   :execute (fn [d]
              (let [name (get-in d [:inputData :name])]
                {:status  "COMPLETED"
                 :outputData {"message" (str "hello "  name)}}))})

(defn -main
  [& args]
  ;; Create and run the task executor
  (tr/runner-executer-for-workers options [worker])
  ;; Keep the process running
  (loop []
    (Thread/sleep 1000)
    (recur)))
