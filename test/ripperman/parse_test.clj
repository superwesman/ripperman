(ns ripperman.parse-test
  (:require [clojure.test :refer :all]

            [ripperman.parse :as p]))

(deftest test-samples
  (testing "testing we can parse INFO output"
    (are [pred? out]
      (pred?
        (p/info (slurp out)))

      map?
      "resources/samples/bluray-info-last-starfighter.out"

      map?
      "resources/samples/bluray-info-sound-of-music.out"

      map?
      "resources/samples/dvd-info-civil-war.out"

      map?
      "resources/samples/dvd-info-zappa.out"

      map?
      "resources/samples/no-disc-info-failed.out"))

  (testing "testing we can parse MKV output"
    (are [pred? out]
      (pred?
        (p/mkv (slurp out)))

      map?
      "resources/samples/bluray-mkv-last-starfighter.out"

      map?
      "resources/samples/dvd-mkv-output-folder-not-set-failed.out"

      map?
      "resources/samples/no-disc-mkv-failed.out")))