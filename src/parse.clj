(ns ripperman.parse
  (:require [clojure.data.csv :as csv]
            [clojure.string :as str]

            [instaparse.core :as insta]))

(def twenty-minutes (* 60 20))
(def forty-minutes (* 60 40))
(def one-hour (* 60 60 1))

(defn last-field
  "last field of a CSV string.  we do this often here"
  [s]
  (-> (csv/read-csv s #" COMMA " #"\"")
      first
      last))

(def info-ids
  "from makemkv-oss-1.14.7/makemkvgui/inc/lgpl/apdefs.h
   typedef enum _AP_ItemAttributeId"
  {
   1  :type                                                 ; ap_iaType=1
   2  :name                                                 ; ap_iaName=2
   3  :lang-code                                            ; ap_iaLangCode=3
   4  :lang-name                                            ; ap_iaLangName=4`
   5  :codec-id                                             ; ap_iaCodecId=5
   6  :codec-short                                          ; ap_iaCodecShort=6
   7  :codec-long                                           ; ap_iaCodecLong=7
   8  :chapter-count                                        ; ap_iaChapterCount=8
   9  :duration                                             ; ap_iaDuration=9
   10 :disk/size-pretty                                     ; ap_iaDiskSize=10
   11 :disk/size-bytes                                      ; ap_iaDiskSizeBytes=11
   12 :stream-type-extension                                ; ap_iaStreamTypeExtension=12
   13 :bit-rate-pretty                                      ; ap_iaBitrate=13
   14 :audio-channels-count                                 ; ap_iaAudioChannelsCount=14
   15 :angle-info                                           ; ap_iaAngleInfo=15
   16 :source-file-name                                     ; ap_iaSourceFileName=16
   17 :sample-rate-hz                                       ; ap_iaAudioSampleRate=17
   18 :audio/sample-size                                    ; ap_iaAudioSampleSize=18
   19 :video/size                                           ; ap_iaVideoSize=19
   20 :video/aspect-ratio                                   ; ap_iaVideoAspectRatio=20
   21 :video/frame-rate                                     ; ap_iaVideoFrameRate=21
   22 :stream-flags                                         ; ap_iaStreamFlags=22
   23 :date-time                                            ; ap_iaDateTime=23
   24 :original-title-id                                    ; ap_iaStreamFlags=22
   25 :segments-count                                       ; ap_iaSegmentsCount=25
   26 :segments-map                                         ; ap_iaSegmentsMap=26
   27 :output-file-name                                     ; ap_iaOutputFileName=27
   28 :metadata/lang-code                                   ; ap_iaMetadataLanguageCode=28
   29 :metadata/lang-name                                   ; ap_iaMetadataLanguageName=29
   30 :tree-info                                            ; ap_iaTreeInfo=30
   31 :panel-title                                          ; ap_iaPanelTitle=31
   32 :volume-name                                          ; ap_iaVolumeName=32
   33 :order-weight                                         ; ap_iaOrderWeight=33
   34 :output-format                                        ; ap_iaOutputFormat=34
   35 :output-format-description                            ; ap_iaOutputFormatDescription=35
   36 :seamless-info                                        ; ap_iaSeamlessInfo=36
   37 :panel-text                                           ; ap_iaPanelText=37
   38 :mkv/flags                                            ; ap_iaMkvFlags=38
   39 :mkv/flags-text                                       ; ap_iaMkvFlagsText=39
   40 :audio/channel-layout-name                            ; ap_iaAudioChannelLayoutName=40
   41 :output/codec-short                                   ; ap_iaOutputCodecShort=41
   42 :output/conversion-type                               ; ap_iaOutputConversionType=42
   43 :output/audio-sample-rate                             ; ap_iaOutputAudioSampleRate=43
   44 :output/audio-sample-size                             ; ap_iaOutputAudioSampleSize=44
   45 :output/audio-channels-count                          ; ap_iaOutputAudioChannelsCount=45
   46 :output/audio-channel-layout-name                     ; ap_iaOutputAudioChannelLayoutName=46
   47 :output/audio-channel-layout                          ; ap_iaOutputAudioChannelLayout=47
   48 :output/audio-mix-description                         ; ap_iaOutputAudioMixDescription=48
   49 :comment                                              ; ap_iaComment=49
   50 :offset-sequence-id                                   ; ap_iaOffsetSequenceId=50
   })

(def info-codes
  "from makemkv-oss-1.14.7/makemkvgui/inc/lgpl/apdefs.h"
  {0    nil                                                 ; I made this one up

   3007 :direct-disc-access-mode                            ; "Using direct disc access mode"
   3025 :title-skipped-too-short                            ; "Title #00000.m2ts has length of 23 seconds which is less than minimum title length of 100 seconds and was therefore skipped"
   3307 :file-added                                         ; "File 00000.mpls was added as title #0"
   3309 :title-skipped-duplicate                            ; "Title 00061.mpls is equal to title 00060.mpls and was skipped"

   4001 :empty-subtitles-removed                            ; "Forced subtitles track #5 turned out to be empty and was removed from output file"
   4007 :av-sync-issues                                     ; "AV synchronization issues were found in file 'The Last Starfighter_t01.mkv' (title #2)"

   5000 :output-folder-not-set                              ; "Output folder is not set"
   5004 :dump-done-partial                                  ; static const unsigned long APP_DUMP_DONE_PARTIAL=5004;
   5005 :dump-done                                          ; static const unsigned long APP_DUMP_DONE=5005;
   5009 :init-failed                                        ; static const unsigned long APP_INIT_FAILED=5009;
   5010 :no-disc                                            ; "Failed to open disc"
   5011 :completed                                          ; "Operation successfully completed"
   5013 :ask-folder-create                                  ; static const unsigned long APP_ASK_FOLDER_CREATE=5013;
   5014 :saving-titles                                      ; "Saving 14 titles into directory /Volumes/Drobo/FACTORY/1-Ripped/LAST_STARFIGHTER"
   5016 :folder-invalid                                     ; static const unsigned long APP_FOLDER_INVALID=5016;

   5036 :copy-complete                                      ; "Copy complete. 14 titles saved."

   5085 :loaded-content-hash-table                          ; "Loaded content hash table, will verify integrity of M2TS files."
   5088 :unknown-5088

   6119 :source                                             ; static const unsigned long APP_IFACE_ITEMINFO_SOURCE=6119;
   6120 :title                                              ; static const unsigned long APP_IFACE_ITEMINFO_TITLE=6120;
   6121 :track                                              ; static const unsigned long APP_IFACE_ITEMINFO_TRACK=6121;

   6201 :video                                              ; static const unsigned long APP_TTREE_VIDEO=6201;
   6202 :audio                                              ; static const unsigned long APP_TTREE_AUDIO=6202;
   6203 :subtitles                                          ; static const unsigned long APP_TTREE_SUBPICTURE=6203;

   6206 :dvd                                                ; static const unsigned long DVD_TYPE_DISK=6206;
   6209 :bluray                                             ; static const unsigned long BRAY_TYPE_DISK=6209;
   6212 :hddvd                                              ; static const unsigned long HDDVD_TYPE_DISK=6212;
   })


(defn key-for
  "get a key to use for a given code"
  [id code]
  (let [ns (if-some [decoded (info-codes code)]
             (name decoded))
        n (if-some [decoded (info-ids id)]
            (name decoded)
            (str id))]
    (keyword ns n)))

(def long-type?
  #{:audio-channels-count
    :chapter-count
    :segments-count
    :size-bytes})

(def keyword-type?
  #{:codec-id
    :lang-code
    :metadata/lang-code})

(defn tx-for
  "get an appropriate type converter"
  [type-name]
  (cond (long-type? type-name)
        #(Long/valueOf %)

        (keyword-type? type-name)
        keyword

        :else
        identity))

(defn decode-message
  "decoder for MSG:"
  [code flags count message]
  {:message message
   :code    code
   :flags   flags
   :count   count})

(defn decode-disc-info
  [id code value]
  (let [key (key-for id code)
        tx (tx-for key)]
    [key (tx value)]))

(defn decode-title-info
  [index id code value]
  (let [key (key-for id code)
        tx (tx-for key)]
    {:title-index index
     key          (tx value)}))


(defn decode-stream-info
  [t-index s-index id code value]
  (let [key (key-for id code)
        tx (tx-for key)]
    {:title-index  t-index
     :stream-index s-index
     key           (tx value)}))

(defn check-it
  "tmp function to inspect objects coming to transformers"
  [& xs]
  (println "check-it: " (type xs) xs)
  xs)

(defn ->map
  "vector of pairs to map"
  [& pairs]
  (into {} pairs))

(defn keyed
  [key]
  (fn [x]
    [key x]))

(defn keyed-coll
  [key]
  (fn [& xs]
    [key xs]))

(defn collapse
  "collapse a collection of maps into a map"
  [& maps]
  (reduce conj maps))

(defn sinfos->stream
  "convert a SINFO map to a STREAM map"
  [sinfos]
  (reduce conj sinfos))

(defn separate-streams
  "STREAMS details are written sequentially with no break COMMA  so we have to look at the :stream-index"
  [& sinfos]
  (map sinfos->stream
       (vals (group-by :stream-index
                       sinfos))))

(insta/defparser
  ^{:doc "Parser for output of makemkvcon info"}
  info-parser
  "
MAKEMKVCON-INFO     = HOST-INFO MESSAGES DISC-INFO

HOST-INFO           = MAKEMKV-VERSION DRIVE-INFO DRIVES
MAKEMKV-VERSION     = <'MSG:1005,0,1,'> CONTENT
DRIVE-INFO          = <'MSG:2010,0,1,'> CONTENT
<CONTENT>           =  #'.+' NEW-LINE
DRIVES              = DRV+

DISC-INFO           = ERROR? TCOUNT CINFO* TITLES?
MESSAGES            = MSG+
ERROR               = <'MSG:5010,'> FLAGS COMMA COUNT COMMA CONTENT

TITLES              = TITLE+
TITLE               = TITLE-INFO STREAMS
TITLE-INFO          = TINFO+
STREAMS             = SINFO+

MSG                 = <'MSG:'> CODE COMMA FLAGS COMMA COUNT COMMA MESSAGE COMMA <FORMAT-SPEC> NEW-LINE
DRV                 = <'DRV:'> DRIVE-INDEX COMMA VISIBLE COMMA ENABLED COMMA FLAGS COMMA DRIVE-NAME COMMA DISC-NAME COMMA DEVICE-NAME NEW-LINE
TCOUNT              = <'TCOUNT:'> #'\\d+' NEW-LINE
CINFO               = <'CINFO:'> ID COMMA CODE COMMA QUOTED-STRING NEW-LINE
TINFO               = <'TINFO:'> TITLE-INDEX COMMA ID COMMA CODE COMMA QUOTED-STRING NEW-LINE
SINFO               = <'SINFO:'> TITLE-INDEX COMMA STREAM-INDEX COMMA ID COMMA CODE COMMA QUOTED-STRING NEW-LINE

DRIVE-INDEX         = INTEGER
TITLE-INDEX         = INTEGER
STREAM-INDEX        = INTEGER
ID                  = INTEGER
CODE                = INTEGER

ENABLED             = INTEGER
VISIBLE             = INTEGER
FLAGS               = INTEGER
COUNT               = INTEGER

<DRIVE-NAME>        = QUOTED-STRING
<DISC-NAME>         = QUOTED-STRING
<DEVICE-NAME>       = QUOTED-STRING
<MESSAGE>           = QUOTED-STRING
FORMAT              = QUOTED-STRING
PARAM               = COMMA QUOTED-STRING
FORMAT-SPEC         = FORMAT PARAM*

<QUOTED-STRING>             = <#'\"'> #'[^\"]*' <#'\"'>
<INTEGER>           = #'\\d+'
<COMMA>             = <#','>
<NEW-LINE>          = <#'\n'>")

(def info-transformer
  (partial insta/transform
           {
            :MAKEMKVCON-INFO (fn [host messages disc]
                               {:host     host
                                :disc     disc
                                ;:titles   titles
                                :messages messages})

            :HOST-INFO       ->map
            :DRIVES          (keyed-coll :drives)
            :MESSAGES        (fn [& messages]
                               messages)
            :MSG             decode-message

            :DRIVE           (fn [messages drives]
                               [messages drives])

            :MAKEMKV-VERSION (fn [content]
                               [:makemkv-version (last-field content)])
            :DRIVE-INFO      (fn [content]
                               [:drive (last-field (str/replace content #"\"" ""))])


            :DISC-INFO       ->map
            :TCOUNT          (fn [tcount]
                               [:disc/title-count (Integer/valueOf tcount)])

            :DRIVE-INDEX     #(Integer/valueOf %)
            :TITLE-INDEX     #(Integer/valueOf %)
            :STREAM-INDEX    #(Integer/valueOf %)

            :DRV             (fn [drive-index visible enabled flags drive-name disc-name device-name]
                               {:drive-index drive-index
                                :visible     visible
                                :enabled     enabled
                                :flags       flags
                                :drive-name  drive-name
                                :disc-name   disc-name
                                :device-name device-name})
            :CINFO           decode-disc-info
            :TINFO           decode-title-info
            :SINFO           decode-stream-info

            :TITLE           (fn [title-info streams]
                               {:title-info title-info
                                :streams    streams})
            :TITLES          (fn [& titles]
                               [:titles titles])
            :TITLE-INFO      collapse
            :STREAMS         separate-streams


            :ID              #(Integer/valueOf %)
            :CODE            #(Integer/valueOf %)
            :VISIBLE         #(Integer/valueOf %)
            :ENABLED         #(Integer/valueOf %)
            :FLAGS           #(Integer/valueOf %)
            :COUNT           #(Integer/valueOf %)
            }))

(insta/defparser
  ^{:doc "Parser for output of makemkvcon mkv"}
  mkv-parser
  "
MAKEMKVCON-MKV      = HOST-INFO MESSAGES RESULT

HOST-INFO           = MAKEMKV-VERSION DRIVE-INFO DRIVES
MAKEMKV-VERSION     = <'MSG:1005,0,1,'> CONTENT
DRIVE-INFO          = <'MSG:2010,0,1,'> CONTENT
<CONTENT>           =  #'.+' NEW-LINE

DRIVES              = DRV+
MESSAGES            = MSG+

<RESULT>            = SUCCESS | ERROR
SUCCESS             = TITLES-SAVED COPY-COMPLETE
TITLES-SAVED        = <'MSG:5005,'> FLAGS COMMA COUNT COMMA CONTENT
COPY-COMPLETE       = <'MSG:5036,'> FLAGS COMMA COUNT COMMA CONTENT
ERROR               = <'MSG:5000,'> FLAGS COMMA COUNT COMMA CONTENT

MSG                 = <'MSG:'> CODE COMMA FLAGS COMMA COUNT COMMA MESSAGE COMMA <FORMAT-SPEC> NEW-LINE
DRV                 = <'DRV:'> DRIVE-INDEX COMMA VISIBLE COMMA ENABLED COMMA FLAGS COMMA DRIVE-NAME COMMA DISC-NAME COMMA DEVICE-NAME NEW-LINE

DRIVE-INDEX         = INTEGER
CODE                = INTEGER

ENABLED             = INTEGER
VISIBLE             = INTEGER
FLAGS               = INTEGER
COUNT               = INTEGER

<DRIVE-NAME>        = QUOTED-STRING
<DISC-NAME>         = QUOTED-STRING
<DEVICE-NAME>       = QUOTED-STRING
<MESSAGE>           = QUOTED-STRING
FORMAT              = QUOTED-STRING
PARAM               = COMMA QUOTED-STRING
FORMAT-SPEC         = FORMAT PARAM*

<QUOTED-STRING>     = <#'\"'> #'[^\"]*' <#'\"'>
<INTEGER>           = #'\\d+'
<COMMA>             = <#','>
<NEW-LINE>          = <#'\n'>")


(def mkv-transformer
  (partial insta/transform
           {
            :MAKEMKVCON-MKV  (fn [host messages result]
                               {:host     host
                                :messages messages
                                :result   result})

            :HOST-INFO       ->map
            :DRIVES          (keyed-coll :drives)
            :MESSAGES        (fn [& messages]
                               messages)
            :MSG             (fn [code flags count message]
                               {:message message
                                :code    code
                                :flags   flags
                                :count   count})

            :SUCCESS         (fn [& messages]
                               {:success? true
                                :messages messages})

            :ERROR           (fn [& messages]
                               {:success? false
                                :messages messages})

            :DRIVE           (fn [messages drives]
                               [messages drives])

            :MAKEMKV-VERSION (fn [content]
                               [:makemkv-version (last-field content)])
            :DRIVE-INFO      (fn [content]
                               [:drive (last-field (str/replace content #"\"" ""))])

            :DRIVE-INDEX     #(Integer/valueOf %)

            :DRV             (fn [drive-index visible enabled flags drive-name disc-name device-name]
                               {:drive-index drive-index
                                :visible     visible
                                :enabled     enabled
                                :flags       flags
                                :drive-name  drive-name
                                :disc-name   disc-name
                                :device-name device-name})

            :CODE            #(Integer/valueOf %)
            :VISIBLE         #(Integer/valueOf %)
            :ENABLED         #(Integer/valueOf %)
            :FLAGS           #(Integer/valueOf %)
            :COUNT           #(Integer/valueOf %)}))

(def info
  (comp info-transformer
        info-parser))

(def mkv
  (comp mkv-transformer
        mkv-parser))

(defn duration
  "get duration in seconds from HH:MM:SS"
  [hhmmss]
  (let [[h m s] (str/split hhmmss #":")]
    (+ (Integer/valueOf s)
       (* 60 (Integer/valueOf m))
       (* 60 60 (Integer/valueOf h)))))