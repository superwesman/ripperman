(ns ripperman.makemkv-test
  (:require [clojure.test :refer :all]
            [ripperman.makemkv :as m]
            [clojure.string :as str]))

(deftest test-examples
  (testing "these are the example commands from the doc"
    (let [executable "makemkvcon"]
      (are [expected switches command parameters]
        (= (set (str/split expected
                           #"\s+"))
           (set (m/makemkvcon executable switches command parameters)))

        "makemkvcon mkv disc:0 all /path/to/folder"         ; changed to unix style path
        {}
        :mkv
        {:disc       0
         :titles     :all
         :output-dir "/path/to/folder"}


        "makemkvcon --robot --cache=1 info disc:9999"       ; changed -r to --robot
        {:robot true
         :cache 1}
        :info
        {:disc 9999}


        "makemkvcon backup --decrypt --cache=16 --noscan --robot --progress=-same disc:0 /path/to/folder" ; changed -r to --robot and changed to unix style path
        {:decrypt  true
         :cache    16
         :noscan   true
         :robot    true
         :progress :-same}
        :backup
        {:disc       0
         :output-dir "/path/to/folder"}

        "makemkvcon stream --upnp=1 --cache=128 --bindip=192.168.1.102 --bindport=51000 --messages=-none" ; fixed typo 'makemvcon'
        {:upnp     1
         :cache    128
         :bindip   "192.168.1.102"
         :bindport 51000
         :messages :-none}
        :stream
        {}

        ))))