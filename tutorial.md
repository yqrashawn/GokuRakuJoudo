# Table of Contents

1.  [Intro](#intro)
    1.  [Basic](#basic)
        1.  [tap a to insert 1, tap b to insert 2](#basic1)
        2.  [tap a to insert 1 only in Google Chrome](#basic2)
        3.  [other conditions](#basic3)
        4.  [Command a to Control 1](#basic4)
        5.  [from simultaneous key](#basic5)
        6.  [to shell command](#basic6)
        7.  [predefined templates](#basic7)
        8.  [Note](#basic8)
    2.  [Advanced](#advanced)
        1.  [variable condition](#advanced1)
        2.  [froms and tos](#advance2)
        3.  [simlayers](#advance3)


<a id="intro"></a>

# Intro

Goku supports (almost?) all the features that Karabiner Elements provides via its [JSON spec](<https://pqrs.org/osx/karabiner/json.html>). 

If you have any question or advice on how to improve the tool, just open an issue or ask questions in the [Telegram group](https://t.me/karabinermac). 

Most docs are comments (after `;;`) in the code block.

<a id="basic"></a>

## Basic


<a id="basic1"></a>

### tap a to insert 1, tap b to insert 2

```clojure
    ;; main contains multiple manipulators
    ;; each manipulator has a description and multiple rules
    
    ;; main                -> {:main [{:des "..." :rules [rule1 rule2 ...]}]}
    ;; manipulator         -> {:des "..." :rules [rule1 rule2 ...]}
    ;; manipulator's rules -> [rule1 rule2 ...]
    {:main [{:des "a to 1, b to 2, c to insert 1 2 3"
             :rules [[:a :1] [:b :2] [:c :1 :2 :3]]}]}
    
    ;; or we can separate them into two manipulators
    {:main [{:des "a to 1" :rules [[:a :1]]}
            {:des "b to 2" :rules [[:b :2]]}
            {:des "c to insert 123" :rules [[:c [:1 :2 :3]]]}]}
    
    ;; in rules [:a :1] -- [<from> <to>]
    ;; it means from key a to key 1
    ;; these keycode is just original karabiner keycode prefix with colon
```

You can find all keycode definition in [this file](https://github.com/yqrashawn/GokuRakuJoudo/blob/master/src/karabiner_configurator/keys_info.clj) or use the Karabiner-EventViewer.app


<a id="basic2"></a>

### tap a to insert 1 only in Google Chrome

```clojure
    {;; define application identifiers
     :applications {:chrome ["^com\\.google\\.Chrome$"]}
     :main [{:des "a to 1 only in chrome" :rules [[:a :1 :chrome]]}]}
    
    ;; [:a :1 :chrome] -- [<from> <to> <conditions>]
    ;; this means from a to 1 under the condition of chrome
    ;; :chrome is a predefined application condition
    ;; it's same with karabiner
    
    ;; and we can use multiple conditions
    {:applications {:chrome ["^com\\.google\\.Chrome$"]
                    :safari ["^com\\.apple\\.Safari$"]}
     :main [{:des "a to 1 only in chrome, safari" :rules [[:a :1 [:chrome :safari]]]}]}
    
    ;; we can also prefix use the opposite condition
    {:applications {:chrome ["^com\\.google\\.Chrome$"]
                    :safari ["^com\\.apple\\.Safari$"]}
     :main [{:des "a to 1 only outside chrome, safari" :rules [[:a :1 [:!chrome :!safari]]]}]}
```

<a id="basic3"></a>

### other conditions

```clojure
    {:devices {:hhkb-bt [{:vendor_id 1278 :product_id 51966}]}
    
     :applications {:chromes ["^com\\.google\\.Chrome$" "^com\\.google\\.Chrome\\.canary$"]}
    
     :input-sources {:us {:input_mode_id ""
                          :input_source_id "com.apple.keylayout.US"
                          :language "en"}}
    
     :main [{:des "a to 1 multiple conditions"
             :rules [[:a :1 [:chromes :hhkb-bt :us]]]}]}
    
    ;; so this means press a to insert 1 in Google Chrome and Google Chrome Canary
    ;; while we are using the US input method, and the device that press a is
    ;; HHKB-BT keyboard.
```

The only condition that Goku dose not support is [keyboard type](https://pqrs.org/osx/karabiner/json.html#condition-definition-keyboard-type).


<a id="basic4"></a>

### Command a to Control 1

```clojure
    {:main [{:des "command a to control 1" [:!Ca :!T1]}]}
    
    ;; this is a little bit weird, but it's convenient
    ;; the rule [:!Ca :!T1]
    ;; means from command a to control 1
    ;; :!Ca is keycode :a and prefix a with !C
    
    ;; here's the definition
    
    ;; !  | means mandatory
    ;; #  | means optional
    ;; C  | left_command
    ;; T  | left_control
    ;; O  | left_option
    ;; S  | left_shift
    ;; F  | fn
    ;; Q  | right_command
    ;; W  | right_control
    ;; E  | right_option
    ;; R  | right_shift
    ;; !! | mandatory command + control + optional + shift (hyper)
    ;; ## | optional any
    
    ;; examples
    
    ;; !CTSequal_sign  | mandatory command control shift =
    ;;                 | which is command control +
    ;; !O#Sright_arrow | mandatory option optional any right_arrow
    
    ;; karabiner definition of mandatory and optional
    ;; https://pqrs.org/osx/karabiner/json.html#from-event-definition-modifiers
    
    ;; rule [<from> <to>]
    ;; if simplified modifier is used in <to>, optional(#) definition will be
    ;; ignored.
```

<a id="basic5"></a>

### from simultaneous key

Karabiner also has this functionality that can map simultaneous key presses to other events. You can use that in Goku as well.

```clojure
    {:main [{:des "simultaneous j l press to F19" :rules [[[:j :l] :f19]]}]}
    ;; rule [[:j :l] :f19]
    ;;       <from>  <to>
    
    ;; so when from is a vector (or array), Goku will parse it as
    ;; simultaneous key press
```


<a id="basic6"></a>

### to shell command

You can set <to> to string to invoke shell command.

```clojure
    {:main [{:des "hyper 1 to cleanup personal folder"
             :rules [[:!!1 "rm -r ~/personal && mkdir ~/personal"]]}]}
    
    ;; or we can break it into two <to>
    
    {:main [{:des "hyper 1 to cleanup personal folder"
             :rules [[:!!1 ["rm -r ~/personal" "mkdir ~/personal"]]]}]}
```


<a id="basic7"></a>

### predefined templates

When we use karabiner to run shell commands or applescripts, we write them as string. The problem is these strings are often similar and really long. So there's predefined templates.

It's same with those string templates in some programming languages. In fact, it use the clojure's string templates, which is actually java's string templates implementation. eg.

Goku support omitting the last n parameters after version 0.2.1.

```clojure
    {:templates {:launch "osascript -e 'tell application \"Alfred 3\" to run trigger \"launch%s\" in workflow \"yqrashawn.workflow.launcher\" with argument \"%s\"'"}
     :main [{:des "launcher mode"
             :rules [[:j [:launch "Alacritty"] :launch-mode]
                     [:k [:launch "Emacs"] :launch-mode]
                     [:l [:launch "Chrome"] :launch-mode]
                     [:m [:launch "Mail"] :launch-mode]
                     [:v [:launch "WeChat"] :launch-mode]
                     [:q [:launch "KE"] :launch-mode]
                     [:f [:launch "Finder"] :launch-mode]
                     [:9 [:launch "PDFExpert"] :launch-mode]
                     [:comma [:launch "Safari"] :launch-mode]
                     [:period [:launch "Paw"] :launch-mode]
                     [:equal_sign [:launch "Textual"] :launch-mode]
                     [:8 [:launch "KEEvents"] :launch-mode]
                     [:b [:launch "BearyChat"] :launch-mode]
                     [:t [:launch "TG"] :launch-mode]]}]}
    
    ;; This is my configuration to launch or display applications with wj wk wl etc.
    ;; First I define the :launch template. Then I use it in <to>.
    
    ;; [:j [:launch "Alacritty"] :launch-mode]
    ;; Goku will parse the rule and replace "%s" with "Alacritty".
    ;; The "%s" is for string. If you need other formats, check out here.
    ;; java.util.Formatter doc:
    ;; https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html
```


<a id="basic8"></a>

### Note

Don't define conditions with the name of keycode. If you define a application condition like `{:a ["^com\\.google\\.Chrome$"]}`. It might work in rules but it may broke other things.


<a id="advanced"></a>

## Advanced


<a id="advanced1"></a>

### variable condition

Karabiner's variable condition functionality make it posible to define keyboard layers. You can use this functionality to use most keys as modifier keys. Goku makes it really easy to use variable conditions.

```clojure
    {:main [{:des "tap w to set w-layer to 1"
             :rules [[:w ["w-layer" 1]]]}]}
    
    ;; this means tap w to set variable "w layer" to 1
    ;; rule [:w     ["w layer" 1]]
    ;;      |____| |____________|
    ;;       <from>     <to>
    
    ;; we can also set multiple <to>, and use the defined variable in <conditions>
    {:main [{:des "tap w to insert w then set w-layer to 1"
             :rules [[:w [:w ["w-layer" 1]]]
                     [:1 [:1 :w] :w-layer]]}]}
    
    ;; rule [:w    [:w ["w layer" 1]]]
    ;;      |____| |_______________|
    ;;      <from>       <to>
```

The rules above are not really useful. Cause we lose `w` key in the first rule and we can't set variable back in both rules. The old way in karabiner to define layer is using the `to_if_alone` option. We will talk about it soon.


<a id="advance2"></a>

### froms and tos

So in the karabiner.json spec, there're [from event definition](https://pqrs.org/osx/karabiner/json.html#from-event-definition) and [to event definition](https://pqrs.org/osx/karabiner/json.html#to-event-definition). We can predefine this in Goku as well.

1.  froms definition

    Since karabiner can send multiple to event triggerd by a single from event, we won't froms definition too much. There're two kinds of situation that we might want to use this.
    
```clojure
        ;; from any key_code, consumer_key_code or pointing_button
        {:froms {:from-any-consumer-key {:any "consumer_key_code"}}
         :main [{:des "disable all consumer key"
                 :rules [[:from-any-consumer-key :vk_none]]}]}
        {:froms {:from-any-keycode-key {:any "key_code"}}
         :main [{:des "disable keycode key"
                 :rules [[:from-any-keycode-key :vk_none]]}]}
        {:froms {:from-any-pointing-button {:any "pointing_button"}}
         :main [{:des "disable all pointing button"
                 :rules [[:from-any-keycode-key :vk_none]
                         [{:any "pointing_button"} :vk_none]]}]}
        
        ;; configs above disable all kinds of keys.
        ;; rule [<from> <to> <conditions>]
        ;; we can put predefined from keyword into <from> section
        ;; or we can just put the from definition map into <from> section, since we
        ;; usually only use each froms one time.
        
        ;; WARNING
        ;; This is just a demonstration of what can be done with :any. DON'T TRY any of
        ;; these three rules, they will disable all your keyboard key and mouse button.
        
        ;; set simultaneous_options
        {:sim [:f :j]
         :simo {:interrupt true
                :dorder :strict
                :uorder :strict_inverse
                :afterup {:set ["fj layer" 1]}}}
```
    
    The simultaneous<sub>options</sub> won't be used frequently. You can find the the detail in the [froms documentation](https://github.com/yqrashawn/GokuRakuJoudo/blob/master/src/karabiner_configurator/froms.clj#L9), which is above its implementation.

2.  tos definition

    Tos is used more often than froms. It's the same idea as froms definition. You can find the detailed [tos documentation](https://github.com/yqrashawn/GokuRakuJoudo/blob/master/src/karabiner_configurator/tos.clj#L7) in the implementation file. There's shot cuts for tos in rules' <to>, like string to shell commands and multiple to definitions in vector.
    
    You only need to use to definition if you want to use or set `select_input_source`, `mouse_key`, `lazy`, `repeat`, `halt`, `hold_down_milliseconds`.


<a id="advance3"></a>

### simlayers

In karabiner, there's two kinds of layers implementation. I'll just call them the old layer and simlayer. I don't know if I can explain this clearly. You may really understand this after tring these two kinds of config. If you are familier with the karabiner.json configuration, you can compare [the old layer example](https://github.com/pqrs-org/KE-complex_modifications/blob/b944d9970aa256f7e86a191e6407a0f9685d511d/docs/json/vi_mode.json#L67) and the [new layer example](https://github.com/pqrs-org/KE-complex_modifications/blob/0417c1ead9455cb101af0cd52ab158a3bfb89b66/docs/json/vi_mode.json#L7).

1.  explanation

    TLDR;
    
    Basically, if you type fast, use simlayer, otherwise, use the old layer. If you don't care about this, you can just jump to the next header, which is how to set this in Goku.
    
    The old layer has the same definition as "layers" in thoes keyboard firmware keymap editors. eg.
    
```
        press w key down --> in w layer ("w layer" set to 1)
        tap 1 key      --> trigger key 1's definition under w layer ("w layer" is 0)
        tap 2 key      --> trigger key 2's definition under w layer ("w layer" is 0)
        release w key up --> out w layer ("w layer" is 0)
```
    
    There're two problems in old layer. When we type "w1" really fast, we trigger the "1" in w layer rather than insert "w1". When we keep press w key down, the w key won't repeat. There won't be a "wwwwwwwwwwwwwwwwwwww".
    
    The karabiner's simlayer is based on its [simultaneous](https://pqrs.org/osx/karabiner/json.html#simultaneous) functionality. It's like this. The `-->` is the symbol of time.
    
```
        press w key down --> if in threshold milliseconds
                            --> press 1 key ("w layer" set to 1)
                            --> in w layer and trigger the 1 definition
                                --> press 2 key even after the threshold ("w layer" is still 1)
                                --> in w layer and trigger the 2 definition
                                    --- we hold the w key for 1 year ---> ("w layer" is still 1)
                                    release w to set "w layer" to 0
                         --> if after threshold milliseconds
                         --> w key begin to repeat, we get "wwwwwwwwwwwww"
```
    
    This solves those two problems. But we need to trigger the second key fast, or the first key starts to repeat. We need must trigger a action the same time we enter a layer. We can't enter the layer in advance and think what we really want to do in that layer.

2.  layer and simlayer in Goku.

```clojure
        ;; simlayer
        {:simlayers {:period-mode {:key :period}}
         :main [{:des "period mode"
                 :rules [[:d :!S9 :period-mode] ;; .d insert (
                         [:f :!S0 :period-mode] ;; .f insert )
                         [:a [:!Sgrave_accent_and_tilde :slash] :period-mode] ;; .a insert ~/
                         [:s [:period :!S8] :period-mode]]}]} ;; .s insert .*
        
        ;; layer
        ;; I've thought about implement a predefined layer section, but it's just
        ;; already really easy to set up with what we have now.
        {:main [{:des "period mode"
                 :rules [[:period ["period-mode" 1] nil {:afterup ["period-mode" 0]
                                                         :alone :period}]
                         [:d :!S9 ["period-mode" 1]]
                         [:f :!S0 ["period-mode" 1]]
                         [:a [:!Sgrave_accent_and_tilde :slash ] ["period-mode" 1]]
                         [:s [:period :!S8] ["period-mode" 1]]]}]}
        
        ;; So the first rule is to define period down trigger set variable so that we
        ;; enter the layer.
        ;; rule [:period ["period-mode" 1] nil {:afterup ["period-mode" 0] :alone :period}]
        ;;       |_____| |_______________| |_| |_________________________________________|
        ;;        <from>    <to>      <conditions>         <other options>
        
        ;; so we have a <other options> here, and we know that <conditions> can be nil
        
        ;; rule is actually the manipulator in karabienr.json, checkout here
        ;; https://pqrs.org/osx/karabiner/json.html#complex_modifications-manipulator-definition
        
        ;; We can see there are ~type~, ~from~, ~to~, ~to_if_alone~, ~to_if_held_down~,
        ;; ~to_after_key_up~, ~to_delayed_action~, ~description~, ~conditions~,
        ;; ~parameters~. We already have <from> <to> <conditions>, and we can omit
        ;; ~type~ and ~description~.
        
        ;; So <other options> includs ~to_if_alone~, ~to_if_held_down~,
        ;; ~to_after_key_up~, ~to_delayed_action~ and ~parameters~.
        
        ;; The first 4 is same as tos definition, and we also have shotcusts for ~parameters~.
```

    
You can checkout [the documentation for <other options>](https://github.com/yqrashawn/GokuRakuJoudo/blob/a9f2551e1961aab3549fd9e7622b40fd6304b27b/src/karabiner_configurator/rules.clj#L170).  
And there're also [<to> documentation](https://github.com/yqrashawn/GokuRakuJoudo/blob/a9f2551e1961aab3549fd9e7622b40fd6304b27b/src/karabiner_configurator/rules.clj#L90), [<from> documentation](https://github.com/yqrashawn/GokuRakuJoudo/blob/a9f2551e1961aab3549fd9e7622b40fd6304b27b/src/karabiner_configurator/rules.clj#L11), [<conditions> documentation](https://github.com/yqrashawn/GokuRakuJoudo/blob/a9f2551e1961aab3549fd9e7622b40fd6304b27b/src/karabiner_configurator/rules.clj#L157).

