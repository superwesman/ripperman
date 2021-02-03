(ns ripperman.makemkv
  (:require [clojure.java.shell :as sh]
            [ripperman.parse :as p]
            [taoensso.timbre :as log])
  (:import (instaparse.gll Failure)))

(defn ->flag
  "creates a command-line flag like: --verbose"
  [option]
  (format "--%s" (name option)))

(defn boolean-flag
  "flag, if enabled"
  [option enabled?]
  (if enabled?
    [(->flag option)]
    []))

(defn ->option
  "creates a command-line option like: --file=/other/here.txt"
  [option value]
  [(format "%s=%s" (->flag option) (str value))])

(defmulti compile-switch
          (fn [switch _] switch))

(defmethod compile-switch :default
  [switch value]
  (throw (IllegalArgumentException. (format "invalid switch! switch: %s, value: %s" switch value))))

(defmethod compile-switch :cache
  [cache size]
  (->option cache size))

(defmethod compile-switch :decrypt
  [switch decrypt?]
  (boolean-flag switch decrypt?))

(defmethod compile-switch :noscan
  [switch noscan?]
  (boolean-flag switch noscan?))

(defmethod compile-switch :robot
  [switch robot?]
  (boolean-flag switch robot?))

(defmethod compile-switch :decrypt
  [switch decrypt?]
  (boolean-flag switch decrypt?))

(defmethod compile-switch :progress
  [switch destination]
  (->option switch (name destination)))

(defmethod compile-switch :upnp
  [switch value]
  (->option switch value))

(defmethod compile-switch :bindip
  [switch value]
  (->option switch (name value)))

(defmethod compile-switch :bindport
  [switch value]
  (->option switch value))

(defmethod compile-switch :messages
  [switch destination]
  (->option switch (name destination)))

(defmulti compile-parameter
          (fn [parameter _] parameter))

(defmethod compile-parameter :default
  [parameter value]
  (throw (IllegalArgumentException. (format "invalid parameter! parameter: %s, value: %s" parameter value))))

(defmethod compile-parameter :disc
  [parameter disc-id]
  (format "%s:%s" (name parameter) disc-id))

(defmethod compile-parameter :output-dir
  [parameter output-dir]
  (str output-dir))

(defmethod compile-parameter :titles
  [parameter titles]
  (if (keyword? titles)
    (name titles)
    (str titles)))

(defn compile-switches
  [switches]
  (mapcat
    (partial apply compile-switch)
    switches))

(defn compile-parameters
  [parameters]
  (map
    (partial apply compile-parameter)
    parameters))

(defn makemkvcon-command
  [executable switches command parameters]
  [[executable]
   (compile-switches switches)
   [(name command)]
   (compile-parameters parameters)])

(defn command-line
  "flattens a collection of collections of arg things into a collection strings"
  [args]
  (vec
    (reduce
      (fn [x bits]
        (concat x bits))
      []
      args)))

(defmulti makemkvcon
          "generates command-line: makemkvcon [switches] command [parameters]"
          (fn [executable switches command parameters]
            command))

(defmethod makemkvcon :default
  [executable switches command parameters]
  (log/errorf "%s is not a valid command!" command)
  executable)

(defmethod makemkvcon :info
  [executable switches command parameters]
  (command-line (makemkvcon-command executable switches command parameters)))

(defmethod makemkvcon :mkv
  [executable switches command parameters]
  (command-line (makemkvcon-command executable switches command parameters)))

(defmethod makemkvcon :backup
  [executable switches command parameters]
  (command-line (makemkvcon-command executable switches command parameters)))

(defmethod makemkvcon :f
  [executable switches command parameters]
  (log/warnf "%s is not implemented! Feel free to submit a PR" command))

(defmethod makemkvcon :stream
  [executable switches command parameters]
  (command-line (makemkvcon-command executable switches command parameters)))

(def parse-failure?
  (partial instance? Failure))

(def command-failure?
  (complement (comp :success? :result)))

(def failed?
  (some-fn parse-failure?
           command-failure?))

(defn cause
  "cause of the failure, if it is one"
  [x]
  (cond (parse-failure? x)
        x

        (command-failure? x)
        (:result x)

        :else
        nil))

(defn execute!
  "run something"
  [result-handler command]
  (result-handler (log/spy :info "Result: "
                           (apply sh/sh (log/spy :info "Executing: "
                                                 command)))))

(defn rip!
  "rips a title from a disc"
  [{:keys [executable] :as options} title]
  (let [command (makemkvcon executable
                            {:robot true}
                            :mkv
                            (dissoc options :executable))
        handler (fn [{:keys [exit out err]}]
                  (log/infof "Exit Code: %s , Err: %s" exit err)
                  (p/mkv out))]
    (execute! handler command)))

(def memoized-execute (memoize execute!))

(defn read-info!
  "reads info from an actual disc!"
  [{:keys [executable disc]}]
  (let [command (makemkvcon executable
                            {:robot true
                             :cache 1}
                            :info
                            {:disc disc})
        handler (fn [{:keys [exit out err]}]
                  (log/infof "Out: %s" (pr-str out))
                  (log/infof "Exit Code: %s , Err: %s" exit err)
                  (p/info out))]
    (memoized-execute handler command)))
