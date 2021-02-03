(ns ripperman.disc
  "functions related to interacting with the disc/drive/device."
  (:require [clojure.java.shell :as sh]
            [taoensso.timbre :as log]))

(defn eject-command
  []
  (let [os (System/getProperty "os.name")]
    (case os
      "Mac OS X" ["drutil" "tray" "eject"]

      (log/warnf "don't know how to eject disc for OS '%s'.  Submit a PR for your OS" os))))

(defn eject
  "eject a disc, the hard way"
  ([]
   (when-some [command (eject-command)]
     (apply sh/sh (log/spy :info "ejecting disc ..."
                           command)))))
